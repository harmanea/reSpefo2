package cz.cuni.mff.respefo.format.formats.legacy;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.format.formats.ImportFileFormat;
import cz.cuni.mff.respefo.function.rectify.RectifyAsset;
import cz.cuni.mff.respefo.function.rectify.RectifyFunction;
import cz.cuni.mff.respefo.util.collections.DoubleArrayList;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import cz.cuni.mff.respefo.util.utils.MathUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static cz.cuni.mff.respefo.util.Constants.SPEED_OF_LIGHT;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

public class LegacySpefoFormat implements ImportFileFormat {
    private static final List<String> FILE_EXTENSIONS = unmodifiableList(asList("uui", "rui", "rci"));

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

            default: throw new IllegalStateException("Unexpected file extension encountered");
        }

        double[] xSeries = new double[spefoFile.getYSeries().length];
        for (int i = 0; i < xSeries.length; i++) {
            xSeries[i] = MathUtils.polynomial(i, spefoFile.getDispCoef());

            if (spefoFile.getRvCorr() != 0) {
                xSeries[i] += spefoFile.getRvCorr() * (xSeries[i] / SPEED_OF_LIGHT);
            }
        }

        Spectrum spectrum = new Spectrum(new XYSeries(xSeries, spefoFile.getYSeries()));
        spectrum.setOrigin(createOrigin(spefoFile, fileName));

        if (spefoFile.getRvCorr() != 0) {
            spectrum.setRvCorrection(spefoFile.getRvCorr());
        }

        if (spefoFile.isRectified()) {
            RectifyAsset rectifyAsset = new RectifyAsset(
                    new DoubleArrayList(Arrays.stream(spefoFile.getRectX()).mapToDouble(index -> xSeries[index]).toArray()),
                    spefoFile.getRectY()
            );
            spectrum.putFunctionAsset(RectifyFunction.SERIALIZE_KEY, rectifyAsset);
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
}
