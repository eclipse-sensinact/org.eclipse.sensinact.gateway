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

import org.eclipse.sensinact.prototype.command.AbstractSensinactCommand;
import org.eclipse.sensinact.prototype.dto.impl.DataUpdateDto;
import org.eclipse.sensinact.prototype.model.Model;
import org.eclipse.sensinact.prototype.model.Resource;
import org.eclipse.sensinact.prototype.model.ResourceBuilder;
import org.eclipse.sensinact.prototype.model.SensinactModelManager;
import org.eclipse.sensinact.prototype.model.Service;
import org.eclipse.sensinact.prototype.model.ValueType;
import org.eclipse.sensinact.prototype.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.prototype.twin.SensinactProvider;
import org.eclipse.sensinact.prototype.twin.SensinactResource;
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
        String mod = dataUpdateDto.model == null ? dataUpdateDto.provider : dataUpdateDto.model;
        String provider = dataUpdateDto.provider;
        String svc = dataUpdateDto.service;
        String res = dataUpdateDto.resource;

        if (mod == null || provider == null || svc == null || res == null) {
            return promiseFactory
                    .failed(new NullPointerException("The provider, service and resource must be non null"));
        }

        SensinactResource resource = twin.getResource(mod, provider, svc, res);

        if (resource == null) {
            Model model = modelMgr.getModel(mod);
            if (model == null) {
                model = modelMgr.createModel(mod).withCreationTime(dataUpdateDto.timestamp).build();
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

            SensinactProvider sp = twin.getProvider(mod, provider);
            if (sp == null) {
                sp = twin.createProvider(mod, provider, dataUpdateDto.timestamp);
            }
            resource = sp.getServices().get(svc).getResources().get(res);
        }
        return resource.setValue(dataUpdateDto.data, dataUpdateDto.timestamp);
    }

}
