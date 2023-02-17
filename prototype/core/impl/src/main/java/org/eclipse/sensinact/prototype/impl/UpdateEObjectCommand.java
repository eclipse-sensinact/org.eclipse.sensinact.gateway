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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.sensinact.prototype.command.AbstractSensinactCommand;
import org.eclipse.sensinact.prototype.dto.impl.EObjectUpdateDto;
import org.eclipse.sensinact.prototype.model.SensinactModelManager;
import org.eclipse.sensinact.prototype.model.nexus.impl.emf.EMFUtil;
import org.eclipse.sensinact.prototype.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.prototype.twin.SensinactObject;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public class UpdateEObjectCommand extends AbstractSensinactCommand<Void> {

    private final EObjectUpdateDto dataUpdateDto;

    public UpdateEObjectCommand(EObjectUpdateDto dataUpdateDto) {
        this.dataUpdateDto = dataUpdateDto;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Promise<Void> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
            PromiseFactory promiseFactory) {

        EObject eObject = dataUpdateDto.eObject;

        Instant timestamp = EMFUtil.getTimestamp(eObject);

        SensinactObject object = twin.getSensinactObject(eObject.eClass(), EMFUtil.getID(eObject));

        return object.update(eObject, timestamp);

//        String mod = dataUpdateDto.model == null ? dataUpdateDto.provider : dataUpdateDto.model;
//        String provider = dataUpdateDto.provider;
//        String svc = dataUpdateDto.service;
//        String res = dataUpdateDto.resource;
//
//        if (mod == null || provider == null || svc == null || res == null) {
//            return promiseFactory
//                    .failed(new NullPointerException("The provider, service and resource must be non null"));
//        }
//
//        SensinactResource resource = twin.getResource(mod, provider, svc, res);
//
//        if (resource == null) {
//            Model model = modelMgr.getModel(mod);
//            if (model == null) {
//                model = modelMgr.createModel(mod).withCreationTime(dataUpdateDto.timestamp).build();
//            }
//            Service service = model.getServices().get(svc);
//            if (service == null) {
//                service = model.createService(svc).withCreationTime(dataUpdateDto.timestamp).build();
//            }
//            Resource r = service.getResources().get(res);
//
//            if (r == null) {
//                r = service.createResource(res).withValueType(ValueType.UPDATABLE)
//                        .withType((Class<Object>) dataUpdateDto.type).build();
//            }
//
//            SensinactProvider sp = twin.getProvider(mod, provider);
//            if (sp == null) {
//                sp = twin.createProvider(mod, provider, dataUpdateDto.timestamp);
//            }
//            resource = sp.getServices().get(svc).getResources().get(res);
//        }
//        return resource.setValue(dataUpdateDto.data, dataUpdateDto.timestamp);
    }

}
