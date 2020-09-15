package cz.cuni.mff.respefo.format.formats.fits;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.format.XYSeries;
import cz.cuni.mff.respefo.format.formats.ExportFileFormat;
import cz.cuni.mff.respefo.util.VersionInfo;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.header.DataDescription;
import nom.tam.fits.header.Standard;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExportFitsFormat extends FitsFormat implements ExportFileFormat {

    @Override
    public void exportTo(Spectrum spectrum, String fileName) throws SpefoException {
        try (Fits fits = new Fits()) {
            XYSeries series = spectrum.getProcessedSeries();

            BasicHDU<?> hdu;
            if (ArrayUtils.valuesHaveSameDifference(series.getXSeries())) {
                hdu = FitsFactory.hduFactory(series.getYSeries());

                hdu.addValue(Standard.CRPIXn.n(1), 1);
                hdu.addValue(Standard.CRVALn.n(1), series.getX(0));
                hdu.addValue(Standard.CDELTn.n(1), series.getX(1) - series.getX(0));
            } else {
                hdu = FitsFactory.hduFactory(new double[][] { series.getXSeries(), series.getYSeries() });
            }

            hdu.addValue(DataDescription.PROGRAM, "reSpefo " + VersionInfo.getVersion());
            hdu.addValue(Standard.DATE, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            // TODO: add more info based on the origin

            fits.addHDU(hdu);

            fits.write(new File(fileName));
        } catch (IOException | FitsException exception) {
            throw new SpefoException("Error while writing to file", exception);
        }
    }
}
