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
package org.eclipse.sensinact.prototype.pull;

import org.eclipse.sensinact.core.annotation.dto.Data;
import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.core.annotation.dto.Service;
import org.eclipse.sensinact.core.annotation.propertytype.WhiteboardResource;
import org.eclipse.sensinact.core.annotation.verb.GET;
import org.eclipse.sensinact.core.annotation.verb.GET.ReturnType;
import org.osgi.service.component.annotations.Component;

/**
 * This component provides a resource handler that return a DTO to describe its
 * value
 */
@WhiteboardResource // Adds the property to be detected by sensiNact
@Component(service = _02_DTOPullBasedResource.class) // The component must provide a service to be detected
public class _02_DTOPullBasedResource {

    /**
     * Definition of the GET handler, where we indicate that we return an annotated
     * DTO instead of a value. The DTO must therefore describe the resource and its
     * value.
     */
    @GET(ReturnType.DTO)
    public SimpleDTO getValue() {
        // Get the value from the sensor
        return null;
    }

    /**
     * Custom DTO for resources of the "dto" service of the "pull-examples" provider
     */
    @Provider("pull-example")
    @Service("dto")
    public static class SimpleDTO {

        /**
         * Resource value. The resource will be named as the field (<code>count</code>)
         */
        @Data
        public int count;
    }
}
