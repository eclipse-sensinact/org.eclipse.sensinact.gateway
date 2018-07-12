package org.eclipse.sensinact.gateway.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Property {
    public static final String INTEGER = "[0-9]+";
    public static final String FLOAT = "[0-9]+\\.[0-9]+";
    public static final String STRING = "[a-zA-Z]+";
    public static final String ALPHANUMERIC = "[a-zA-Z0-9]+";

    public String name() default "";

    public boolean mandatory() default true;

    public String defaultValue() default "";

    public String validationRegex() default "";
}
