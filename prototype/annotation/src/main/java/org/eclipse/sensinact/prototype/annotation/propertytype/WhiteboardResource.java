/*********************************************************************
* Copyright (c) 2022 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
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
