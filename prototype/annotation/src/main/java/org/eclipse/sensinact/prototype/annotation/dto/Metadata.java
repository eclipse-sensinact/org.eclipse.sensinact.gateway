package org.eclipse.sensinact.prototype.annotation.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a metadata field in a DTO which is applied to a resource. 
 * The name of the resource is determined by the following preference order
 * 
 * <ol>
 *   <li>If present, the {@link Resource} annotation present on the field</li>
 *   <li>If present, value of the dto field annotated with {@link Resource}</li>
 *   <li>If present, the {@link Resource} annotation present on the dto type</li>
 *   <li>If none of the above are present then this is an error and no metadata is set</li>
 * </ol>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Metadata {
	/**
	 * The name of the metadata field, if not set then the dto field name is used
	 * @return
	 */
	String value() default AnnotationConstants.NOT_SET;
	/**
	 * The resource action when the data field is null
	 * @return
	 */
	NullAction onNull() default NullAction.IGNORE;
	
	/**
	 * The action to take if the annotated field is a map. The default is to treat the map as the value
	 * @return
	 */
	MapAction[] onMap() default {};
}
