package cz.cuni.mff.respefo.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum FileType {
    COMPATIBLE_SPECTRUM_FILES("Compatible Spectrum Files",
            "fits", "fit", "fts", "txt", "asc", "ascii", "rui", "uui", "rci", "rfi"),
    SPECTRUM("Spectrum Files", "spf"),
    FITS("All FITS Files", "fits", "fit", "fts"),
    CMP("CMP Files", "cmp"),
    STL("STL Files", "stl"),
    LST("LST Files", "lst"),
    AC("AC Files", "ac"),
    ASCII_FILES("Plain Text Files", "txt", "asc", "ascii"),
    ;

    private final String[] filterNames;
    private final String[] filterExtensions;

    FileType(String filterName, String filterExtension) {
        this.filterNames = new String[]{filterName, "All Files"};
        this.filterExtensions = new String[]{"*." + filterExtension, "*.*"};
    }

    FileType(String filterName, String ... filterExtensions) {
        List<String> filterNamesList = new ArrayList<>(filterExtensions.length + 2);
        List<String> filterExtensionsList = new ArrayList<>(filterExtensions.length + 2);

        String compoundFilterExtension = Arrays.stream(filterExtensions)
                .map(filterExtension -> "*." + filterExtension)
                .collect(Collectors.joining(";"));

        filterNamesList.add(filterName);
        filterExtensionsList.add(compoundFilterExtension);

        for (String filterExtension : filterExtensions) {
            filterNamesList.add(filterExtension.toUpperCase() + " Files");
            filterExtensionsList.add("*." + filterExtension);
        }

        filterNamesList.add("All Files");
        filterExtensionsList.add("*.*");

        this.filterNames = filterNamesList.toArray(new String[0]);
        this.filterExtensions = filterExtensionsList.toArray(new String[0]);
    }

    public String[] getFilterNames() {
        return filterNames;
    }

    public String[] getFilterExtensions() {
        return filterExtensions;
    }
}
