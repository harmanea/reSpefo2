package cz.cuni.mff.respefo.spectrum.port.fits;

import cz.cuni.mff.respefo.exception.InvalidFileFormatException;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.spectrum.format.EchelleSpectrum;
import cz.cuni.mff.respefo.util.collections.FitsFile;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.collections.tuple.Triplet;
import cz.cuni.mff.respefo.util.collections.tuple.Tuple;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class OESFitsFormat extends ImportFitsFormat {

    // https://astro.uni-bonn.de/~sysstw/lfa_html/iraf/noao.onedspec.specwcs.html
    // specN = ap beam dtype w1 dw nw z aplow aphigh
    // assert dtype == 0, nw = data[i].length
    //  w = (w1 + dw * (p - 1)) / (1 + z)

    private static final String REGEX = "spec\\d+ ?= \"([ \\d.]+)\"";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    @Override
    protected Spectrum createSpectrum(FitsFile fits) throws InvalidFileFormatException {
        float[][] data = castData(fits);
        String multispecHeaderString = extractMultispecHeaderString(fits.getHeader());
        List<Triplet<Double, Double, Double>> dispersionValues = extractDispersionValues(multispecHeaderString);

        XYSeries[] series = new XYSeries[data.length];

        for (int i = 0; i < data.length; i++) {
            float[] dataSlice = data[i];

            double[] ySeries = IntStream.range(0, dataSlice.length).mapToDouble(j -> dataSlice[j]).toArray();

            Triplet<Double, Double, Double> dispersion = dispersionValues.get(i);
            double w1 = dispersion.a;
            double dw = dispersion.b;
            double z = dispersion.c;

            double[] xSeries = ArrayUtils.createArray(dataSlice.length, j -> (w1 + dw * j) / (1 + z));

            series[i] = new XYSeries(xSeries, ySeries);
        }

        return new EchelleSpectrum(series);
    }

    private float[][] castData(FitsFile fits) throws InvalidFileFormatException {
        try {
            return (float[][]) fits.getData();
        } catch (ClassCastException exception) {
            throw new InvalidFileFormatException("The HDU kernel is not a 2-D array of type float");
        }
    }

    private String extractMultispecHeaderString(Header header) {
        StringBuilder sb = new StringBuilder();

        int i = 1;
        String value;
        while ((value = header.getStringValue(String.format("WAT2_%03d", i))) != null) {
            sb.append(value);

            if (value.length() < HeaderCard.MAX_STRING_VALUE_LENGTH) {
                sb.append(" ");  // replace missing trailing space
            }

            i++;
        }

        return sb.toString();
    }

    private List<Triplet<Double, Double, Double>> extractDispersionValues(String multispecHeaderString) throws InvalidFileFormatException {
        List<Triplet<Double, Double, Double>> values = new ArrayList<>();

        Matcher matcher = PATTERN.matcher(multispecHeaderString);
        while (matcher.find()) {
            String spec = matcher.group(1);
            String[] tokens = spec.split(" ");

            try {
                double w1 = Double.parseDouble(tokens[3]);
                double dw = Double.parseDouble(tokens[4]);
                double z = Double.parseDouble(tokens[6]);

                values.add(Tuple.of(w1, dw, z));

            } catch (NumberFormatException exception) {
                throw new InvalidFileFormatException("Couldn't parse multispec dispersion coordinates", exception);
            }
        }

        return values;
    }

    @Override
    public String name() {
        return "OES";
    }

    @Override
    public String description() {
        return "The OES FITS format.\n\n" +
                "Values extracted along each echelle are stored in IRAF multispec format separately in a 2-D array of type float.\n\n" +
                "This format is used by the Ondřejov Observatory to store data obtained using OES, an echelle spectrograph.";
    }

    @Override
    public boolean isDefault() {
        return false;
    }
}
