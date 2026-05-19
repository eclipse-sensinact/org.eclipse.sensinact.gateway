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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessServiceUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.endpoint.DependsOnUseCases;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.DtoToModelMapper;

import tools.jackson.databind.ObjectMapper;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Providers;

/**
 * manage create , update or delete of $ref
 */
@DependsOnUseCases({ DatastreamsExtraUseCase.class, FeatureOfInterestExtraUseCase.class, LocationsExtraUseCase.class,
        ThingsExtraUseCase.class, ObservationsExtraUseCase.class, ObservedPropertiesExtraUseCase.class,
        SensorsExtraUseCase.class })
public class RefIdUseCase extends AbstractExtraUseCase<RefId, Object> {

    private final IAccessProviderUseCase providerUseCase;
    private final IAccessServiceUseCase serviceUseCase;
    private DatastreamsExtraUseCase datastreamUseCase;

    private final LocationsExtraUseCase locationUseCase;
    private final ThingsExtraUseCase thingExtraUseCase;

    private final ObservationsExtraUseCase observationsExtraUseCase;
    private final ObservedPropertiesExtraUseCase observedPropertyExtraUseCase;
    private final SensorsExtraUseCase sensorExtraUseCase;
    private final FeatureOfInterestExtraUseCase foiExtraUseCase;
    private final GatewayThread gatewayThread;

    @FunctionalInterface
    interface RefHandler {
        ExtraUseCaseResponse<Object> handle(ExtraUseCaseRequest<RefId> request);
    }

    record RefKey(Class<? extends Id> source, Class<? extends Id> target) {
    }

    private final Map<RefKey, RefHandler> deleteHandlers = Map.of(
            new RefKey(ExpandedDataStream.class, ExpandedThing.class), this::deleteDatastreamThingRef,
            new RefKey(ExpandedLocation.class, ExpandedThing.class), this::deleteLocationThingRef,

            new RefKey(ExpandedThing.class, ExpandedLocation.class), this::deleteThingLocationRef,
            new RefKey(ExpandedDataStream.class, Sensor.class), this::deleteDatastreamSensor,
            new RefKey(ExpandedDataStream.class, ObservedProperty.class), this::deleteDatastreamObservedPropertyRef,
            new RefKey(ExpandedObservation.class, FeatureOfInterest.class), this::deleteObservationFeatureOfInterest,

            new RefKey(ExpandedDataStream.class, ExpandedObservation.class), this::deleteDatastreamObservationRef);

    private final Map<RefKey, RefHandler> updateHandlers = new HashMap<RefIdUseCase.RefKey, RefIdUseCase.RefHandler>();
    private final Map<RefKey, RefHandler> createHandlers = Map.of(
            new RefKey(ExpandedDataStream.class, ExpandedThing.class), this::updateDatastreamThingRef,
            new RefKey(ExpandedThing.class, ExpandedLocation.class), this::updateThingLocationRef);

    public RefIdUseCase(Providers providers, Application application) {
        super(providers, application);
        providerUseCase = resolve(providers, IAccessProviderUseCase.class);
        gatewayThread = resolve(providers, GatewayThread.class);
        serviceUseCase = resolve(providers, IAccessServiceUseCase.class);

        datastreamUseCase = resolveUseCase(providers, DatastreamsExtraUseCase.class);
        foiExtraUseCase = resolveUseCase(providers, FeatureOfInterestExtraUseCase.class);
        locationUseCase = resolveUseCase(providers, LocationsExtraUseCase.class);
        thingExtraUseCase = resolveUseCase(providers, ThingsExtraUseCase.class);
        observationsExtraUseCase = resolveUseCase(providers, ObservationsExtraUseCase.class);
        observedPropertyExtraUseCase = resolveUseCase(providers, ObservedPropertiesExtraUseCase.class);
        sensorExtraUseCase = resolveUseCase(providers, SensorsExtraUseCase.class);
        initUpdateHandler();
    }

    private void initUpdateHandler() {
        updateHandlers.put(new RefKey(ExpandedDataStream.class, ExpandedThing.class), this::updateDatastreamThingRef);
        updateHandlers.put(new RefKey(ExpandedObservation.class, FeatureOfInterest.class),
                request -> updateObservationFeatureOfInterestRef(request, observationsExtraUseCase,
                        "FeatureOfInterest"));
        updateHandlers.put(new RefKey(ExpandedThing.class, ExpandedLocation.class), this::updateThingLocationRef);
        updateHandlers.put(new RefKey(ExpandedDataStream.class, ObservedProperty.class),
                request -> updateDatasteamSensorOrObservedPropertyRef(request, datastreamUseCase,
                        ObservedProperty.class));
        updateHandlers.put(new RefKey(ExpandedDataStream.class, Sensor.class),
                request -> updateDatasteamSensorOrObservedPropertyRef(request, datastreamUseCase, Sensor.class));
    }

    /**
     * create the reference between 2 entities
     */
    @Override
    public ExtraUseCaseResponse<Object> create(ExtraUseCaseRequest<RefId> request) {
        RefKey key = new RefKey(request.clazzModel(), request.clazzRef());

        RefHandler handler = createHandlers.get(key);
        if (handler == null) {
            return new ExtraUseCaseResponse<>(false, "Unsupported $ref delete: " + key);
        }
        return handler.handle(request);

    }

    /**
     * delete the reference between 2 entities
     */
    @Override
    public ExtraUseCaseResponse<Object> delete(ExtraUseCaseRequest<RefId> request) {
        RefKey key = new RefKey(request.clazzModel(), request.clazzRef());

        RefHandler handler = deleteHandlers.get(key);
        if (handler == null) {
            return new ExtraUseCaseResponse<>(false, "Unsupported $ref delete: " + key);
        }
        return handler.handle(request);
    }

    private ExtraUseCaseResponse<Object> deleteThingLocationRef(ExtraUseCaseRequest<RefId> request) {
        String idThing = request.parentId();
        String idLocation = request.id(); // can be null in that cas
        // delete thing location ref
        AbstractSensinactCommand<?> command = thingExtraUseCase.deleteThingLocationsRef(idThing, idLocation);
        try {
            gatewayThread.execute(command).getValue();
        } catch (InvocationTargetException | InterruptedException e) {
            throw new InternalServerErrorException(e);
        }
        return new ExtraUseCaseResponse<Object>(true, "link deleted");

    }

    private ExtraUseCaseResponse<Object> deleteLocationThingRef(ExtraUseCaseRequest<RefId> request) {
        String idLocation = request.parentId();
        String idThing = request.id();
        return deleteLocationThingsLink(idLocation, idThing);

    }

    /**
     * delete link between location and thing, thing can be null and we remove all
     * link in that case
     *
     * @param idLocation
     * @param idThing
     * @return
     */
    private ExtraUseCaseResponse<Object> deleteLocationThingsLink(String idLocation, String idThing) {

        AbstractSensinactCommand<?> command = locationUseCase.deleteLocationThingsLink(idLocation, idThing);
        try {
            gatewayThread.execute(command).getValue();
        } catch (InvocationTargetException | InterruptedException e) {
            throw new InternalServerErrorException(e);
        }
        return new ExtraUseCaseResponse<Object>(true, "link deleted");
    }

    private ExtraUseCaseResponse<Object> deleteDatastreamObservedPropertyRef(ExtraUseCaseRequest<RefId> request) {
        String idDatastream = request.parentId();
        ServiceSnapshot datastreamService = serviceUseCase.read(request.session(), idDatastream,
                DtoMapperSimple.SERVICE_DATASTREAM);
        String idProperty = DtoMapperSimple.getResourceField(datastreamService, "observedPropertyId", String.class);

        ServiceSnapshot service = serviceUseCase.read(request.session(), idProperty,
                DtoMapperSimple.SERVICE_OBSERVED_PROPERTY);
        ObservedProperty obsProp = DtoMapperSimple.toObservedProperty(service.getProvider(), null, null);
        ExtraUseCaseResponse<ProviderSnapshot> response = observedPropertyExtraUseCase
                .delete(new ExtraUseCaseRequest<ObservedProperty>(request.session(), request.mapper(),
                        request.uriInfo(), HttpMethod.DELETE, obsProp));
        return new ExtraUseCaseResponse<Object>(response.id(), response.snapshot(), response.success(), response.e(),
                response.message());
    }

    private ExtraUseCaseResponse<Object> deleteDatastreamSensor(ExtraUseCaseRequest<RefId> request) {
        String idDatastream = request.parentId();
        ServiceSnapshot datastreamService = serviceUseCase.read(request.session(), idDatastream,
                DtoMapperSimple.SERVICE_DATASTREAM);
        String sensorId = DtoMapperSimple.getResourceField(datastreamService, "sensorId", String.class);
        ServiceSnapshot service = serviceUseCase.read(request.session(), sensorId, DtoMapperSimple.SERVICE_SENSOR);

        Sensor sensor = DtoMapperSimple.toSensor(service.getProvider(), null, null);
        ExtraUseCaseResponse<ProviderSnapshot> response = sensorExtraUseCase.delete(new ExtraUseCaseRequest<Sensor>(
                request.session(), request.mapper(), request.uriInfo(), HttpMethod.DELETE, sensor));
        return new ExtraUseCaseResponse<Object>(response.id(), response.snapshot(), response.success(), response.e(),
                response.message());
    }

    private ExtraUseCaseResponse<Object> deleteObservationFeatureOfInterest(ExtraUseCaseRequest<RefId> request) {

        String idDatastream = DtoMapperSimple.extractFirstIdSegment(request.parentId());
        ServiceSnapshot service = serviceUseCase.read(request.session(), idDatastream,
                DtoMapperSimple.SERVICE_DATASTREAM);
        ExpandedObservation obs = getExpandedObservationFromService(request, service);

        FeatureOfInterest foi = obs.featureOfInterest();
        if (foi == null) {
            throw new NotFoundException();

        }
        ExtraUseCaseResponse<ProviderSnapshot> response = foiExtraUseCase
                .delete(new ExtraUseCaseRequest<FeatureOfInterest>(request.session(), request.mapper(),
                        request.uriInfo(), HttpMethod.DELETE, foi));
        return new ExtraUseCaseResponse<Object>(response.id(), response.snapshot(), response.success(), response.e(),
                response.message());

    }

    private ExtraUseCaseResponse<Object> deleteDatastreamThingRef(ExtraUseCaseRequest<RefId> request) {
        return new ExtraUseCaseResponse<Object>(false, "not implemented");

    }

    private ExtraUseCaseResponse<Object> deleteDatastreamObservationRef(ExtraUseCaseRequest<RefId> request) {
        return new ExtraUseCaseResponse<Object>(false, "not implemented");

    }

    @Override
    public Class<RefId> getType() {
        return RefId.class;
    }

    private ExtraUseCaseResponse<Object> updateDatastreamThingRef(ExtraUseCaseRequest<RefId> request) {
        // TODO how to keep observation history
        // update reference between thing and datastream
        String idThing = (String) request.model().id();
        String idDatastream = request.parentId();
        // read datastream
        ProviderSnapshot snapshot = providerUseCase.read(request.session(), idDatastream);

        // create new datastream link to thing provider
        ExpandedDataStream newDatastream = DtoToModelMapper.toDatastreamOnly(snapshot);
        ExtraUseCaseResponse<ProviderSnapshot> response = datastreamUseCase
                .update(new ExtraUseCaseRequest<ExpandedDataStream>(request.session(), request.mapper(),
                        request.uriInfo(), HttpMethod.PATCH, newDatastream, idThing));

        return new ExtraUseCaseResponse<Object>(response.id(), response.snapshot(), response.success(), response.e(),
                response.message());

    }

    /**
     * update the reference between 2 entities
     */
    @Override
    public ExtraUseCaseResponse<Object> update(ExtraUseCaseRequest<RefId> request) {
        RefKey key = new RefKey(request.clazzModel(), request.clazzRef());

        RefHandler handler = updateHandlers.get(key);
        if (handler == null) {
            return new ExtraUseCaseResponse<>(false, "Unsupported $ref update: " + key);
        }
        return handler.handle(request);
    }

    private <T extends Id> ExtraUseCaseResponse<Object> updateDatasteamSensorOrObservedPropertyRef(
            ExtraUseCaseRequest<RefId> request, DatastreamsExtraUseCase useCase, Class<?> dtoType) {
        String id = (String) request.model().id();
        String parentId = request.parentId();

        if (id.contains("~")) {
            throw new WebApplicationException("Conflict", Response.Status.CONFLICT);
        }

        ExpandedDataStream datastream = null;
        if (dtoType.equals(Sensor.class)) {
            Sensor sensor = providerUseCase.read(request.session(), id) != null
                    ? DtoMapperSimple.toSensor(providerUseCase.read(request.session(), id), null, null)
                    : null;
            datastream = new ExpandedDataStream(null, parentId, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, sensor, null, null);
        } else if (dtoType.equals(ObservedProperty.class)) {
            ObservedProperty op = providerUseCase.read(request.session(), id) != null
                    ? DtoMapperSimple.toObservedProperty(providerUseCase.read(request.session(), id), null, null)
                    : null;
            datastream = new ExpandedDataStream(null, parentId, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, op, null, null, null);
        }
        ExtraUseCaseResponse<ProviderSnapshot> response = useCase.update(new ExtraUseCaseRequest<>(request.session(),
                request.mapper(), request.uriInfo(), HttpMethod.PATCH, datastream, null, true));
        return new ExtraUseCaseResponse<Object>(response.id(), response.snapshot(), response.success(), response.e(),
                response.message());
    }

    private ExtraUseCaseResponse<Object> updateObservationFeatureOfInterestRef(ExtraUseCaseRequest<RefId> request,
            ObservationsExtraUseCase useCase, String entityName) {
        String id = (String) request.model().id();
        String parentId = request.parentId();

        if (id.contains("~")) {
            throw new WebApplicationException("Conflict", Response.Status.CONFLICT);
        }
        ProviderSnapshot providerFoi = providerUseCase.read(request.session(), id);

        ExpandedObservation observation = new ExpandedObservation(null, parentId, null, null, null, null, null, null,
                null, null, null, null,
                DtoMapperSimple.toFeatureOfInterest(providerFoi, providerFoi.getName(), null, null), false);

        ExtraUseCaseResponse<ServiceSnapshot> response = useCase.update(new ExtraUseCaseRequest<>(request.session(),
                request.mapper(), request.uriInfo(), HttpMethod.PATCH, observation, parentId, true));
        return new ExtraUseCaseResponse<Object>(response.id(), response.snapshot(), response.success(), response.e(),
                response.message());
    }

    private ExtraUseCaseResponse<Object> updateThingLocationRef(ExtraUseCaseRequest<RefId> request) {
        // update reference between thing and datastream
        String idLocation = (String) request.model().id();
        String idThing = request.parentId();
        // check if location exists
        ProviderSnapshot providerLocation = providerUseCase.read(request.session(), idLocation);
        ProviderSnapshot providerThing = providerUseCase.read(request.session(), idThing);

        if (providerLocation == null) {
            throw new NotFoundException("Location %s not found");
        }
        if (providerThing == null) {
            throw new NotFoundException("Thing %s not found");
        }

        ExpandedLocation locationUpdate = toLocation(request.mapper(), providerLocation, idThing);
        ExtraUseCaseResponse<ProviderSnapshot> result = locationUseCase
                .update(new ExtraUseCaseRequest<ExpandedLocation>(request.session(), request.mapper(),
                        request.uriInfo(), HttpMethod.PATCH, locationUpdate, true));
        return new ExtraUseCaseResponse<Object>(result.id(), result.snapshot());
    }

    public static ExpandedLocation toLocation(ObjectMapper mapper, ProviderSnapshot provider, String thingId) {
        Location location = DtoMapperSimple.toLocation(mapper, provider, null, null, null);
        return new ExpandedLocation(null, location.id(), location.name(), location.description(),
                location.encodingType(), location.location(), null, null, null, List.of(new RefId(thingId)));
    }

}
