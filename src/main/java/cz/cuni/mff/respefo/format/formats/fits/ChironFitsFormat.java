package cz.cuni.mff.respefo.format.formats.fits;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.format.InvalidFileFormatException;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.util.collections.FitsFile;
import cz.cuni.mff.respefo.util.collections.Point;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import nom.tam.fits.FitsFactory;

import java.util.SortedSet;
import java.util.TreeSet;

public class ChironFitsFormat extends ImportFitsFormat {

    @Override
    public Spectrum importFrom(String fileName) throws SpefoException {
        boolean previousSetting = FitsFactory.isAllowHeaderRepairs();
        FitsFactory.setAllowHeaderRepairs(true);
        try {
            return super.importFrom(fileName);
        } finally {
            FitsFactory.setAllowHeaderRepairs(previousSetting);
        }
    }

    @Override
    public XYSeries parseData(FitsFile fits) throws SpefoException {
        float[][][] data = castData(fits);

        SortedSet<Point> points = new TreeSet<>();
        for (int i = 0; i <= data.length - 1; i++) {
            float[][] matrix = data[i];
            for (float[] row : matrix) {
                if (row.length != 2) {
                    throw new InvalidFileFormatException("The last dimension is not of size 2");
                }

                points.add(new Point(row[0], row[1]));
            }
        }

        double[] xSeries = new double[points.size()];
        double[] ySeries = new double[points.size()];

        int i = 0;
        for (Point point : points) {
            xSeries[i] = point.getX();
            ySeries[i] = point.getY();
            i++;
        }

        return new XYSeries(xSeries, ySeries);
    }

    private float[][][] castData(FitsFile fits) throws InvalidFileFormatException {
        try {
            return (float[][][]) fits.getData();
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
