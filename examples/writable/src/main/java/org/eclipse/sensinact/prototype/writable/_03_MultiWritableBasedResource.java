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
package org.eclipse.sensinact.prototype.writable;

import org.eclipse.sensinact.core.annotation.propertytype.ProviderName;
import org.eclipse.sensinact.core.annotation.propertytype.WhiteboardResource;
import org.eclipse.sensinact.core.annotation.verb.SET;
import org.eclipse.sensinact.core.annotation.verb.UriParam;
import org.eclipse.sensinact.core.annotation.verb.UriParam.UriSegment;
import org.osgi.service.component.annotations.Component;

/**
 * This component provides a shared handler for multiple resources.
 *
 * The handler is called when a SET verb is applied to the resource. It can
 * return a value that will be stored in sensiNact as the value to returned when
 * calling GET.
 */
@WhiteboardResource // Adds the property to be detected by sensiNact
@ProviderName({ "foo", "bar", "foobar" }) // Names of the providers those resources are provided by
@Component(service = _03_MultiWritableBasedResource.class) // The component must provide a service to be detected
public class _03_MultiWritableBasedResource {

    /**
     * A SET handler for multiple resourecs. Here, the resource type is explicitly
     * given for each resource: the method can return void if no correction is
     * expected, or have the return type Object and return a value with the same
     * type as the handled resource one. The handler takes a single argument from
     * the caller (the new value), but accepts different URI parameters (see
     * {@link UriParam}) from sensiNact.
     *
     * @param provider Provider name
     * @param service  Service name
     * @param resource Resource name
     * @param value    New value to set
     */
    @SET(model = "model", service = "example", resource = "fizz", type = String.class)
    @SET(model = "model", service = "example", resource = "buzz", type = Long.class)
    @SET(model = "model", service = "example2", resource = "fizzbuzz", type = Double.class)
    public void setValue(@UriParam(UriSegment.PROVIDER) String provider, @UriParam(UriSegment.SERVICE) String service,
            @UriParam(UriSegment.RESOURCE) String resource, Object value) {
        // Update the sensor
    }
}
