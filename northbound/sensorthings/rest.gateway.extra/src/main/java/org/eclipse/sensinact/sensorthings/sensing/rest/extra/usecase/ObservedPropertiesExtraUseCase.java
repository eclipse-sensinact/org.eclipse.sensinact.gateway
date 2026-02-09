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
import java.util.Map;

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.DependentCommand;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IDtoMemoryCache;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.DtoToModelMapper;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.ext.Providers;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * observedProperty
 */
public class ObservedPropertiesExtraUseCase extends AbstractExtraUseCaseDtoDelete<ObservedProperty, Object> {

    private final IDtoMemoryCache<ObservedProperty> cacheObservedProperty;

    @SuppressWarnings("unchecked")
    public ObservedPropertiesExtraUseCase(Providers providers, Application application) {
        super(providers, application);
        cacheObservedProperty = resolve(providers, IDtoMemoryCache.class, ObservedProperty.class);

    }

    private ObservedProperty updateInMemoryObservedProperty(ExtraUseCaseRequest<ObservedProperty> request,
            ObservedProperty property) {
        ObservedProperty updateProp = request.model();
        String selfLink = getLink(request.uriInfo(), DtoMapperSimple.VERSION, "/ObservedProperties({id})",
                request.id());
        String datastreamLink = getLink(request.uriInfo(), selfLink, "Datastreams");
        String observedPropertyLink = getLink(request.uriInfo(), DtoMapperSimple.VERSION, "/ObservedProperties({id})",
                request.id());
        ObservedProperty createdProp = new ObservedProperty(observedPropertyLink, request.id(),
                updateProp.name() != null ? updateProp.name() : property.name(),
                updateProp.description() != null ? updateProp.description() : property.description(),
                updateProp.definition() != null ? updateProp.definition() : property.definition(),
                updateProp.properties() != null ? updateProp.properties() : property.properties(), datastreamLink);
        cacheObservedProperty.addDto(request.id(), createdProp);
        return createdProp;
    }

    public ExtraUseCaseResponse<Object> create(ExtraUseCaseRequest<ObservedProperty> request) {
        ObservedProperty observedProperty = request.model();
        checkRequireField(request);
        String observedPropertyId = request.id();
        String selfLink = getLink(request.uriInfo(), DtoMapperSimple.VERSION, "/ObservedProperties({id})",
                observedPropertyId);
        String datastreamLink = getLink(request.uriInfo(), selfLink, "Datastreams");

        ObservedProperty createExpandedProperty = new ObservedProperty(selfLink, observedPropertyId,
                observedProperty.name(), observedProperty.description(), observedProperty.definition(),
                observedProperty.properties(), datastreamLink);
        cacheObservedProperty.addDto(observedPropertyId, createExpandedProperty);

        return new ExtraUseCaseResponse<Object>(observedPropertyId, createExpandedProperty);

    }

    @Override
    public ExtraUseCaseResponse<Object> delete(ExtraUseCaseRequest<ObservedProperty> request) {
        if (isIdFromCache(request.id())) {
            if (cacheObservedProperty.getDto(request.id()) != null) {
                cacheObservedProperty.removeDto(request.id());
                return new ExtraUseCaseResponse<Object>(true, "observed property deleted");
            }
            throw new NotFoundException();

        } else {
            return super.delete(request);
        }
    }

    @Override
    public List<SensorThingsUpdate> dtosToCreateUpdate(ExtraUseCaseRequest<ObservedProperty> request) {
        String providerId = DtoToModelMapper.extractFirstIdSegment(request.id());
        String sensorId = DtoToModelMapper.extractSecondIdSegment(request.id());
        if (providerId == null || sensorId == null) {
            throw new BadRequestException("bad id format");
        }
        ObservedProperty receivedOp = request.model();
        checkRequireField(request);
        ObservedProperty opToUpdate = new ObservedProperty(null, sensorId, receivedOp.name(), receivedOp.description(),
                receivedOp.definition(), receivedOp.properties(), null);
        return List.of(DtoToModelMapper.toDatastreamUpdate(request.mapper(), providerId,
                getObservedArea(request.session(), providerId), null, null, null, opToUpdate, null, null, null));

    }

    public ExtraUseCaseResponse<Object> update(ExtraUseCaseRequest<ObservedProperty> request) {
        // check if sensor is in cached map
        ObservedProperty property = cacheObservedProperty.getDto(request.id());
        if (property != null) {
            ObservedProperty createdProperty = updateInMemoryObservedProperty(request, property);
            return new ExtraUseCaseResponse<Object>(request.id(), createdProperty);
        } else {
            String providerId = DtoMapperSimple.extractFirstIdSegment(request.id());

            List<SensorThingsUpdate> listDtoModels = dtosToCreateUpdate(request);

            // update/create provider
            try {
                dataUpdate.pushUpdate(listDtoModels).getValue();

            } catch (InvocationTargetException | InterruptedException e) {
                throw new InternalServerErrorException(e);
            }
            ServiceSnapshot serviceSnapshot = serviceUseCase.read(request.session(), providerId, "datastream");
            if (serviceSnapshot == null) {
                return new ExtraUseCaseResponse<Object>(false, "can't find sensor");
            }
            return new ExtraUseCaseResponse<Object>(request.id(), serviceSnapshot);

        }

    }

    public ObservedProperty getInMemoryObservedProperty(String id) {
        return cacheObservedProperty.getDto(id);
    }

    @Override
    public AbstractSensinactCommand<?> dtoToDelete(ExtraUseCaseRequest<ObservedProperty> request) {
        AbstractSensinactCommand<Map<String, TimedValue<?>>> parentCommand = getContextDeleteDatastreamProvider(
                request);

        return new DependentCommand<Map<String, TimedValue<?>>, Void>(parentCommand) {

            @Override
            protected Promise<Void> call(Promise<Map<String, TimedValue<?>>> parentResult, SensinactDigitalTwin twin,
                    SensinactModelManager modelMgr, PromiseFactory pf) {
                try {
                    String providerId = DtoMapperSimple.extractFirstIdSegment(request.id());
                    SensinactProvider sp = twin.getProvider(providerId);
                    String sensorId = (String) parentResult.getValue().get("sensorId").getValue();
                    String id = (String) parentResult.getValue().get("id").getValue();

                    if (sp != null) {
                        // check if there are still observed property and sensor
                        if (hasNoDatastreamAndSensor(sensorId, id)) {
                            sp.delete();
                        } else if (id == null) {

                            return removeObservedProperty(twin, providerId);
                        }
                    }

                    return pf.resolved(null);
                } catch (InvocationTargetException | InterruptedException e) {
                    return pf.failed(e);
                }
            }

            private Promise<Void> removeObservedProperty(SensinactDigitalTwin twin, String providerId) {
                SensinactResource resource = twin.getResource(providerId, DtoMapperSimple.SERVICE_DATASTREAM,
                        "observedPropertyId");
                return resource.setValue(null);
            }

            private boolean hasNoDatastreamAndSensor(String sensorId, String id) {
                return sensorId == null && id == null;
            }

        };

    }

    public void removeInMemoryObservedProperty(String id) {
        cacheObservedProperty.removeDto(id);
    }

}
