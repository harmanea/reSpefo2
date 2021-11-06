package cz.cuni.mff.respefo.spectrum.origin;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Origin {
    String key();
}
