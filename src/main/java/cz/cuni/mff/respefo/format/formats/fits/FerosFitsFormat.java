package cz.cuni.mff.respefo.format.formats.fits;

import nom.tam.fits.Header;

public class FerosFitsFormat extends ImportFitsFormat {

    @Override
    protected double getRVCorrection(Header header) {
        return -header.getDoubleValue("BSS_VHEL"); // TODO: investigate further, maybe use BSS_RQVH instead?
    }

    @Override
    public String name() {
        return "ESO Feros";
    }

    @Override
    public String description() {
        return "The ESO Feros FITS format.\n\n" +
                "It is very similar to the default FITS format but uses some non-standard header cards.\n\n" +
                "This format is used by the La Silla Observatory to store data obtained using FEROS, the fiber-fed extended range optical spectrograph.";
    }

    @Override
    public boolean isDefault() {
        return false;
    }
}
