package cz.cuni.mff.respefo.spectrum.port.fits;

import cz.cuni.mff.respefo.spectrum.origin.BaseOrigin;
import cz.cuni.mff.respefo.spectrum.origin.Origin;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cz.cuni.mff.respefo.util.utils.CollectionUtils.setOf;

@Origin(key = "fits")
public class FitsOrigin extends BaseOrigin {

    public static final Set<String> IGNORED_HEADER_KEYS =
            setOf("", "END", "BITPIX", "NAXIS", "NAXIS1", "NAXIS2", "NAXIS3", "EXTEND","CRPIX1", "CRVAL1", "CDELT1", "BZERO", "BSCALE", "SIMPLE");

    private List<HeaderCard> headerCards;

    private FitsOrigin() {
        // default empty constructor
    }

    public FitsOrigin(String fileName, List<HeaderCard> headerCards) {
        super(fileName);
        this.headerCards = headerCards;
    }

    public List<HeaderCard> getFilteredHeaderCards() {
        // The filtering is moved to this method to ensure backwards compatibility
        return headerCards.stream()
                .filter(card -> !IGNORED_HEADER_KEYS.contains(card.getKey()))
                .collect(Collectors.toList());
    }
}
