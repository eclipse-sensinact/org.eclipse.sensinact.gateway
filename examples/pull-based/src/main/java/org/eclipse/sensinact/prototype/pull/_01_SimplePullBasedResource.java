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

import java.time.temporal.ChronoUnit;

import org.eclipse.sensinact.core.annotation.propertytype.ProviderName;
import org.eclipse.sensinact.core.annotation.propertytype.WhiteboardResource;
import org.eclipse.sensinact.core.annotation.verb.GET;
import org.eclipse.sensinact.core.command.GetLevel;
import org.osgi.service.component.annotations.Component;

/**
 * This component provides a custom handler to get the current value of a
 * resource.
 *
 * Note that such handler is called if:
 * <ul>
 * <li>... the resource doesn't have a value yet</li>
 * <li>... if the value request is a strong GET (see
 * {@link GetLevel#STRONG})</li>
 * <li>... if the value request is a normal (default) GET (see
 * {@link GetLevel#NORMAL}) and if the data cache period has expired</li>
 * </ul>
 *
 * The handler is never called if the value request is a weak GET (see
 * {@link GetLevel#WEAK}).
 */
@WhiteboardResource // Adds the property to be detected by sensiNact
@ProviderName("pull-based") // Service property to define the provider that this resource is for
@Component(service = _01_SimplePullBasedResource.class) // The component must provide a service to be detected
public class _01_SimplePullBasedResource {

    /**
     * A GET method for resource "getter" of service "example".
     *
     * The annotation also indicates how long the cached value is considered valid
     * (500&nbsp;ms by default).
     */
    @GET(service = "example", resource = "getter", cacheDuration = 5, cacheDurationUnit = ChronoUnit.MINUTES)
    public Double getValue() {
        // Get the value from the sensor
        return 42.0;
    }
}
