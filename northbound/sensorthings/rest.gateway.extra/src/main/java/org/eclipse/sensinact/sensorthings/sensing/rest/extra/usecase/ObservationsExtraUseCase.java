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
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilIds;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessServiceUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.DtoToModelMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.ext.Providers;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * observation
 */
public class ObservationsExtraUseCase extends AbstractExtraUseCaseDto<ExpandedObservation, ServiceSnapshot> {

    private final IAccessServiceUseCase serviceUseCase;

    private final DataUpdate dataUpdate;

    private final FeatureOfInterestExtraUseCase featureOfInterestUseCase;

    public ObservationsExtraUseCase(Providers providers) {
        dataUpdate = resolve(providers, DataUpdate.class);
        serviceUseCase = resolve(providers, IAccessServiceUseCase.class);
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

    public ExtraUseCaseResponse<ServiceSnapshot> delete(ExtraUseCaseRequest<ExpandedObservation> request) {
        return new ExtraUseCaseResponse<ServiceSnapshot>(false, "not implemented");

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
        String providerId = UtilIds.extractFirstIdSegment(id);
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
        String dataStreamId = UtilIds.extractFirstIdSegment(request.parentId());
        ServiceSnapshot service = serviceUseCase.read(request.session(), dataStreamId, "datastream");
        if (service != null) {
            removeFeatureOfInterest(request.model());
            return new ExtraUseCaseResponse<ServiceSnapshot>(observationId, service);
        }
        return new ExtraUseCaseResponse<ServiceSnapshot>(false, "fail to get Snapshot");
    }

    @Override
    public List<AbstractSensinactCommand<?>> dtoToDelete(ExtraUseCaseRequest<ExpandedObservation> request) {
        // TODO Auto-generated method stub
        return null;
    }

}
