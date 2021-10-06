package cz.cuni.mff.respefo.format.formats.fits;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.format.Spectrum;
import nom.tam.fits.Header;

public class BeSSFitsFormat extends ImportFitsFormat {

    private boolean applyCorrection;

    @Override
    public Spectrum importFrom(String fileName) throws SpefoException {
        applyCorrection = false;

        Spectrum spectrum = super.importFrom(fileName);

        if (applyCorrection) {
            double rvCorr = spectrum.getRvCorrection();
            spectrum.setRvCorrection(0);
            spectrum.updateRvCorrection(rvCorr);
        }

        return spectrum;
    }

    @Override
    protected double getRVCorrection(Header header) {
        double BSS_VHEL = header.getDoubleValue("BSS_VHEL");

        if (BSS_VHEL != 0) {
            return -BSS_VHEL;
        }

        double BSS_RQHV = header.getDoubleValue("BSS_RQHV");
        applyCorrection = true;

        return -BSS_RQHV;
    }

    @Override
    public String name() {
        return "BeSS database";
    }

    @Override
    public String description() {
        return "The BeSS database FITS format.\n\n" +
                "It is very similar to the default FITS format but uses some non-standard header cards.\n\n" +
                "This format is used by the Be Star Spectra database. It assembles spectra obtained by professional and amateur astronomers of those stars.";
    }

    @Override
    public boolean isDefault() {
        return false;
    }
}
