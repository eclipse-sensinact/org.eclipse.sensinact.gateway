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
import org.osgi.service.component.annotations.Component;

/**
 * This component provides a resource handler that can propagate a value
 * assignment and return the real one.
 *
 * The handler is called when a SET verb is applied to the resource. It can
 * return a value that will be stored in sensiNact as the value to returned when
 * calling GET.
 */
@WhiteboardResource // Adds the property to be detected by sensiNact
@ProviderName("pull-based") // Service property to define the provider that this resource is for
@Component(service = _01_SimpleWritableBasedResource.class) // The component must provide a service to be detected
public class _01_SimpleWritableBasedResource {

    /**
     * A SET method for a service and resource. Note that the resource type will be
     * the return type of the method.
     *
     * @param newValue New value to set
     * @return The new value on the device
     */
    @SET(model = "model", service = "example", resource = "default")
    public Double setValue(Double newValue) {
        // Set the value for the device
        // If supported, the method can return the new state of the device for that
        // value.
        // For example, if the device rejects values above 255, this new value on the
        // device can be returned instead of the expected one.
        return Math.max(newValue, 255d);
    }
}
