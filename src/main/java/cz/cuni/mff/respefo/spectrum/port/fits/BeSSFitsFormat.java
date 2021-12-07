package cz.cuni.mff.respefo.spectrum.port.fits;

import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.spectrum.Spectrum;
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
    public double getRVCorrection(Header header) {
        double vhel = header.getDoubleValue("BSS_VHEL");
        if (vhel != 0.0) {
            return vhel;
        }

        double rqvh = header.getDoubleValue("BSS_RQVH");
        if (rqvh != 0.0) {
            applyCorrection = true;
            return -rqvh;
        }

        return Double.NaN;
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
