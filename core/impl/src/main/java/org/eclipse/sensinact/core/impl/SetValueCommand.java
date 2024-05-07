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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.dto.impl.DataUpdateDto;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFDigitalTwin;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFProvider;
import org.eclipse.sensinact.core.model.Model;
import org.eclipse.sensinact.core.model.Resource;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.model.Service;
import org.eclipse.sensinact.core.model.ValueType;
import org.eclipse.sensinact.core.push.DataUpdateException;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public class SetValueCommand extends AbstractSensinactCommand<Void> {

    private final DataUpdateDto dataUpdateDto;

    public SetValueCommand(DataUpdateDto dataUpdateDto) {
        this.dataUpdateDto = dataUpdateDto;
    }

    @Override
    protected Promise<Void> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
            PromiseFactory promiseFactory) {
        return doCall((SensinactEMFDigitalTwin) twin, modelMgr, promiseFactory).recoverWith(p -> {
            return promiseFactory.failed(
                    new DataUpdateException(dataUpdateDto.modelPackageUri, dataUpdateDto.model, dataUpdateDto.provider,
                            dataUpdateDto.service, dataUpdateDto.resource, dataUpdateDto.originalDto, p.getFailure()));
        });
    }

    private Promise<Void> doCall(SensinactEMFDigitalTwin twin, SensinactModelManager modelMgr,
            PromiseFactory promiseFactory) {
        EClass modelEClass = dataUpdateDto.modelEClass;
        String packageUri = modelEClass == null ? dataUpdateDto.modelPackageUri : modelEClass.getEPackage().getNsURI();
        String mod = modelEClass != null ? modelEClass.getName()
                : (dataUpdateDto.model == null ? dataUpdateDto.provider : dataUpdateDto.model);
        String provider = dataUpdateDto.provider;
        EReference svcReference = dataUpdateDto.serviceReference;
        EClass svcEClass = svcReference != null ? svcReference.getEReferenceType() : dataUpdateDto.serviceEClass;
        String svc = svcReference != null ? svcReference.getName() : dataUpdateDto.service;
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
            if (!model.isFrozen()) {
                Service service = model.getServices().get(svc);
                if (service == null) {
                    service = model.createService(svc).withCreationTime(dataUpdateDto.timestamp).build();
                }
                Resource r = service.getResources().get(res);

                if (r == null) {
                    r = service.createResource(res).withValueType(ValueType.UPDATABLE)
                            .withType((Class<?>) dataUpdateDto.type).build();
                }
            }

            SensinactEMFProvider sp = twin.getProvider(packageUri, mod, provider);
            if (sp == null) {
                sp = twin.createProvider(packageUri, mod, provider, dataUpdateDto.timestamp);
            }
            resource = sp.getOrCreateService(svc, svcEClass).getResources().get(res);
        }
        return resource.setValue(dataUpdateDto.data, dataUpdateDto.timestamp);
    }

}
