package cz.cuni.mff.respefo.resources;

public enum ImageResource {

    BOOKMARK("/bookmark.png"),
    FOLDER("/folder.png"),
    OPENED_FOLDER("/opened_folder.png"),
    FILE("/file.png");

    private String fileName;

    ImageResource(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
