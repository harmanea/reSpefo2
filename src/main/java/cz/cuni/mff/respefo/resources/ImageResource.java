package cz.cuni.mff.respefo.resources;

public enum ImageResource {

    FOLDER("folder.png"),
    OPENED_FOLDER("opened_folder.png"),
    FILE("file.png"),
    SPECTRUM_FILE("spectrum_file.png"),
    IMPORTABLE_FILE("importable_file.png"),
    SUPPORT_FILE("support_file.png"),

    REFRESH("sync.png"),
    MINIMIZE("minimize.png"),
    COLLAPSE("collapse.png"),

    LEFT_ARROW("left_arrow.png"),
    RIGHT_ARROW("right_arrow.png"),
    CHECK("check.png"),

    COPY("copy.png"),
    DELETE("delete.png"),
    PASTE("paste.png"),
    EDIT("edit.png"),

    FOLDER_LARGE("folder_large.png"),
    SPECTRA_LARGE("spectra_large.png"),
    TOOLS_LARGE("tools_large.png"),
    KEYBOARD_LARGE("keyboard_large.png"),
    HELP_LARGE("help_large.png"),
    INFO_LARGE("info_large.png"),
    EVENT_LOG_LARGE("event_log_large.png"),

    LINES_LARGE("lines_large.png"),
    RULER_LARGE("ruler_large.png"),

    RECTIFY("rectify.png"),
    RV("rv.png"),
    EW("ew.png"),

    FILTER("filter.png"),
    SCROLL_TO_END("scroll_to_end.png"),
    ;

    private final String fileName;

    ImageResource(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
