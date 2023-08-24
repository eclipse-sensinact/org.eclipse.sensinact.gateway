/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

import java.util.List;

import org.eclipse.sensinact.core.annotation.propertytype.ProviderName;
import org.eclipse.sensinact.core.annotation.propertytype.WhiteboardResource;
import org.eclipse.sensinact.core.annotation.verb.ACT;
import org.osgi.service.component.annotations.Component;

/**
 * This sample component handles the ACT verb of the "action" resource of the
 * "example" service of the "actionResource" provider
 */
@WhiteboardResource // Adds the property to be detected by sensiNact
@ProviderName("actionResource") // Service property to define the provider that this resource is for
@Component(service = _01_SimpleActionResource.class) // The component must provide a service to be detected
public class _01_SimpleActionResource {

    /**
     * An ACT method for service "example" and resource "action". This action takes
     * no argument and returns a list of long integers.
     */
    @ACT(model = "testModel", service = "example", resource = "action")
    public List<Long> doAction() {
        // Run the action and return the result
        return List.of(42L);
    }
}
