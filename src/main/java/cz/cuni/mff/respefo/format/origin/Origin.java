package cz.cuni.mff.respefo.format.origin;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Origin {
    String key();
}
