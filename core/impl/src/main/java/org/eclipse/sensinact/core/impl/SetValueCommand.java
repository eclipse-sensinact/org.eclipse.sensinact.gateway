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

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.model.Model;
import org.eclipse.sensinact.core.model.Resource;
import org.eclipse.sensinact.core.model.ResourceBuilder;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.model.Service;
import org.eclipse.sensinact.core.model.ValueType;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.dto.impl.DataUpdateDto;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public class SetValueCommand extends AbstractSensinactCommand<Void> {

    private final DataUpdateDto dataUpdateDto;

    public SetValueCommand(DataUpdateDto dataUpdateDto) {
        this.dataUpdateDto = dataUpdateDto;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Promise<Void> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
            PromiseFactory promiseFactory) {
        String packageUri = dataUpdateDto.modelPackageUri;
        String mod = dataUpdateDto.model == null ? dataUpdateDto.provider : dataUpdateDto.model;
        String provider = dataUpdateDto.provider;
        String svc = dataUpdateDto.service;
        String res = dataUpdateDto.resource;

        if (mod == null || provider == null || svc == null || res == null) {
            return promiseFactory
                    .failed(new NullPointerException("The provider, service and resource must be non null"));
        }

        SensinactResource resource = twin.getResource(packageUri, mod, provider, svc, res);

        if (resource == null) {
            Model model = modelMgr.getModel(packageUri, mod);
            if (model == null) {
                model = modelMgr.createModel(packageUri, mod).withCreationTime(dataUpdateDto.timestamp).build();
            }
            Service service = model.getServices().get(svc);
            if (service == null) {
                service = model.createService(svc).withCreationTime(dataUpdateDto.timestamp).build();
            }
            Resource r = service.getResources().get(res);

            if (r == null) {
                r = service.createResource(res).withValueType(ValueType.UPDATABLE)
                        .withType((Class<Object>) dataUpdateDto.type).build();
            }

            SensinactProvider sp = twin.getProvider(packageUri, mod, provider);
            if (sp == null) {
                sp = twin.createProvider(packageUri, mod, provider, dataUpdateDto.timestamp);
            }
            resource = sp.getServices().get(svc).getResources().get(res);
        }
        return resource.setValue(dataUpdateDto.data, dataUpdateDto.timestamp);
    }

}
