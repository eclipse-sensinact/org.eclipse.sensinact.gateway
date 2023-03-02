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
package org.eclipse.sensinact.prototype.impl;

import java.time.Instant;

import org.eclipse.sensinact.model.core.Provider;
import org.eclipse.sensinact.prototype.command.AbstractSensinactCommand;
import org.eclipse.sensinact.prototype.model.Model;
import org.eclipse.sensinact.prototype.model.SensinactModelManager;
import org.eclipse.sensinact.prototype.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.prototype.twin.SensinactProvider;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public class SaveProviderCommand extends AbstractSensinactCommand<Void> {

    private Provider provider;

    public SaveProviderCommand(Provider provider) {
        this.provider = provider;
    }

    @Override
    protected Promise<Void> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
            PromiseFactory promiseFactory) {

        Model model = modelMgr.getModel(provider.eClass());
        if (model == null) {
            model = modelMgr.createModel(provider.eClass()).withCreationTime(Instant.now()).build();
        }
        SensinactProvider sp = twin.getProvider(model.getName(), provider.getId());
        if (sp == null) {
            sp = twin.createProvider(model.getName(), provider.getId(), Instant.now());
        }

        return sp.update(provider);
    }
}
