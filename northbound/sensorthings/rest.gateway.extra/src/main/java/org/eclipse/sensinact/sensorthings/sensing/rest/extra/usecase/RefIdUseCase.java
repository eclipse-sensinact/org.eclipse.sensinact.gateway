package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.util.HashMap;
import java.util.Map;

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
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilIds;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessServiceUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IDtoMemoryCache;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.ServiceSnapshotMapper;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Providers;

public class RefIdUseCase extends AbstractExtraUseCase<RefId, Object> {

    private final IAccessProviderUseCase providerUseCase;
    private final IAccessServiceUseCase serviceUseCase;
    private final DatastreamsExtraUseCase datastreamUseCase;

    private final IDtoMemoryCache<ExpandedSensor> sensorCaches;
    private final IDtoMemoryCache<FeatureOfInterest> foiCaches;
    private final IDtoMemoryCache<ExpandedObservedProperty> observedPropertyCaches;
    private final LocationsExtraUseCase locationUsecase;
    private final ObservationsExtraUseCase observationsExtraUseCase;

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
    private final Map<RefKey, RefHandler> createHandlers = Map.of(
            new RefKey(ExpandedDataStream.class, ExpandedThing.class), this::updateDatastreamThingRef,
            new RefKey(ExpandedThing.class, ExpandedLocation.class), this::updateThingLocationRef);

    @SuppressWarnings("unchecked")
    public RefIdUseCase(Providers providers) {
        providerUseCase = resolve(providers, IAccessProviderUseCase.class);
        datastreamUseCase = resolveUseCase(providers, DatastreamsExtraUseCase.class);
        serviceUseCase = resolve(providers, IAccessServiceUseCase.class);
        sensorCaches = resolve(providers, IDtoMemoryCache.class, ExpandedSensor.class);
        foiCaches = resolve(providers, IDtoMemoryCache.class, FeatureOfInterest.class);
        observedPropertyCaches = resolve(providers, IDtoMemoryCache.class, ExpandedObservedProperty.class);
        locationUsecase = resolveUseCase(providers, LocationsExtraUseCase.class);
        observationsExtraUseCase = resolveUseCase(providers, ObservationsExtraUseCase.class);
        initUpdateHandler();
    }

    private void initUpdateHandler() {
        updateHandlers.put(new RefKey(ExpandedDataStream.class, ExpandedThing.class), this::updateDatastreamThingRef);
        updateHandlers.put(new RefKey(ExpandedObservation.class, FeatureOfInterest.class),
                request -> updateObservationFeatureOfInterestRef(request, foiCaches, observationsExtraUseCase,
                        "FeatureOfInterest"));
        updateHandlers.put(new RefKey(ExpandedThing.class, ExpandedLocation.class), this::updateThingLocationRef);
        updateHandlers.put(new RefKey(ExpandedDataStream.class, ExpandedObservedProperty.class),
                request -> updateDatasteamSensorOrObservedPropertyRef(request, observedPropertyCaches,
                        datastreamUseCase, "ObservedProperty"));
        updateHandlers.put(new RefKey(ExpandedDataStream.class, ExpandedSensor.class),
                request -> updateDatasteamSensorOrObservedPropertyRef(request, sensorCaches, datastreamUseCase,
                        "Sensor"));
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

    private ExtraUseCaseResponse<Object> updateDatastreamThingRef(ExtraUseCaseRequest<RefId> request) {
        // TODO how to keep observation history
        // update reference between thing and datastream
        String idThing = (String) request.model().id();
        String idDatastream = request.parentId();
        // read datastream
        ServiceSnapshot service = serviceUseCase.read(request.session(), idDatastream, "datastream");

        // create new datastream link to thing provider
        ExpandedDataStream newDatastream = ServiceSnapshotMapper.toDatastream(service);
        ExtraUseCaseResponse<ServiceSnapshot> response = datastreamUseCase
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
            ExtraUseCaseRequest<RefId> request, IDtoMemoryCache<T> cache, DatastreamsExtraUseCase useCase,
            String entityName) {
        String id = (String) request.model().id();
        String parentId = request.parentId();

        if (id.contains("~")) {
            throw new WebApplicationException("Conflict", Response.Status.CONFLICT);
        }

        T dto = cache.getDto(id);
        if (dto == null) {
            throw new NotFoundException(entityName + " " + id + " not found");
        }
        ExpandedDataStream datastream = null;
        if (dto instanceof ExpandedSensor) {
            datastream = new ExpandedDataStream(null, parentId, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, (ExpandedSensor) dto, null, null);
        } else if (dto instanceof ExpandedObservedProperty) {
            datastream = new ExpandedDataStream(null, parentId, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, (ExpandedObservedProperty) dto, null, null, null);
        }
        ExtraUseCaseResponse<ServiceSnapshot> response = useCase.update(new ExtraUseCaseRequest<>(request.session(),
                request.mapper(), request.uriInfo(), HttpMethod.PATCH, datastream, null));
        return new ExtraUseCaseResponse<Object>(response.id(), response.snapshot(), response.success(), response.e(),
                response.message());
    }

    private ExtraUseCaseResponse<Object> updateObservationFeatureOfInterestRef(ExtraUseCaseRequest<RefId> request,
            IDtoMemoryCache<FeatureOfInterest> cache, ObservationsExtraUseCase useCase, String entityName) {
        String id = (String) request.model().id();
        String parentId = request.parentId();

        if (id.contains("~")) {
            throw new WebApplicationException("Conflict", Response.Status.CONFLICT);
        }

        FeatureOfInterest dto = cache.getDto(id);
        if (dto == null) {
            throw new NotFoundException(entityName + " " + id + " not found");
        }
        ExpandedObservation observation = new ExpandedObservation(null, parentId, null, null, null, null, null, null,
                null, null, null, null, dto);

        ExtraUseCaseResponse<ServiceSnapshot> response = useCase.update(new ExtraUseCaseRequest<>(request.session(),
                request.mapper(), request.uriInfo(), HttpMethod.PATCH, observation, parentId));
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
        ExpandedLocation locationUpdate = ServiceSnapshotMapper
                .toLocation(providerLocation.getService(UtilIds.SERVICE_LOCATON), idThing);
        ExtraUseCaseResponse<ServiceSnapshot> result = locationUsecase.update(new ExtraUseCaseRequest<ExpandedLocation>(
                request.session(), request.mapper(), request.uriInfo(), HttpMethod.PATCH, locationUpdate));
        return new ExtraUseCaseResponse<Object>(result.id(), result.snapshot());
    }

}
