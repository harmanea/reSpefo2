package cz.cuni.mff.respefo.format.origin.scan;

import cz.cuni.mff.respefo.format.origin.Origin;

@Origin(key = "ascii")
public class AsciiOrigin {
    private String fileName;

    public AsciiOrigin(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
