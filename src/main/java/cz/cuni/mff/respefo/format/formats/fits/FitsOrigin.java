package cz.cuni.mff.respefo.format.formats.fits;

import cz.cuni.mff.respefo.format.origin.BaseOrigin;
import cz.cuni.mff.respefo.format.origin.Origin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

@Origin(key = "fits")
public class FitsOrigin extends BaseOrigin {

    private static final Set<String> IGNORED_HEADER_KEYS = unmodifiableSet(new HashSet<>(
            asList("", "END", "BITPIX", "NAXIS", "NAXIS1", "EXTEND", "CRPIX1", "CRVAL1", "CDELT1", "BZERO", "BSCALE", "SIMPLE")));

    private List<HeaderCard> headerCards;

    private FitsOrigin() {
        // default empty constructor
    }

    public FitsOrigin(String fileName, List<HeaderCard> headerCards) {
        super(fileName);
        this.headerCards = headerCards.stream()
                .filter(card -> !IGNORED_HEADER_KEYS.contains(card.getKey()))
                .collect(Collectors.toList());
    }

    public List<HeaderCard> getHeaderCards() {
        return headerCards;
    }
}
