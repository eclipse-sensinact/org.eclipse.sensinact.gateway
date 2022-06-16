package org.eclipse.sensinact.prototype.annotation.verb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A parameter annotation used to indicate that a sensiNact URI, or URI segment,
 * should be passed to the resource method
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface UriParam {

	/**
	 * What part of the URI should be passed
	 * @return
	 */
	UriSegment value () default UriSegment.URI;
	
	public enum UriSegment {
		/** The whole URI */
		URI,
		/** The provider name */
		PROVIDER,
		/** The service name */
		SERVICE,
		/** The resource name */
		RESOURCE
	}
	
}