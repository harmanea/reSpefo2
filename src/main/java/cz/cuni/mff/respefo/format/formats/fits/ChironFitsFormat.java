package cz.cuni.mff.respefo.format.formats.fits;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.format.InvalidFileFormatException;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.format.XYSeries;
import cz.cuni.mff.respefo.util.DoubleArrayList;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.ImageHDU;

public class ChironFitsFormat extends ImportFitsFormat {

    @Override
    public Spectrum importFrom(String fileName) throws SpefoException {
        FitsFactory.setAllowHeaderRepairs(true);
        try {
            return super.importFrom(fileName);
        } finally {
            FitsFactory.setAllowHeaderRepairs(false);
        }
    }

    @Override
    protected XYSeries parseData(ImageHDU imageHdu) throws SpefoException {
        try {
            float[][][] data = (float[][][]) imageHdu.getKernel();

            DoubleArrayList xList = new DoubleArrayList();
            DoubleArrayList yList = new DoubleArrayList();

            for (float[][] matrix : data) {
                for (float[] row : matrix) {
                    int k = 0;
                    while (k < row.length) {
                        xList.add(row[k++]);
                        yList.add(row[k++]);
                    }
                }
            }

            return new XYSeries(xList.toArray(), yList.toArray());

        } catch (ClassCastException exception) {
            throw new InvalidFileFormatException("The HDU kernel is not a 3-D array of type float");
        }
    }

    @Override
    public String name() {
        return "CTIO Chiron";
    }

    @Override
    public String description() {
        return "The CTIO Chiron FITS format.\n\n" +
                "Pixels extracted along each echel are stored separately in a 3-D array of type float.\n\n" +
                "This format is used by the Cerro Tololo Inter-American Observatory (CTIO) to store data obtained using CHIRON, the optical high-resolution echelle spectrometer.";
    }

    @Override
    public boolean isDefault() {
        return false;
    }
}
