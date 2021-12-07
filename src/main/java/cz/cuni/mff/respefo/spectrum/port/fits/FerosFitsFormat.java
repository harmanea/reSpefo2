package cz.cuni.mff.respefo.spectrum.port.fits;

import cz.cuni.mff.respefo.logging.Log;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.header.Standard;
import nom.tam.util.Cursor;

import java.util.NoSuchElementException;

public class FerosFitsFormat extends ImportFitsFormat {
    @Override
    public double getRVCorrection(Header header) {
        Cursor<String, HeaderCard> cursor = header.iterator();
        while (cursor.hasNext()) {
            HeaderCard card = cursor.next();

            if (Standard.HISTORY.key().equals(card.getKey()) && card.getComment().contains("BARY_COR")) {
                try {
                    HeaderCard nextCard = cursor.next();
                    return Double.parseDouble(nextCard.getComment());

                } catch (NoSuchElementException | NumberFormatException | NullPointerException exception) {
                    Log.error("RV correction could not be determined from the FITS header", exception);
                    break;  // invalid format, break to immediately return NaN
                }
            }
        }

        return Double.NaN;
    }

    @Override
    public String name() {
        return "FEROS";
    }

    @Override
    public String description() {
        return "The ESO FEROS FITS format.\n\n" +
                "It is very similar to the default FITS format but uses a non-standard way of storing rv corrections.\n\n" +
                "This format is used by FEROS (the Fiber-fed Extended Range Optical Spectrograph) installed at ESO’s La Silla Observatory.";
    }

    @Override
    public boolean isDefault() {
        return false;
    }
}
