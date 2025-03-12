package cz.cuni.mff.respefo.spectrum.port.fits;

import cz.cuni.mff.respefo.exception.InvalidFileFormatException;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.spectrum.format.SimpleSpectrum;
import cz.cuni.mff.respefo.util.collections.FitsFile;
import cz.cuni.mff.respefo.util.collections.JulianDate;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import nom.tam.fits.Header;

import java.time.LocalDateTime;

public class HerculesFitsFormat extends ImportFitsFormat {

    @Override
    protected Spectrum createSpectrum(FitsFile fits) throws SpefoException {
        double[][] data = castData(fits);

        double[] xSeries = new double[data.length];
        double[] ySeries = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            xSeries[i] = data[i][0];
            ySeries[i] = data[i][1];
        }

        XYSeries series = new XYSeries(xSeries, ySeries);
        return new SimpleSpectrum(series);
    }

    private double[][] castData(FitsFile fits) throws InvalidFileFormatException {
        try {
            return (double[][]) fits.getData();
        } catch (ClassCastException exception) {
            throw new InvalidFileFormatException("The HDU kernel is not a 2-D array of type double");
        }
    }

    @Override
    public JulianDate getHJD(Header header) {
        return JulianDate.fromRJD(header.getDoubleValue("JD"));
    }

    @Override
    public LocalDateTime getDateOfObservation(Header header) {
        String date = header.getStringValue("DATE");
        try {
            return LocalDateTime.parse(date.substring(1, date.length() - 1));
        } catch (Exception exception) {
            return LocalDateTime.MIN;
        }
    }

    @Override
    public double getRVCorrection(Header header) {
        return header.getDoubleValue("BCORR");
    }

    @Override
    public double getExpTime(Header header) {
        return header.getDoubleValue("START");
    }

    @Override
    public String name() {
        return "Hercules";
    }

    @Override
    public String description() {
        return "The Hercules FITS format.\n\n" +
                "Values are stored in a 2-D array in a non-standard way along with some non-standard header cards.\n\n" +
                "This format is used by the University of Canterbury Mount John Observatory (UCMJO) to store data obtained using HERCULES, the vacuum fibre-fed echelle spectrograph used on the McLellan 1-metre telescope.";
    }

    @Override
    public boolean isDefault() {
        return false;
    }
}
