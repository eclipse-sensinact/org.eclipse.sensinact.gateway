/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.core.annotation.verb;

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
     * What part of the URI should be passed ( {@link UriSegment#URI} by default)
     */
    UriSegment value() default UriSegment.URI;

    /**
     * URI segment that can be provided as whiteboard method argument
     */
    public enum UriSegment {
        /** The whole URI */
        URI,
        /** The model name */
        MODEL,
        /** The provider name */
        PROVIDER,
        /** The service name */
        SERVICE,
        /** The resource name */
        RESOURCE
    }

}
