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
package org.eclipse.sensinact.prototype.action;

import java.time.Duration;
import java.time.Instant;

import org.eclipse.sensinact.core.annotation.propertytype.ProviderName;
import org.eclipse.sensinact.core.annotation.propertytype.WhiteboardResource;
import org.eclipse.sensinact.core.annotation.verb.ACT;
import org.osgi.service.component.annotations.Component;

/**
 * This sample component handles the ACT verb of the "delta" resource of the
 * "example" service of the "actionResource" provider. The sample handler
 * accepts caller arguments.
 */
@WhiteboardResource // Adds the property to be detected by sensiNact
@ProviderName("actionResource") // Name of the provider the resource is provided by
@Component(service = _04_ParameterActionResource.class) // The component must provide a service to be detected
public class _04_ParameterActionResource {

    /**
     * An ACT method for service "example" and resource "delta". This action takes
     * two arguments from the caller and returns a value.
     *
     * @param fromTime A parameter given by the caller
     * @param toTime   A parameter given by the caller
     */
    @ACT(model = "testModel", service = "example", resource = "delta")
    public Long doAction(Instant fromTime, Instant toTime) {
        // Run the action and return the result
        return Duration.between(fromTime, toTime).getSeconds();
    }
}
