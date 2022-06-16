package org.eclipse.sensinact.prototype.annotation.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;


/**
 * Marks a field as containing a timestamp. The field must be convertible to a long, and will be
 * interpreted based on the epoch.
 * 
 * If the annotated field is <code>null</code> then the current system time will be used when setting the data
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Timestamp {
	/**
	 * The unit of time this represents since the epoch, 
	 * either {@link ChronoUnit#MILLIS} or {@link ChronoUnit#SECONDS}
	 * @return
	 */
	ChronoUnit value() default ChronoUnit.MILLIS;
}
