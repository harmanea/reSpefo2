package cz.cuni.mff.respefo.format.formats.fits;

import nom.tam.fits.Header;

public class BeSSFitsFormat extends ImportFitsFormat {

    @Override
    protected double getRVCorrection(Header header) {
        return -header.getDoubleValue("BSS_VHEL");
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
