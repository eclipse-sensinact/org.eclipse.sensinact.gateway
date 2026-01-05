package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedSensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessServiceUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IDtoMemoryCache;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.DtoToModelMapper;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.ServiceSnapshotMapper;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Providers;

public class RefIdUseCase extends AbstractExtraUseCase<RefId, Object> {

    private final IAccessProviderUseCase providerUseCase;
    private final IAccessServiceUseCase serviceUseCase;
    private final DatastreamsExtraUseCase datastreamUseCase;
    private final ThingsExtraUseCase thingExtraUseCase;

    private final FeatureOfInterestExtraUseCase foiUseCase;
    private final IDtoMemoryCache<ExpandedSensor> sensorCaches;
    private final IDtoMemoryCache<FeatureOfInterest> foiCaches;
    private final IDtoMemoryCache<ExpandedObservedProperty> observedPropertyCaches;
    private final SensorsExtraUseCase sensorUseCase;
    private final ObservedPropertiesExtraUseCase observedPropertyUseCase;
    private final LocationsExtraUseCase locationUsecase;
    private final DataUpdate dataUpdate;

    @FunctionalInterface
    interface RefHandler {
        ExtraUseCaseResponse<Object> handle(ExtraUseCaseRequest<RefId> request);
    }

    record RefKey(Class<? extends Id> source, Class<? extends Id> target) {
    }

    private final Map<RefKey, RefHandler> deleteHandlers = Map.of(
            new RefKey(ExpandedDataStream.class, ExpandedThing.class), this::deleteDatastreamThingRef,

            new RefKey(ExpandedObservation.class, FeatureOfInterest.class),
            this::deleteObservationFeratureOfInterestRef,

            new RefKey(ExpandedThing.class, ExpandedLocation.class), this::deleteThingLocationRef,

            new RefKey(ExpandedDataStream.class, ExpandedObservedProperty.class),
            this::deleteDatastreamObservedPropertyRef,

            new RefKey(ExpandedDataStream.class, ExpandedSensor.class), this::deleteDatastreamSensorRef,
            new RefKey(ExpandedDataStream.class, ExpandedObservation.class), this::deleteDatastreamObservationRef);

    private final Map<RefKey, RefHandler> updateHandlers = new HashMap<RefIdUseCase.RefKey, RefIdUseCase.RefHandler>();

    private final Map<RefKey, RefHandler> createHandlers = Map
            .of(new RefKey(ExpandedDataStream.class, ExpandedObservation.class), this::createDatastreamObservationRef);

    @SuppressWarnings("unchecked")
    public RefIdUseCase(Providers providers) {
        providerUseCase = resolve(providers, IAccessProviderUseCase.class);
        datastreamUseCase = resolveUseCase(providers, DatastreamsExtraUseCase.class);
        serviceUseCase = resolve(providers, IAccessServiceUseCase.class);
        dataUpdate = resolve(providers, DataUpdate.class);
        foiUseCase = resolve(providers, FeatureOfInterestExtraUseCase.class);
        sensorCaches = resolve(providers, IDtoMemoryCache.class, ExpandedSensor.class);
        foiCaches = resolve(providers, IDtoMemoryCache.class, FeatureOfInterest.class);
        thingExtraUseCase = resolve(providers, ThingsExtraUseCase.class);
        observedPropertyCaches = resolve(providers, IDtoMemoryCache.class, ExpandedObservedProperty.class);
        sensorUseCase = resolve(providers, SensorsExtraUseCase.class);
        observedPropertyUseCase = resolve(providers, ObservedPropertiesExtraUseCase.class);
        locationUsecase = resolve(providers, LocationsExtraUseCase.class);

        initUpdateHandler();
    }

    private void initUpdateHandler() {
        updateHandlers.put(new RefKey(ExpandedDataStream.class, ExpandedThing.class), this::updateDatastreamThingRef);
        updateHandlers.put(new RefKey(ExpandedObservation.class, FeatureOfInterest.class),
                request -> updateSingleRef(request, foiCaches, foiUseCase, "FeatureOfInterest"));
        updateHandlers.put(new RefKey(ExpandedThing.class, ExpandedLocation.class), this::updateThingLocationRef);
        updateHandlers.put(new RefKey(ExpandedDataStream.class, ExpandedObservedProperty.class),
                request -> updateSingleRef(request, observedPropertyCaches, observedPropertyUseCase,
                        "ObservedProperty"));
        updateHandlers.put(new RefKey(ExpandedDataStream.class, ExpandedSensor.class),
                request -> updateSingleRef(request, sensorCaches, sensorUseCase, "Sensor"));
    }

    /**
     * create the reference between 2 entities
     */
    @Override
    public ExtraUseCaseResponse<Object> create(ExtraUseCaseRequest<RefId> request) {
        RefKey key = new RefKey(request.clazzModel(), request.clazzRef());

        RefHandler handler = createHandlers.get(key);
        if (handler == null) {
            return new ExtraUseCaseResponse<>(false, "Unsupported $ref create: " + key);
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

    private ExtraUseCaseResponse<Object> deleteDatastreamSensorRef(ExtraUseCaseRequest<RefId> request) {
        return new ExtraUseCaseResponse<Object>(false, "not implemented");

    }

    private ExtraUseCaseResponse<Object> deleteDatastreamObservedPropertyRef(ExtraUseCaseRequest<RefId> request) {
        return new ExtraUseCaseResponse<Object>(false, "not implemented");

    }

    private ExtraUseCaseResponse<Object> deleteThingLocationRef(ExtraUseCaseRequest<RefId> request) {
        return new ExtraUseCaseResponse<Object>(false, "not implemented");

    }

    private ExtraUseCaseResponse<Object> deleteObservationFeratureOfInterestRef(ExtraUseCaseRequest<RefId> request) {
        return new ExtraUseCaseResponse<Object>(false, "not implemented");

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

    private ExtraUseCaseResponse<Object> createDatastreamObservationRef(ExtraUseCaseRequest<RefId> request) {
        return new ExtraUseCaseResponse<Object>(false, "not implemented");

    }

    private ExtraUseCaseResponse<Object> updateDatastreamThingRef(ExtraUseCaseRequest<RefId> request) {
        // TODO how to keep observation history
        // update reference between thing and datastream
        String idDatastream = (String) request.model().id();
        String idThing = request.parentId();
        // read datastream
        ServiceSnapshot service = serviceUseCase.read(request.session(), idDatastream);

        // create new datastream link to thing provider
        ExpandedDataStream newDatastream = ServiceSnapshotMapper.toDatastream(service);
        String idNewDatastream = DtoToModelMapper.sanitizeId(newDatastream.id());
        List<SensorThingsUpdate> listUpdate = datastreamUseCase
                .dtosToCreateUpdate(new ExtraUseCaseRequest<ExpandedDataStream>(request.session(), request.mapper(),
                        request.uriInfo(), request.method(), newDatastream, idThing));

        try {
            dataUpdate.pushUpdate(listUpdate).getValue();
        } catch (Exception e) {
            return new ExtraUseCaseResponse<Object>(false, new InternalServerErrorException(e), e.getMessage());
        }
        ServiceSnapshot newService = serviceUseCase.read(request.session(), idThing, idNewDatastream);

        return new ExtraUseCaseResponse<Object>(idNewDatastream, newService);

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

    @SuppressWarnings("unchecked")
    private <T extends Id> ExtraUseCaseResponse<Object> updateSingleRef(ExtraUseCaseRequest<RefId> request,
            IDtoMemoryCache<T> cache, AbstractExtraUseCase<T, ?> useCase, String entityName) {
        String id = (String) request.model().id();
        String parentId = request.parentId();

        if (id.contains("~")) {
            throw new WebApplicationException("Conflict", Response.Status.CONFLICT);
        }

        T dto = cache.getDto(id);
        if (dto == null) {
            throw new NotFoundException(entityName + " " + id + " not found");
        }

        return (ExtraUseCaseResponse<Object>) useCase.update(new ExtraUseCaseRequest<>(request.session(),
                request.mapper(), request.uriInfo(), request.method(), dto, parentId));
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
        ExpandedLocation locationUpdate = ServiceSnapshotMapper.toLocation(providerLocation.getService("locations"),
                idThing);
        ExtraUseCaseResponse<ServiceSnapshot> result = locationUsecase.update(new ExtraUseCaseRequest<ExpandedLocation>(
                request.session(), request.mapper(), request.uriInfo(), request.method(), locationUpdate));
        return new ExtraUseCaseResponse<Object>(result.id(), result.snapshot());
    }

}
