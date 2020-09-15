package cz.cuni.mff.respefo.format.formats.fits;

import cz.cuni.mff.respefo.format.origin.BaseOrigin;
import cz.cuni.mff.respefo.format.origin.Origin;

import java.util.List;

@Origin(key = "fits")
public class FitsOrigin extends BaseOrigin {

    private List<HeaderCard> headerCards;

    public FitsOrigin() {
        // Default empty constructor
    }

    public FitsOrigin(String fileName, List<HeaderCard> headerCards) {
        super(fileName);
        this.headerCards = headerCards;
    }

    public List<HeaderCard> getHeaderCards() {
        return headerCards;
    }

    public void setHeaderCards(List<HeaderCard> headerCards) {
        this.headerCards = headerCards;
    }
}
