package cz.cuni.mff.respefo.resources;

public enum ImageResource {

    FOLDER("test2/folder.png"),
    OPENED_FOLDER("test2/opened_folder.png"),
    FILE("test2/file.png"),
    SPECTRUM_FILE("test2/spectrum_file.png"),
    IMPORTABLE_FILE("test2/importable_file.png"),
    SUPPORT_FILE("test2/support_file.png"),
    FOLDER_LARGE("folder_large.png"),
    WRENCH_LARGE("wrench_large.png"),
    SCROLL_LARGE("scroll_large.png");

    private String fileName;

    ImageResource(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
