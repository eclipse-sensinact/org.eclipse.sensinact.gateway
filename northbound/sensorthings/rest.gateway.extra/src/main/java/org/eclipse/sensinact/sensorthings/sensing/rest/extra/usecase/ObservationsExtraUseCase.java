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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilDto;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.DtoToModelMapper;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.ext.Providers;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * observation
 */
public class ObservationsExtraUseCase extends AbstractExtraUseCaseDtoDelete<ExpandedObservation, ServiceSnapshot> {

    private final FeatureOfInterestExtraUseCase featureOfInterestUseCase;

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
            return new ExtraUseCaseResponse<ServiceSnapshot>(false, new InternalServerErrorException(e),
                    e.getMessage());

        }

        ServiceSnapshot service = serviceUseCase.read(request.session(), request.parentId(), "datastream");
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
            DtoToModelMapper.checkRequireField(foi);
        }
        String id = request.parentId() != null ? request.parentId() : request.id();
        String providerId = UtilDto.extractFirstIdSegment(id);
        String serviceId = "datastream";
        checkRequireLink(serviceUseCase.read(request.session(), providerId, serviceId));

        return List.of(DtoToModelMapper.toDatastreamUpdate(providerId, null, null, null, observation, foi));
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
            return new ExtraUseCaseResponse<ServiceSnapshot>(false, new InternalServerErrorException(e),
                    e.getMessage());

        }
        String dataStreamId = UtilDto.extractFirstIdSegment(request.parentId());
        ServiceSnapshot service = serviceUseCase.read(request.session(), dataStreamId, "datastream");
        if (service != null) {
            removeFeatureOfInterest(request.model());
            return new ExtraUseCaseResponse<ServiceSnapshot>(observationId, service);
        }
        return new ExtraUseCaseResponse<ServiceSnapshot>(false, "fail to get Snapshot");
    }

    @Override
    public List<AbstractSensinactCommand<?>> dtoToDelete(ExtraUseCaseRequest<ExpandedObservation> request) {
        List<AbstractSensinactCommand<?>> list = new ArrayList<AbstractSensinactCommand<?>>();
        String datastreamId = UtilDto.extractFirstIdSegment(request.id());
        String observationId = UtilDto.extractSecondIdSegment(request.id());
        ServiceSnapshot service = serviceUseCase.read(request.session(), datastreamId, "datastream");
        ExpandedObservation obs = UtilDto.getResourceField(service, "lastObservation", ExpandedObservation.class);
        if (observationId == null || !observationId.equals(obs.id())) {
            throw new BadRequestException();
        }

        list.add(new AbstractTwinCommand<Void>() {
            @Override
            protected Promise<Void> call(SensinactDigitalTwin twin, PromiseFactory pf) {
                try {
                    SensinactProvider sp = twin.getProvider(datastreamId);
                    SensinactResource resource = sp.getResource("datastream", "lastObservation");

                    resource.setValue(null).getValue();

                    return pf.resolved(null);
                } catch (InvocationTargetException | InterruptedException e) {
                    return pf.failed(e);
                }
            }
        });
        return list;
    }

}
