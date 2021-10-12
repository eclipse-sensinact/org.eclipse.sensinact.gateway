/*
* Copyright (c) 2021 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Bischof - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core.filtering;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.osgi.service.component.annotations.ComponentPropertyType;

/**
 * Component Property Type for the org.eclipse.sensinact.gateway.core.filtering.Filtering service property.
 * <p>
 * This annotation can be used on a Filtering to declare the value of the type service property.
 * 
 * @see "Component Property Types"
 * @author Stefan Bischof
 */
@ComponentPropertyType
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface FilteringType {
	/**
	 * Prefix for the property name. This value is prepended to each property
	 * name.
	 */
	String PREFIX_ = "sensinact.";

	String[] value();
}
