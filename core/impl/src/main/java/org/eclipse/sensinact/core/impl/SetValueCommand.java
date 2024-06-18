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
import org.eclipse.sensinact.core.annotation.dto.NullAction;
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GetLevel;
import org.eclipse.sensinact.core.dto.impl.DataUpdateDto;
import org.eclipse.sensinact.core.emf.model.EMFModel;
import org.eclipse.sensinact.core.emf.model.EMFService;
import org.eclipse.sensinact.core.emf.model.SensinactEMFModelManager;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFDigitalTwin;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFProvider;
import org.eclipse.sensinact.core.model.Resource;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.model.ValueType;
import org.eclipse.sensinact.core.push.DataUpdateException;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
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
        return doCall((SensinactEMFDigitalTwin) twin, (SensinactEMFModelManager) modelMgr, promiseFactory)
                .recoverWith(p -> {
                    return promiseFactory.failed(new DataUpdateException(dataUpdateDto.modelPackageUri,
                            dataUpdateDto.model, dataUpdateDto.provider, dataUpdateDto.service, dataUpdateDto.resource,
                            dataUpdateDto.originalDto, p.getFailure()));
                });
    }

    private Promise<Void> doCall(SensinactEMFDigitalTwin twin, SensinactEMFModelManager modelMgr,
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
            EMFModel model = null;
            if (modelEClass != null) {
                model = modelMgr.getModel(modelEClass);
            } else {
                model = (EMFModel) modelMgr.getModel(packageUri, mod);
                if (model == null) {
                    model = modelMgr.createModel(packageUri, mod).withCreationTime(dataUpdateDto.timestamp).build();
                }
                modelEClass = model.getModelEClass();
            }
            EMFService service = model.getServices().get(svc);
            if (service == null) {
                if (ProviderPackage.Literals.DYNAMIC_PROVIDER.isSuperTypeOf(model.getModelEClass())) {
                    service = model.createDynamicService(svc, svcEClass);
                } else if (!model.isFrozen()) {
                    service = model.createService(svc).withCreationTime(dataUpdateDto.timestamp).build();
                }
            }

            Resource r = service.getResources().get(res);
            if (!model.isFrozen() && r == null) {
                r = service.createResource(res).withValueType(ValueType.UPDATABLE)
                        .withType((Class<?>) dataUpdateDto.type).build();
            }
            if (svcEClass == null) {
                svcEClass = service.getServiceEClass();
            }

            SensinactEMFProvider sp = twin.getProvider(packageUri, mod, provider);
            if (sp == null) {
                sp = twin.createProvider(packageUri, mod, provider, dataUpdateDto.timestamp);
            }
            resource = sp.getOrCreateService(svc, svcEClass).getResources().get(res);
        }

        if(dataUpdateDto.actionOnNull == NullAction.UPDATE_IF_PRESENT) {
            // This must be a weak get so that it returns immediately with a resolved value
            Promise<TimedValue<Object>> p = resource.getValue(Object.class, GetLevel.WEAK).timeout(0);
            try {
                Throwable t = p.getFailure();
                if(t != null) {
                    return promiseFactory.failed(t);
                } else {
                    TimedValue<Object> value = p.getValue();
                    if(value == null || value.getTimestamp() == null) {
                        return promiseFactory.resolved(null);
                    }
                }
            } catch (Exception e) {
                return promiseFactory.failed(e);
            }
        }

        return resource.setValue(dataUpdateDto.data, dataUpdateDto.timestamp);
    }

}
