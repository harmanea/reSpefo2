package cz.cuni.mff.respefo.spectrum.port.fits;

import cz.cuni.mff.respefo.exception.InvalidFileFormatException;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.spectrum.format.EchelleSpectrum;
import cz.cuni.mff.respefo.util.collections.FitsFile;
import cz.cuni.mff.respefo.util.collections.XYSeries;

public class ChironFitsFormat extends ImportFitsFormat {

    @Override
    protected Spectrum createSpectrum(FitsFile fits) throws InvalidFileFormatException {
        float[][][] data = castData(fits);

        XYSeries[] series = new XYSeries[data.length];

        for (int i = 0; i < data.length; i++) {
            float[][] matrix = data[i];

            double[] xSeries = new double[matrix.length];
            double[] ySeries = new double[matrix.length];

            for (int j = 0; j < matrix.length; j++) {
                assertSize(matrix[j]);

                xSeries[j] = matrix[j][0];
                ySeries[j] = matrix[j][1];
            }

            series[i] = new XYSeries(xSeries, ySeries);
        }

        return new EchelleSpectrum(series);
    }

    private float[][][] castData(FitsFile fits) throws InvalidFileFormatException {
        try {
            return (float[][][]) fits.getData();
        } catch (ClassCastException exception) {
            throw new InvalidFileFormatException("The HDU kernel is not a 3-D array of type float");
        }
    }

    private void assertSize(float[] row) throws InvalidFileFormatException {
        if (row.length != 2) {
            throw new InvalidFileFormatException("The last dimension is not of size 2");
        }
    }

    @Override
    public String name() {
        return "CTIO Chiron";
    }

    @Override
    public String description() {
        return "The CTIO Chiron FITS format.\n\n" +
                "Pixels extracted along each echelle are stored separately in a 3-D array of type float.\n\n" +
                "This format is used by the Cerro Tololo Inter-American Observatory (CTIO) to store data obtained using CHIRON, the optical high-resolution echelle spectrometer.";
    }

    @Override
    public boolean isDefault() {
        return false;
    }
}
