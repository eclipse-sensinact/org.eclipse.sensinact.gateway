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
import org.eclipse.sensinact.core.annotation.dto.Service;
import org.eclipse.sensinact.core.annotation.propertytype.ProviderName;
import org.eclipse.sensinact.core.annotation.propertytype.WhiteboardResource;
import org.eclipse.sensinact.core.annotation.verb.GET;
import org.eclipse.sensinact.core.annotation.verb.GET.ReturnType;
import org.osgi.service.component.annotations.Component;

/**
 * This component provides a resource handler that return a DTO to describe its
 * value, but the provider is determined by the service properties
 */
@WhiteboardResource // Adds the property to be detected by sensiNact
@ProviderName("pull-based") // Service property to define the provider that this resource is for
@Component(service = _03_DTOPullBasedResource.class) // The component must provide a service to be detected
public class _03_DTOPullBasedResource {

    /**
     * Definition of the GET handler, where we indicate that we return an annotated
     * DTO instead of a value. The DTO must therefore describe the resource and its
     * value. The DTO doesn't need to define a provider as it will be found in the
     * service properties
     */
    @GET(ReturnType.DTO)
    public SimpleDTO getValue() {
        // Get the value from the sensor
        return null;
    }

    /**
     * Custom DTO for multiple resources of the "example" service
     */
    @Service("example")
    public static class SimpleDTO {

        /**
         * Resource value. The resource will be named as the field (<code>count</code>)
         */
        @Data
        public int count;

        /**
         * Resource value. The resource will be named as the field
         * (<code>average</code>)
         */
        @Data
        public double average;

    }
}
