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
package org.eclipse.sensinact.prototype.resource;

import java.security.SecureRandom;

import org.eclipse.sensinact.prototype.model.SensinactModelManager;
import org.eclipse.sensinact.prototype.model.ModelProvider;
import org.osgi.service.component.annotations.Component;

@Component
public class ResourceModelProvider implements ModelProvider {

    SecureRandom random = new SecureRandom();

    @Override
    public void init(SensinactModelManager manager) {
        manager.registerModel("fan-resource.xml");
    }

    @Override
    public void destroy() {
        // Nothing to do here as the model is auto-deleted
    }
}
