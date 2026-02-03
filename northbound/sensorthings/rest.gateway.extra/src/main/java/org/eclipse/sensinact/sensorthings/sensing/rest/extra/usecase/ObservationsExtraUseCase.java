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

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.DependentCommand;
import org.eclipse.sensinact.core.command.ResourceCommand;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.endpoint.DependsOnUseCases;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.DtoToModelMapper;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ext.Providers;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * observation
 */
@DependsOnUseCases(value = { FeatureOfInterestExtraUseCase.class })
public class ObservationsExtraUseCase extends AbstractExtraUseCaseDtoDelete<ExpandedObservation, ServiceSnapshot> {

    private FeatureOfInterestExtraUseCase featureOfInterestUseCase;

    public ObservationsExtraUseCase(Providers providers) {
        super(providers);
        featureOfInterestUseCase = resolveUseCase(providers, FeatureOfInterestExtraUseCase.class);

    }

    public ExtraUseCaseResponse<ServiceSnapshot> create(ExtraUseCaseRequest<ExpandedObservation> request) {
        String observationId = request.id();
        List<SensorThingsUpdate> listDtoModels = dtosToCreateUpdate(request);

        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            throw new InternalServerErrorException(e);

        }

        ServiceSnapshot service = serviceUseCase.read(request.session(), getProviderId(request), "datastream");
        if (service != null) {
            removeFeatureOfInterest(request.model());
            return new ExtraUseCaseResponse<ServiceSnapshot>(observationId, service);
        }
        return new ExtraUseCaseResponse<ServiceSnapshot>(false, "fail to get Snapshot");

    }

    private void checkRequireLink(ServiceSnapshot datastream) {
        if (datastream == null) {
            throw new BadRequestException("datastream not found in Observation Payload");
        }

    }

    @Override
    public List<SensorThingsUpdate> dtosToCreateUpdate(ExtraUseCaseRequest<ExpandedObservation> request) {
        // read thing for each location and update it
        ExpandedObservation observation = request.model();
        checkRequireField(request);
        // parent can be datastream or featureOfInterest TODO

        FeatureOfInterest foi = getFeatureOfInterest(observation);
        if (foi != null) {
            checkRequireField(foi);
        } else {
            // create default foi
            foi = new FeatureOfInterest(null, DtoToModelMapper.getNewId(), "default_foi", "default Foi",
                    "application/vnd.geo+json", new Point(0, 0), null);
        }
        String id = getProviderId(request);
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        String serviceId = "datastream";
        ServiceSnapshot serviceDatastream = serviceUseCase.read(request.session(), providerId, serviceId);
        checkRequireLink(serviceDatastream);
        return List.of(DtoToModelMapper.toDatastreamUpdate(request.mapper(), providerId,
                getObservedArea(request.session(), providerId), null, null, null, null, null, null, observation, foi));
    }

    private String getProviderId(ExtraUseCaseRequest<ExpandedObservation> request) {
        String id = request.parentId() != null ? request.parentId() : (String) request.model().datastream().id();
        if (id == null) {
            id = request.id();
        }
        return id;
    }

    private void checkRequireField(FeatureOfInterest foi) {
        try {
            DtoMapperSimple.checkRequireField(foi);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private FeatureOfInterest getFeatureOfInterest(ExpandedObservation observation) {
        FeatureOfInterest foi = null;
        // retrieve created sensor
        if (observation.featureOfInterest() != null
                && DtoToModelMapper.isRecordOnlyField(observation.featureOfInterest(), "id")) {
            String idFoi = DtoToModelMapper.getIdFromRecord(observation.featureOfInterest());

            foi = featureOfInterestUseCase.getInMemoryFeatureOfInterest(idFoi);

        } else {
            foi = observation.featureOfInterest();
        }
        return foi;
    }

    private void removeFeatureOfInterest(ExpandedObservation observation) {
        // retrieve created sensor
        if (observation.featureOfInterest() != null
                && DtoToModelMapper.isRecordOnlyField(observation.featureOfInterest(), "id")) {
            String idFoi = DtoToModelMapper.getIdFromRecord(observation.featureOfInterest());

            featureOfInterestUseCase.removeInMemoryFeatureOfInterest(idFoi);
        }

    }

    public ExtraUseCaseResponse<ServiceSnapshot> update(ExtraUseCaseRequest<ExpandedObservation> request) {
        String observationId = request.id();
        List<SensorThingsUpdate> listDtoModels = dtosToCreateUpdate(request);

        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            throw new InternalServerErrorException(e);

        }
        String dataStreamId = DtoMapperSimple.extractFirstIdSegment(request.parentId());
        ServiceSnapshot service = serviceUseCase.read(request.session(), dataStreamId, "datastream");
        if (service != null) {
            removeFeatureOfInterest(request.model());
            return new ExtraUseCaseResponse<ServiceSnapshot>(observationId, service);
        }
        return new ExtraUseCaseResponse<ServiceSnapshot>(false, "fail to get Snapshot");
    }

    @Override
    public AbstractSensinactCommand<?> dtoToDelete(ExtraUseCaseRequest<ExpandedObservation> request) {
        String datastreamId = DtoMapperSimple.extractFirstIdSegment(request.id());

        ResourceCommand<TimedValue<String>> parentCommand = new ResourceCommand<TimedValue<String>>(datastreamId,
                DtoMapperSimple.SERVICE_DATASTREAM, "lastObservation") {
            @Override
            protected Promise<TimedValue<String>> call(SensinactResource resource, PromiseFactory pf) {
                return resource.getValue(String.class);

            }
        };
        return new DependentCommand<TimedValue<String>, Void>(parentCommand) {

            @Override
            protected Promise<Void> call(Promise<TimedValue<String>> parentResult, SensinactDigitalTwin twin,
                    SensinactModelManager modelMgr, PromiseFactory pf) {
                try {
                    if (parentResult.getFailure() == null) {
                        String obsStr = parentResult.getValue().getValue();
                        if (obsStr == null) {
                            return pf.failed(new NotFoundException());
                        }
                        ExpandedObservation obs = parseObservation(request, obsStr);
                        if (request.id() == null || !request.id().startsWith((String) obs.id())) {
                            return pf.failed(new BadRequestException());

                        }
                        String datastreamId = DtoMapperSimple.extractFirstIdSegment(request.id());

                        SensinactProvider sp = twin.getProvider(datastreamId);
                        SensinactResource resource = sp.getResource("datastream", "lastObservation");

                        return resource.setValue(null);
                    }
                    return pf.resolved(null);
                } catch (InvocationTargetException | InterruptedException e) {
                    return pf.failed(e);
                }

            }
        };
    }

}
