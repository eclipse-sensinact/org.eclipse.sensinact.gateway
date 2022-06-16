package org.eclipse.sensinact.prototype.annotation.verb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to define a SET method for writable values
 * 
 * Can be repeated if a single method can write values for more than one
 * service/resource.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(SET.SETs.class)
public @interface SET {
	
	/**
	 * The service that this SET method applies to
	 * @return
	 */
	String service();
	
	/**
	 * The resource that this SET method applies to
	 * @return
	 */
	String resource();
	
	/**
	 * The type of the resource data. If not set then the received parameter type of the method is used. 
	 * @return
	 */
	Class<?> type() default Object.class;
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface SETs {
		SET[] value();
	}

}
