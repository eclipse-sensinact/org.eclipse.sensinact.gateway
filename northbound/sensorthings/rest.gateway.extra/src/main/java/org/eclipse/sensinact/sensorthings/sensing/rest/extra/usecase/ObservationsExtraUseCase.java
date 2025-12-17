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

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessResourceUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessServiceUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * observation
 */
@Component(service = IExtraUseCase.class)
public class ObservationsExtraUseCase extends AbstractExtraUseCase<ExpandedObservation, ServiceSnapshot> {

    @Reference
    IAccessResourceUseCase resourceUseCase;

    @Reference
    IAccessServiceUseCase serviceUseCase;

    @Reference
    DataUpdate dataUpdate;

    @Reference
    IFeatureOfInterestExtraUseCase featureOfInterestUseCase;

    public ExtraUseCaseResponse<ServiceSnapshot> create(ExtraUseCaseRequest<ExpandedObservation> request) {
        String observationId = getId(request.model());
        List<SensorThingsUpdate> listDtoModels = toDtos(request);
        String fullDatastreamId = request.parentId();
        String thingId = DtoToModelMapper.extractFirstIdSegment(fullDatastreamId);
        String datastreamId = fullDatastreamId.substring(thingId.length() + 1);

        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            return new ExtraUseCaseResponse<ServiceSnapshot>(false, new InternalServerErrorException(e),
                    e.getMessage());

        }

        ServiceSnapshot service = serviceUseCase.read(request.session(), thingId, datastreamId);
        if (service != null) {
            removeFeatureOfInterest(request.model());
            return new ExtraUseCaseResponse<ServiceSnapshot>(observationId, service);
        }
        return new ExtraUseCaseResponse<ServiceSnapshot>(false, "fail to get Snapshot");

    }

    public ExtraUseCaseResponse<ServiceSnapshot> delete(ExtraUseCaseRequest<ExpandedObservation> request) {
        return new ExtraUseCaseResponse<ServiceSnapshot>(false, "fail to get providerSnapshot");

    }

    public ExtraUseCaseResponse<ServiceSnapshot> patch(ExtraUseCaseRequest<ExpandedObservation> request) {
        return new ExtraUseCaseResponse<ServiceSnapshot>(false, "fail to get providerSnapshot");

    }

    private void checkRequireLink(ServiceSnapshot datastream) {
        if (datastream == null) {
            throw new BadRequestException("datastream not found in Observation Payload");
        }

    }

    @Override
    protected List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<ExpandedObservation> request) {
        // read thing for each location and update it
        ExpandedObservation observation = request.model();
        DtoToModelMapper.checkRequireField(observation);
        // parent can be datastream or featureOfInterest TODO
        String idFullDatastream = request.parentId();
        String providerId = DtoToModelMapper.extractFirstIdSegment(idFullDatastream);
        String idDatastream = idFullDatastream.substring(providerId.length() + 1);
        if (idDatastream == null) {
            throw new BadRequestException("can't find datastream parent ");
        }
        FeatureOfInterest foi = getFeatureOfInterest(observation);
        if (foi != null) {
            DtoToModelMapper.checkRequireField(foi);

        }
        checkRequireLink(serviceUseCase.read(request.session(), providerId, idDatastream));

        return List
                .of(DtoToModelMapper.toDatastreamUpdate(providerId, idDatastream, null, null, null, observation, foi));
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
        return new ExtraUseCaseResponse<ServiceSnapshot>(false, "fail to get providerSnapshot");

    }

    @Override
    public String getId(ExpandedObservation dto) {
        return DtoToModelMapper.sanitizeId(dto.id() != null ? dto.id() : dto.result());
    }

}
