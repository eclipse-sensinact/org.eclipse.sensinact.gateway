/*********************************************************************
* Copyright (c) 2022 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
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