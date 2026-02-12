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

import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessResourceUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessServiceUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IDtoMemoryCache;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.DtoToModelMapper;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

/**
 * abstract use case
 *
 * @param <M>
 * @param <S>
 */
public abstract class AbstractExtraUseCaseDto<M extends Id, S> extends AbstractExtraUseCase<M, S> {

    public abstract List<SensorThingsUpdate> dtosToCreateUpdate(ExtraUseCaseRequest<M> request);

    protected final DataUpdate dataUpdate;
    protected final IAccessProviderUseCase providerUseCase;
    protected final IAccessServiceUseCase serviceUseCase;
    protected final IAccessResourceUseCase resourceUseCase;

    protected final GatewayThread gatewayThread;

    protected boolean isIdFromCache(String id) {
        return !id.contains("~");
    }

    protected static void checkRequireField(Id ds) {
        try {
            DtoMapperSimple.checkRequireField(ds);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    protected void updateObservationMemoryHistory(IDtoMemoryCache<ExpandedObservation> cacheObs,
            IDtoMemoryCache<FeatureOfInterest> cacheFoi, ObjectMapper mapper, ResourceSnapshot resource) {
        if (resource != null) {
            ExpandedObservation oldObs = DtoMapperSimple.parseExpandObservation(mapper, resource.getValue().getValue());
            cacheObs.addDto(oldObs.id() + "~" + DtoMapperSimple.stampToId(resource.getValue().getTimestamp()), oldObs);
            cacheFoi.addDto(
                    oldObs.id() + "~" + oldObs.featureOfInterest().id() + "~"
                            + DtoMapperSimple.stampToId(resource.getValue().getTimestamp()),
                    oldObs.featureOfInterest());
        }
    }

    protected ResourceSnapshot getObservationForMemoryHistory(SensiNactSession session, String providerId) {
        ResourceSnapshot resource = null;
        if (isHistoryMemory()) {
            resource = resourceUseCase.read(session, providerId, DtoMapperSimple.SERVICE_DATASTREAM, "lastObservation");
        }
        if (resource == null || !resource.isSet())
            return null;
        return resource;
    }

    protected ResourceSnapshot getProviderThingIfLocationFieldExists(SensiNactSession session, String thingId) {
        ResourceSnapshot locationThing = null;
        if (isHistoryMemory()) {
            // get if exists last historical
            locationThing = resourceUseCase.read(session, thingId, DtoMapperSimple.SERVICE_ADMIN, "location");
        }
        if (!locationThing.isSet())
            return null;
        return locationThing;
    }

    protected static void checkRequireLink(Object... obs) {
        try {
            DtoMapperSimple.checkRequireLink(obs);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    public ObservedProperty getObservedProperty(ExtraUseCaseRequest<?> request, String id) {
        String idProvider = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = providerUseCase.read(request.session(), idProvider);
        return DtoMapperSimple.toObservedProperty(provider, id, null, null);
    }

    protected AbstractSensinactCommand<Map<String, TimedValue<?>>> getContextDeleteDatastreamProvider(
            ExtraUseCaseRequest<?> request) {
        AbstractSensinactCommand<Map<String, TimedValue<?>>> parentCommand = new AbstractSensinactCommand<Map<String, TimedValue<?>>>() {

            @Override
            protected Promise<Map<String, TimedValue<?>>> call(SensinactDigitalTwin twin,
                    SensinactModelManager modelMgr, PromiseFactory pf) {
                String providerId = DtoMapperSimple.extractFirstIdSegment(request.id());
                SensinactProvider sp = twin.getProvider(providerId);

                if (sp == null) {
                    return pf.failed(new NotFoundException("provider for datastream " + request.id() + " not found"));
                }

                // Make sure all promises are Promise<TimedValue<?>>
                @SuppressWarnings("unchecked")
                Promise<TimedValue<?>> id = (Promise<TimedValue<?>>) (Promise<?>) sp
                        .getResource(DtoMapperSimple.SERVICE_DATASTREAM, "id").getValue(String.class);

                @SuppressWarnings("unchecked")
                Promise<TimedValue<?>> observedPropertyId = (Promise<TimedValue<?>>) (Promise<?>) sp
                        .getResource(DtoMapperSimple.SERVICE_DATASTREAM, "observedPropertyId").getValue(String.class);

                @SuppressWarnings("unchecked")
                Promise<TimedValue<?>> sensorId = (Promise<TimedValue<?>>) (Promise<?>) sp
                        .getResource(DtoMapperSimple.SERVICE_DATASTREAM, "sensorId").getValue(String.class);
                @SuppressWarnings("unchecked")
                Promise<TimedValue<?>> lastObservation = (Promise<TimedValue<?>>) (Promise<?>) sp
                        .getResource(DtoMapperSimple.SERVICE_DATASTREAM, "lastObservation").getValue(String.class);

                // Combine all promises in a concrete list
                List<Promise<TimedValue<?>>> promises = List.of(id, observedPropertyId, sensorId, lastObservation);

                // pf.all now works
                return pf.all(promises).map(list -> Map.of("id", list.get(0), "observedPropertyId", list.get(1),
                        "sensorId", list.get(2), "lastObservation", list.get(3)));

            }
        };
        return parentCommand;
    }

    /**
     * get observed property if we create datastream using link observedpropertyh id
     * or observed property inline or null
     *
     * @param datastream
     * @return
     */
    public ObservedProperty getCachedExpandedObservedProperty(ExtraUseCaseRequest<?> request,
            IDtoMemoryCache<ObservedProperty> observedPropertyCache, ExpandedDataStream datastream) {
        ObservedProperty observedProperty = null;
        // retrieve create observedPorperty
        if (datastream.observedProperty() != null) {
            if (DtoToModelMapper.isRecordOnlyField(datastream.observedProperty(), "id")) {
                String idObservedProperty = DtoToModelMapper.getIdFromRecord(datastream.observedProperty());
                if (idObservedProperty == null) {
                    throw new BadRequestException(String.format("observedProperty id is null"));
                }
                if (isIdFromCache(idObservedProperty)) {
                    observedProperty = observedPropertyCache.getDto(idObservedProperty);
                } else {
                    observedProperty = getObservedProperty(request, idObservedProperty);
                }
                if (observedProperty == null) {
                    throw new BadRequestException(
                            String.format("observedProperty id %s doesn't exists", idObservedProperty));
                }
            } else {
                observedProperty = datastream.observedProperty();
                checkRequireField(observedProperty);
            }
        }
        return observedProperty;
    }

    public Sensor getSensor(ExtraUseCaseRequest<?> request, String id) {
        String idProvider = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = providerUseCase.read(request.session(), idProvider);
        return DtoMapperSimple.toSensor(provider, id, null, null);
    }

    /**
     * get cache sensor if we create datastream using link sensor id or directly
     * sensor inline or null
     *
     * @param datastream
     * @return
     */
    protected Sensor getCachedExpandedSensor(ExtraUseCaseRequest<?> request, IDtoMemoryCache<Sensor> sensorCache,
            ExpandedDataStream datastream) {
        Sensor sensor = null;
        // retrieve created sensor
        if (datastream.sensor() != null) {
            if (DtoToModelMapper.isRecordOnlyField(datastream.sensor(), "id")) {
                String idSensor = DtoToModelMapper.getIdFromRecord(datastream.sensor());
                if (idSensor == null) {
                    throw new BadRequestException(String.format("sensor id is null"));
                }
                if (isIdFromCache(idSensor)) {
                    sensor = sensorCache.getDto(idSensor);
                } else {
                    sensor = getSensor(request, idSensor);
                }
                if (sensor == null) {
                    throw new BadRequestException(String.format("sensor id %s doesn't exists", idSensor));
                }
            } else {
                sensor = datastream.sensor();
                checkRequireField(sensor);

            }
        }
        return sensor;
    }

    public static String getLink(UriInfo uriInfo, String baseUri, String path) {
        String sensorLink = uriInfo.getBaseUriBuilder().uri(baseUri).path(path).build().toString();
        return sensorLink;
    }

    public static String getLink(UriInfo uriInfo, String baseUri, String path, String id) {
        if (id == null) {
            id = "null";
        }
        String link = uriInfo.getBaseUriBuilder().uri(baseUri).path(path).resolveTemplate("id", id).build().toString();
        return link;
    }

    protected void checkRequireField(ExtraUseCaseRequest<M> request) {
        try {
            if (HttpMethod.POST.equals(request.method()) || HttpMethod.PUT.equals(request.method())) {
                DtoMapperSimple.checkRequireField(request.model());
            }
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    protected GeoJsonObject getObservedArea(SensiNactSession session, String datastreamId) {
        ProviderSnapshot providerDatastream = providerUseCase.read(session, datastreamId);
        GeoJsonObject observedArea = null;

        if (providerDatastream != null) {
            observedArea = DtoMapperSimple.getResourceField(DtoMapperSimple.getAdminService(providerDatastream),
                    DtoMapperSimple.LOCATION, GeoJsonObject.class);
        }
        return observedArea;
    }

    public AbstractExtraUseCaseDto(Providers providers, Application application) {
        super(providers, application);
        dataUpdate = resolve(providers, DataUpdate.class);
        providerUseCase = resolve(providers, IAccessProviderUseCase.class);
        serviceUseCase = resolve(providers, IAccessServiceUseCase.class);
        resourceUseCase = resolve(providers, IAccessResourceUseCase.class);

        gatewayThread = resolve(providers, GatewayThread.class);
    }

    @SuppressWarnings("unchecked")
    protected List<String> getDatastreamIds(ServiceSnapshot serviceThing) {
        return DtoMapperSimple.getResourceField(serviceThing, "datastreamIds", List.class);

    }

    protected List<String> getLocationIds(ProviderSnapshot provider) {
        return getLocationIds(DtoMapperSimple.getThingService(provider));

    }

    protected List<String> getDatastreamIds(ProviderSnapshot provider) {
        return getDatastreamIds(DtoMapperSimple.getThingService(provider));
    }

    @SuppressWarnings("unchecked")
    protected List<String> getLocationIds(ServiceSnapshot serviceThing) {
        return DtoMapperSimple.getResourceField(serviceThing, "locationIds", List.class);

    }
}
