package cz.cuni.mff.respefo.resources;

public enum ImageResource {

    FOLDER("folder.png"),
    OPENED_FOLDER("opened_folder.png"),
    FILE("file.png"),
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
