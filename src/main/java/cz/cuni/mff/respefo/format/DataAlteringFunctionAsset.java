package cz.cuni.mff.respefo.format;

public interface DataAlteringFunctionAsset extends FunctionAsset {
    Data process(Data originalData);
}
