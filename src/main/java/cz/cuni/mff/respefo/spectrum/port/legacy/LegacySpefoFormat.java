package cz.cuni.mff.respefo.spectrum.port.legacy;

import cz.cuni.mff.respefo.exception.InvalidFileFormatException;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.rectify.RectifyAsset;
import cz.cuni.mff.respefo.function.rectify.RectifyFunction;
import cz.cuni.mff.respefo.function.rv.MeasureRVFunction;
import cz.cuni.mff.respefo.function.rv.MeasureRVResult;
import cz.cuni.mff.respefo.function.rv.MeasureRVResults;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.spectrum.format.SimpleSpectrum;
import cz.cuni.mff.respefo.spectrum.port.ImportFileFormat;
import cz.cuni.mff.respefo.util.collections.DoubleArrayList;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import cz.cuni.mff.respefo.util.utils.MathUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static cz.cuni.mff.respefo.util.Constants.SPEED_OF_LIGHT;
import static cz.cuni.mff.respefo.util.utils.CollectionUtils.listOf;
import static java.util.Arrays.stream;

public class LegacySpefoFormat implements ImportFileFormat {
    private static final List<String> FILE_EXTENSIONS = listOf("uui", "rui", "rci");

    @Override
    public List<String> fileExtensions() {
        return FILE_EXTENSIONS;
    }

    @Override
    public String name() {
        return "Legacy format";
    }

    @Override
    public String description() {
        return "The legacy format used by the original Spefo.\n\n" +
                "Data is stored in a proprietary way in a binary file. The file header may contain extra information such as a short comment or points used for rectification.\n\n" +
                "If there is a matching .con file, it is imported as well.";
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public Spectrum importFrom(String fileName) throws SpefoException {
        LegacySpefoFile spefoFile = new LegacySpefoFile(fileName);

        switch (FileUtils.getFileExtension(fileName)) {
            case "uui":
                String conFileName = FileUtils.replaceFileExtension(fileName, "con");
                if (Files.exists(Paths.get(conFileName))) {
                    LegacyConFile conFile = new LegacyConFile(conFileName);
                    spefoFile.setRemark(conFile.getRemark());
                    spefoFile.setRectX(conFile.getRectX());
                    spefoFile.setRectY(conFile.getRectY());
                }
                break;

            case "rui":
            case "rci":
                spefoFile.setYSeries(ArrayUtils.divideArrayValues(spefoFile.getYSeries(), spefoFile.getMaxInt()));
                break;

            default:
                throw new IllegalStateException("Unexpected file extension encountered");
        }

        double[] xSeries = new double[spefoFile.getYSeries().length];
        for (int i = 0; i < xSeries.length; i++) {
            double[] coefficients = Arrays.copyOf(spefoFile.getDispCoef(), 6);
            xSeries[i] = MathUtils.polynomial(i, coefficients);

            if (spefoFile.getRvCorr() != 0) {
                xSeries[i] += spefoFile.getRvCorr() * (xSeries[i] / SPEED_OF_LIGHT);
            }
        }

        Spectrum spectrum = new SimpleSpectrum(new XYSeries(xSeries, spefoFile.getYSeries()));
        spectrum.setOrigin(createOrigin(spefoFile, fileName));

        if (spefoFile.getRvCorr() != 0) {
            spectrum.setRvCorrection(spefoFile.getRvCorr());
        }

        if (spefoFile.isRectified()) {
            RectifyAsset rectifyAsset = new RectifyAsset(
                    stream(spefoFile.getRectX())
                            .mapToDouble(index -> xSeries[index - 1])
                            .collect(DoubleArrayList::new, DoubleArrayList::add, DoubleArrayList::addAll),
                    spefoFile.getRectY()
            );
            spectrum.putFunctionAsset(RectifyFunction.RECTIFY_SERIALIZE_KEY, rectifyAsset);
        }

        String rvFileName = FileUtils.replaceFileExtension(fileName, "rv");
        if (Files.exists(Paths.get(rvFileName))) {
            try (BufferedReader br = new BufferedReader(new FileReader(rvFileName))) {
                MeasureRVResults results = new MeasureRVResults();

                double[] coefficients = new double[6];
                for (int i = 0; i < 6; i++) {
                    coefficients[i] = Double.parseDouble(br.readLine());
                }
                double deltaRV = Double.parseDouble(br.readLine());

                br.lines().forEach(line -> {
                    try {
                        results.add(parseRvLine(line, coefficients, deltaRV));
                    } catch (NumberFormatException numberFormatException) {
                        Log.error("Couldn't load measurement", numberFormatException);
                    }

                });

                spectrum.putFunctionAsset(MeasureRVFunction.SERIALIZE_KEY, results);

            } catch (IOException exception) {
                throw new InvalidFileFormatException("Rv file has invalid format", exception);
            }
        }

        return spectrum;
    }

    private LegacySpefoOrigin createOrigin(LegacySpefoFile spefoFile, String fileName) {
        LegacySpefoOrigin origin = new LegacySpefoOrigin(fileName);
        origin.setRemark(spefoFile.getRemark());
        origin.setUsedCal(spefoFile.getUsedCal());
        origin.setStarStep(spefoFile.getStarStep());
        origin.setDispCoef(spefoFile.getDispCoef());
        origin.setMinTransp(spefoFile.getMinTransp());
        origin.setMaxInt(spefoFile.getMaxInt());
        origin.setFilterWidth(spefoFile.getFilterWidth());
        origin.setReserve(spefoFile.getReserve());

        return origin;
    }

    private MeasureRVResult parseRvLine(String line, double[] coefficients, double deltaRV) {
        String[] tokens = line.trim().replaceAll(" +", " ").split("\\s+", 6);

        double position = Double.parseDouble(tokens[0]);
        double radius = Double.parseDouble(tokens[1]);
        String category = tokens[3];
        double l0 = Double.parseDouble(tokens[4]);
        String name = tokens[5];

        double measured = MathUtils.polynomial(position - 1, coefficients);
        double shift = l0 - measured;
        double rv = (measured * deltaRV - l0) * (SPEED_OF_LIGHT / l0);

        if (category.equals("10")) {
            category = "corr";
        }

        return new MeasureRVResult(rv, shift, radius, category, l0, name, "");
    }
}
