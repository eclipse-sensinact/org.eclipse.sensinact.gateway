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
package org.eclipse.sensinact.core.impl;

import java.time.Instant;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.emf.command.AbstractSensinactEMFCommand;
import org.eclipse.sensinact.core.emf.model.SensinactEMFModelManager;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFDigitalTwin;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFProvider;
import org.eclipse.sensinact.core.model.Model;
import org.eclipse.sensinact.model.core.provider.Provider;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public class SaveProviderCommand extends AbstractSensinactEMFCommand<Void> {

    private Provider provider;

    public SaveProviderCommand(Provider provider) {
        this.provider = provider;
    }

    @Override
    protected Promise<Void> call(SensinactEMFDigitalTwin twin, SensinactEMFModelManager modelMgr,
            PromiseFactory promiseFactory) {

        EClass eClass = provider.eClass();
        Model model = modelMgr.getModel(eClass);
        if (model == null) {
            model = modelMgr.createModel(eClass).withCreationTime(Instant.now()).build();
        }
        SensinactEMFProvider sp = twin.getProvider(model.getPackageUri(), model.getName(), provider.getId());
        if (sp == null) {
            sp = twin.createProvider(provider);
        }

        return sp.update(provider);
    }
}
