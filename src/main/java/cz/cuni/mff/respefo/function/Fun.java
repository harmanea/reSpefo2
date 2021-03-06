package cz.cuni.mff.respefo.function;

import cz.cuni.mff.respefo.function.filter.AllAcceptingFileFilter;

import java.io.FileFilter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Fun {
    String name();
    Class<? extends FileFilter> fileFilter() default AllAcceptingFileFilter.class;
    String group() default "";
}
