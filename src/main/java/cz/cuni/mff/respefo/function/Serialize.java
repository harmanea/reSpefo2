package cz.cuni.mff.respefo.function;

import cz.cuni.mff.respefo.spectrum.asset.FunctionAsset;

import java.lang.annotation.*;

@Repeatable(Serialized.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Serialize {
    String key();
    Class<? extends FunctionAsset> assetClass();
}
