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
 * Service properties define the provider that this resource is for
 */
@WhiteboardResource
@ProviderName("pull_based")
@Component(service = _01_SimpleActionResource.class)
public class _01_SimpleActionResource {

    /**
     * A GET method for a service and resource
     *
     * @return
     */
    @ACT(model = "testModel", service = "example", resource = "default")
    public List<Long> doAction() {
        // Run the action and return the result
        return null;
    }
}
