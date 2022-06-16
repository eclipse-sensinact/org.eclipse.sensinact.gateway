package org.eclipse.sensinact.prototype.annotation.propertytype;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.osgi.service.component.annotations.ComponentPropertyType;

/**
 * Used to set the String+ <code>sensinact.provider.name</code> service property
 * for a whiteboard resource
 */
@ComponentPropertyType
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface ProviderName {
	
	String PREFIX_ = "sensiNact.";
	
	/**
	 * The name(s) of the provider(s) provided by the whiteboard resource
	 * @return
	 */
	String[] value();
}
