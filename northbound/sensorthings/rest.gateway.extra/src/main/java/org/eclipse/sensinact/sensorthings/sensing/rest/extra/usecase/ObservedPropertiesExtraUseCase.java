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
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilIds;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessServiceUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IDtoMemoryCache;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.DtoToModelMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.ext.Providers;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * observedProperty
 */
public class ObservedPropertiesExtraUseCase extends AbstractExtraUseCaseDto<ExpandedObservedProperty, Object> {

    private final IDtoMemoryCache<ExpandedObservedProperty> cacheObservedProperty;

    private final DataUpdate dataUpdate;
    private final IAccessServiceUseCase serviceUseCase;

    @SuppressWarnings("unchecked")
    public ObservedPropertiesExtraUseCase(Providers providers) {
        cacheObservedProperty = resolve(providers, IDtoMemoryCache.class, ExpandedObservedProperty.class);
        dataUpdate = resolve(providers, DataUpdate.class);
        serviceUseCase = resolve(providers, IAccessServiceUseCase.class);
    }

    private ExpandedObservedProperty updateInMemoryObservedProperty(
            ExtraUseCaseRequest<ExpandedObservedProperty> request, ExpandedObservedProperty property) {
        ExpandedObservedProperty updateProp = request.model();
        ExpandedObservedProperty createdProp = new ExpandedObservedProperty(null, request.id(),
                updateProp.name() != null ? updateProp.name() : property.name(),
                updateProp.description() != null ? updateProp.description() : property.description(),
                updateProp.definition() != null ? property.definition() : property.definition(),
                updateProp.properties() != null ? updateProp.properties() : property.properties(), null);
        cacheObservedProperty.addDto(request.id(), createdProp);
        return createdProp;
    }

    public ExtraUseCaseResponse<Object> create(ExtraUseCaseRequest<ExpandedObservedProperty> request) {
        ExpandedObservedProperty observedProperty = request.model();
        checkRequireField(request);
        String observedPropertyId = getId(request);
        ExpandedObservedProperty createExpandedProperty = new ExpandedObservedProperty(null, observedPropertyId,
                observedProperty.name(), observedProperty.description(), observedProperty.definition(),
                observedProperty.properties(), null);
        cacheObservedProperty.addDto(observedPropertyId, createExpandedProperty);

        return new ExtraUseCaseResponse<Object>(observedPropertyId, createExpandedProperty);

    }

    @Override
    public String getId(ExtraUseCaseRequest<ExpandedObservedProperty> request) {
        return request.id() != null ? request.id()
                : DtoToModelMapper
                        .sanitizeId(request.model().id() != null ? request.model().id() : request.model().name());
    }

    public ExtraUseCaseResponse<Object> delete(ExtraUseCaseRequest<ExpandedObservedProperty> request) {
        return new ExtraUseCaseResponse<Object>(false, "not implemented");

    }

    @Override
    public List<SensorThingsUpdate> dtosToCreateUpdate(ExtraUseCaseRequest<ExpandedObservedProperty> request) {
        String providerId = DtoToModelMapper.extractFirstIdSegment(request.id());
        String datastreamId = providerId;
        String sensorId = DtoToModelMapper.extractSecondIdSegment(request.id());
        if (providerId == null || datastreamId == null || sensorId == null) {
            throw new BadRequestException("bad id format");
        }
        ExpandedObservedProperty receivedOp = request.model();
        checkRequireField(request);
        ExpandedObservedProperty opToUpdate = new ExpandedObservedProperty(null, sensorId, receivedOp.name(),
                receivedOp.description(), receivedOp.definition(), receivedOp.properties(), null);
        return List
                .of(DtoToModelMapper.toDatastreamUpdate(providerId, datastreamId, null, opToUpdate, null, null, null));

    }

    public ExtraUseCaseResponse<Object> update(ExtraUseCaseRequest<ExpandedObservedProperty> request) {
        // check if sensor is in cached map
        ExpandedObservedProperty property = cacheObservedProperty.getDto(request.id());
        if (property != null) {
            ExpandedObservedProperty createdProperty = updateInMemoryObservedProperty(request, property);
            return new ExtraUseCaseResponse<Object>(request.id(), createdProperty);
        } else {
            String providerId = UtilIds.extractFirstIdSegment(request.id());

            List<SensorThingsUpdate> listDtoModels = dtosToCreateUpdate(request);

            // update/create provider
            try {
                dataUpdate.pushUpdate(listDtoModels).getValue();

            } catch (InvocationTargetException | InterruptedException e) {
                return new ExtraUseCaseResponse<Object>(false, new InternalServerErrorException(e), e.getMessage());
            }
            ServiceSnapshot serviceSnapshot = serviceUseCase.read(request.session(), providerId, "datastream");
            if (serviceSnapshot == null) {
                return new ExtraUseCaseResponse<Object>(false, "can't find sensor");
            }
            return new ExtraUseCaseResponse<Object>(request.id(), serviceSnapshot);

        }

    }

    public ExpandedObservedProperty getInMemoryObservedProperty(String id) {
        return cacheObservedProperty.getDto(id);
    }

    public void removeInMemoryObservedProperty(String id) {
        cacheObservedProperty.removeDto(id);
    }

    @Override
    public List<AbstractSensinactCommand<?>> dtoToDelete(ExtraUseCaseRequest<ExpandedObservedProperty> request) {
        // TODO Auto-generated method stub
        return null;
    }

}
