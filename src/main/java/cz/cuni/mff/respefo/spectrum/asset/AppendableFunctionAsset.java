package cz.cuni.mff.respefo.spectrum.asset;

public interface AppendableFunctionAsset<T extends FunctionAsset > extends FunctionAsset {
    void append(T other);
}
