package org.eclipse.sensinact.prototype.annotation.propertytype;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.osgi.service.component.annotations.ComponentPropertyType;

/**
 * Used to mark a service with the sensiNact.whiteboard.resource property.
 */
@ComponentPropertyType
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface WhiteboardResource {
	
	String PREFIX_ = "sensiNact.";
}
