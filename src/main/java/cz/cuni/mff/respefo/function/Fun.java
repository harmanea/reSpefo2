package cz.cuni.mff.respefo.function;

import cz.cuni.mff.respefo.function.filter.AllAcceptingFileFilter;

import java.io.FileFilter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Fun {
    String name();
    Class<? extends FileFilter> fileFilter() default AllAcceptingFileFilter.class;
    String group() default "";
}
