/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.DependentCommand;
import org.eclipse.sensinact.core.command.ResourceCommand;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.IDtoMemoryCache;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.DtoToModelMapper;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.ext.Providers;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * FeatureOfInterest
 */
public class FeatureOfInterestExtraUseCase
        extends AbstractExtraUseCaseModelDelete<FeatureOfInterest, ProviderSnapshot> {
    private final IDtoMemoryCache<ExpandedObservation> cacheObs;

    @SuppressWarnings("unchecked")
    public FeatureOfInterestExtraUseCase(Providers providers, Application application) {
        super(providers, application);
        cacheObs = resolve(providers, IDtoMemoryCache.class, ExpandedObservation.class);
    }

    @Override
    public List<SensorThingsUpdate> dtosToCreateUpdate(ExtraUseCaseRequest<FeatureOfInterest> request) {
        String providerId = DtoToModelMapper.extractFirstIdSegment(request.id());

        checkRequireField(request);

        return List.of(DtoToModelMapper.toFoiUpdate(providerId, request.model(), List.of(), false));

    }

    public ExtraUseCaseResponse<ProviderSnapshot> create(ExtraUseCaseRequest<FeatureOfInterest> request) {
        String idOp = request.id();

        List<SensorThingsUpdate> listDtoModels = dtosToCreateUpdate(request);

        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            throw new InternalServerErrorException(e);
        }
        ProviderSnapshot snapshot = providerUseCase.read(request.session(), idOp);
        if (snapshot == null) {
            return new ExtraUseCaseResponse<ProviderSnapshot>(false, "can't find sensor");
        }
        return new ExtraUseCaseResponse<ProviderSnapshot>(request.id(), snapshot);

    }

    public ExtraUseCaseResponse<ProviderSnapshot> update(ExtraUseCaseRequest<FeatureOfInterest> request) {
        // check if sensor is in cached map

        String providerId = DtoMapperSimple.extractFirstIdSegment(request.id());

        List<SensorThingsUpdate> listDtoModels = dtosToCreateUpdate(request);

        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            throw new InternalServerErrorException(e);
        }
        ProviderSnapshot snapshot = providerUseCase.read(request.session(), providerId);
        if (snapshot == null) {
            return new ExtraUseCaseResponse<ProviderSnapshot>(false, "can't find sensor");
        }
        return new ExtraUseCaseResponse<ProviderSnapshot>(request.id(), snapshot);

    }

    @Override
    public AbstractSensinactCommand<?> dtoToDelete(ExtraUseCaseRequest<FeatureOfInterest> request) {
        ProviderSnapshot provider = providerUseCase.read(request.session(), request.id());
        boolean hasObs = DtoMapperSimple.getResourceField(DtoMapperSimple.getFeatureOfInterestService(provider),
                "hasObs", Boolean.class);

        if (!isHistoryMemory())
            if (hasObs)
                throw new WebApplicationException(409);

        cacheObs.removeDtoContain(request.id());
        @SuppressWarnings("unchecked")
        List<String> datastreamIds = DtoMapperSimple
                .getResourceField(DtoMapperSimple.getFeatureOfInterestService(provider), "datastreamIds", List.class);
        ResourceCommand<TimedValue<List<String>>> datastreamIdsCommand = new ResourceCommand<TimedValue<List<String>>>(
                request.id(), DtoMapperSimple.SERVICE_FOI, "datastreamIds") {

            @Override
            protected Promise<TimedValue<List<String>>> call(SensinactResource resource, PromiseFactory pf) {
                return resource.getMultiValue(String.class);
            }
        };

        return new DependentCommand<TimedValue<List<String>>, Void>(datastreamIdsCommand) {

            @Override
            protected Promise<Void> call(Promise<TimedValue<List<String>>> parentResult, SensinactDigitalTwin twin,
                    SensinactModelManager modelMgr, PromiseFactory pf) {
                try {
                    SensinactProvider sp = twin.getProvider(request.id());
                    parentResult.getValue().getValue().stream().map(id -> twin.getProvider(id)).filter(Objects::nonNull)
                            .forEach(prov -> {
                                SensinactResource resource = prov.getResource(DtoMapperSimple.SERVICE_DATASTREAM,
                                        "lastObservation");
                                resource.setValue(null);
                            });
                    if (sp != null) {
                        sp.delete();
                    }
                    return pf.resolved(null);
                } catch (Exception e) {
                    return pf.failed(e);
                }
            }

        };
    }

}
