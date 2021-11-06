package cz.cuni.mff.respefo.spectrum.port.fits;

import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.spectrum.port.ExportFileFormat;
import cz.cuni.mff.respefo.spectrum.port.ascii.AsciiOrigin;
import cz.cuni.mff.respefo.spectrum.port.legacy.LegacySpefoOrigin;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.info.VersionInfo;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import nom.tam.fits.*;
import nom.tam.fits.header.DataDescription;
import nom.tam.fits.header.ObservationDurationDescription;
import nom.tam.fits.header.Standard;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static cz.cuni.mff.respefo.util.utils.MathUtils.isNotNaN;

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

            Object origin = spectrum.getOrigin();
            if (origin instanceof FitsOrigin) {
                addFitsOriginInfo(hdu, (FitsOrigin) origin);

            } else if (origin instanceof LegacySpefoOrigin) {
                addLegacySpefoOriginInfo(hdu, (LegacySpefoOrigin) origin);

            } else if (origin instanceof AsciiOrigin) {
                hdu.addValue("FILENAME", ((AsciiOrigin) origin).getFileName(), "Original file name");
            }

            if (isNotNaN(spectrum.getHjd().getJD())) {
                hdu.addValue("HJD", spectrum.getHjd().getJD(), "Heliocentric Julian Date");
            }
            if (!spectrum.getDateOfObservation().equals(LocalDateTime.MIN)) {
                hdu.addValue(Standard.DATE_OBS, spectrum.getDateOfObservation().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
            if (isNotNaN(spectrum.getRvCorrection()) && spectrum.getRvCorrection() != 0) {
                hdu.addValue("VHELIO", spectrum.getRvCorrection(), "RV Correction");
            }
            if (isNotNaN(spectrum.getExpTime())) {
                hdu.addValue(ObservationDurationDescription.EXPTIME, spectrum.getExpTime());
            }

            hdu.addValue(DataDescription.CREATOR, "reSpefo " + VersionInfo.getVersion());
            hdu.addValue(Standard.DATE, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            fits.addHDU(hdu);

            fits.write(new File(fileName));
        } catch (IOException | FitsException exception) {
            throw new SpefoException("Error while writing to file", exception);
        }
    }

    private void addFitsOriginInfo(BasicHDU<?> hdu, FitsOrigin origin) throws HeaderCardException {
        for (HeaderCard card : origin.getHeaderCards()) {
            try {
                double value = Double.parseDouble(card.getValue());
                if (Double.isFinite(value) && value == Math.rint(value)) {
                    hdu.addValue(card.getKey(), (int) value, card.getComment());
                } else {
                    hdu.addValue(card.getKey(), value, card.getComment());
                }

            } catch (NumberFormatException | NullPointerException exception) {
                hdu.addValue(card.getKey(), card.getValue(), card.getComment());
            }
        }
    }

    private void addLegacySpefoOriginInfo(BasicHDU<?> hdu, LegacySpefoOrigin origin) throws HeaderCardException {
        hdu.addValue("FILENAME", origin.getFileName(), "Original file name");
        hdu.addValue("REMARK", origin.getRemark(), "Remark");

        if (!origin.getUsedCal().trim().isEmpty()) {
            hdu.addValue("USEDCAL", origin.getUsedCal(), "Used calibration");
        }

        if (origin.getStarStep() != 0) {
            hdu.addValue("STARSTEP", origin.getStarStep(), "Star step");
        }

        hdu.addValue("DCOEF1", origin.getDispCoef(0), "First coefficient");
        hdu.addValue("DCOEF2", origin.getDispCoef(1), "Second coefficient");
        hdu.addValue("DCOEF3", origin.getDispCoef(2), "Third coefficient");
        hdu.addValue("DCOEF4", origin.getDispCoef(3), "Fourth coefficient");
        hdu.addValue("DCOEF5", origin.getDispCoef(4), "Fifth coefficient");
        hdu.addValue("DCOEF6", origin.getDispCoef(5), "Sixth coefficient");
        hdu.addValue("DCOEF7", origin.getDispCoef(6), "Seventh coefficient");

        hdu.addValue("MINTRANS", origin.getMinTransp(), "Minimum transposition");

        if (origin.getMaxInt() != 1) {
            hdu.addValue("MAXINT", origin.getMaxInt(), "Maximum value"); // Maybe DATAMAX?
        }

        if (origin.hasValidFilterWidth()) {
            for (int i = 0; i < 4; i++) {
                hdu.addValue("FILTERW" + (i + 1), origin.getFilterWidth(i), "Filter width");
            }
        }

        if (origin.getReserve() != 0) {
            hdu.addValue("RESERVE", origin.getReserve(), "Reserve");
        }
    }
}
