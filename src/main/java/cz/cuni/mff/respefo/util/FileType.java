package cz.cuni.mff.respefo.util;

public enum FileType {
    COMPATIBLE_SPECTRUM_FILES("Compatible Spectrum Files", "*.fits;*.fit;*.fts;*.txt;*.asc;*.ascii;*;*.rui;*.uui;*.rci;*.rfi"),
    SPECTRUM("Spectrum Files", "*.spf");

    private String filterNames;
    private String filterExtensions;

    FileType(String filterNames, String filterExtensions) {
        this.filterNames = filterNames;
        this.filterExtensions = filterExtensions;
    }

    public String getFilterNames() {
        return filterNames;
    }

    public String getFilterExtensions() {
        return filterExtensions;
    }
}
