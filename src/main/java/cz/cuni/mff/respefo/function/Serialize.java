package cz.cuni.mff.respefo.function;

import cz.cuni.mff.respefo.format.asset.FunctionAsset;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Serialize {
    String key();
    Class<? extends FunctionAsset> assetClass();
}
