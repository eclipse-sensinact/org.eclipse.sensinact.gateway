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
package org.eclipse.sensinact.prototype.reflective;

import java.security.SecureRandom;

import org.eclipse.sensinact.core.model.ModelProvider;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.model.ValueType;
import org.osgi.service.component.annotations.Component;

@Component
public class ProgrammaticModelProvider implements ModelProvider {

    SecureRandom random = new SecureRandom();

    @Override
    public void init(SensinactModelManager manager) {
        manager.createModel("reflective").withAutoDeletion(true).build().createService("testService").build()
                .createResource("testResource").withType(Integer.class).withGetter()
                .withValueType(ValueType.UPDATABLE);
    }

    @Override
    public void destroy() {
        // Nothing to do here as the model is auto-deleted
    }
}
