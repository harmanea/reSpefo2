package cz.cuni.mff.respefo.spectrum.port.ascii;

import cz.cuni.mff.respefo.spectrum.origin.BaseOrigin;
import cz.cuni.mff.respefo.spectrum.origin.Origin;

@Origin(key = "ascii")
public class AsciiOrigin extends BaseOrigin {

    private String firstLine;

    private AsciiOrigin() {
        // default empty constructor
    }

    public AsciiOrigin(String fileName, String firstLine) {
        super(fileName);
        this.firstLine = firstLine;
    }

    public String getFirstLine() {
        return firstLine;
    }

    public void setFirstLine(String firstLine) {
        this.firstLine = firstLine;
    }
}
