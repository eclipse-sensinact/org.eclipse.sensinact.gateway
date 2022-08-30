package org.eclipse.sensinact.prototype.annotation.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the name of the resource for a given data value
 * 
 * Either used:
 * 
 * On a String field with no value to supply the resource name.
 * 
 * <pre>
 * &#64;Resource
 * public String resource;
 * </pre>
 * 
 * or
 * 
 * On the type, or a {@link Data} field with a value containing the resource name
 * 
 * <pre>
 * &#64;Provider(&quot;exampleProvider&quot;)
 * &#64;Service(&quot;exampleService&quot;)
 * public class MyDto {
 *   &#64;Resource(&quot;exampleResource&quot;)
 *   &#64;Data
 *   public String value;
 * }
 * </pre>
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Resource {
	String value() default AnnotationConstants.NOT_SET;
}
