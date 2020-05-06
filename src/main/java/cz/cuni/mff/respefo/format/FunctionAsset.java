package cz.cuni.mff.respefo.format;

public interface FunctionAsset {
    default Data process(Data data) {
        return data;
    }
}
