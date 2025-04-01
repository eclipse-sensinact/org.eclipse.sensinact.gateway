/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.notification.ResourceNotification;
import org.eclipse.sensinact.core.notification.LifecycleNotification;
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.notification.ResourceMetaDataNotification;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.mappers.DatastreamMapper;
import org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.mappers.DatastreamsMapper;
import org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.mappers.FeatureOfInterestMapper;
import org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.mappers.FeaturesOfInterestMapper;
import org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.mappers.HistoricalLocationMapper;
import org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.mappers.HistoricalLocationsMapper;
import org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.mappers.LocationMapper;
import org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.mappers.LocationsMapper;
import org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.mappers.ObservationMapper;
import org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.mappers.ObservationsMapper;
import org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.mappers.ObservedPropertiesMapper;
import org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.mappers.ObservedPropertyMapper;
import org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.mappers.SensorMapper;
import org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.mappers.SensorsMapper;
import org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.mappers.ThingMapper;
import org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.mappers.ThingsMapper;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.NameDescription;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class SensorthingsMapper<T> {

    private static final Logger LOG = LoggerFactory.getLogger(SensorthingsMapper.class);

    private static final Set<String> COLLECTIONS = Set.of("Things", "Locations", "HistoricalLocations", "Datastreams",
            "Sensors", "Observations", "ObservedProperties", "FeaturesOfInterest");

    private static final Pattern ENTITY_PATH = Pattern.compile("([\\w~]+)\\(([\\w~]+)\\)");

    private final String topicFilter;

    /**
     * SensiNact gateway thread
     */
    private final GatewayThread thread;

    protected final ObjectMapper jsonMapper;

    protected SensorthingsMapper(String topicFilter, ObjectMapper mapper, GatewayThread gateway) {
        this.topicFilter = topicFilter;
        this.jsonMapper = mapper;
        this.thread = gateway;
    }

    public Promise<Stream<T>> toPayload(ResourceNotification notification) {
        if (notification instanceof ResourceDataNotification) {
            return toPayload((ResourceDataNotification) notification).filter(Objects::nonNull);
        } else if (notification instanceof LifecycleNotification) {
            return toPayload((LifecycleNotification) notification).filter(Objects::nonNull);
        } else if (notification instanceof ResourceMetaDataNotification) {
            return toPayload((ResourceMetaDataNotification) notification).filter(Objects::nonNull);
        }
        LOG.error("Unknown notification type {}", notification.getClass());
        return emptyStream();
    }

    protected Promise<Stream<T>> toPayload(LifecycleNotification lifecycleNotification) {
        return emptyStream();
    }

    protected Promise<Stream<T>> toPayload(ResourceDataNotification dataNotification) {
        return emptyStream();
    }

    protected Promise<Stream<T>> toPayload(ResourceMetaDataNotification metadataNotification) {
        return emptyStream();
    }

    public String getTopicFilter() {
        return topicFilter;
    }

    protected abstract Class<T> getPayloadType();

    protected final Promise<Stream<T>> emptyStream() {
        return thread.getPromiseFactory().resolved(Stream.empty());
    }

    protected Promise<ProviderSnapshot> getProvider(final String providerName) {
        try {
            return thread.execute(new AbstractTwinCommand<ProviderSnapshot>() {
                @Override
                protected Promise<ProviderSnapshot> call(SensinactDigitalTwin twin, PromiseFactory pf) {
                    List<ProviderSnapshot> snapshots = twin.filteredSnapshot(null,
                            p -> p.getName().equals(providerName), null, null);
                    if (snapshots.isEmpty()) {
                        return pf.failed(new NotFoundException(providerName));
                    } else {
                        return pf.resolved(snapshots.get(0));
                    }
                }
            }).recoverWith(p -> {
                PromiseFactory pf = thread.getPromiseFactory();
                final Throwable cause = p.getFailure();
                if (cause instanceof SensorThingsMqttException) {
                    return pf.failed(cause);
                } else {
                    return pf.failed(new SensorThingsMqttException("Error querying SensiNact", cause));
                }
            });
        } catch (Exception e) {
            return thread.getPromiseFactory()
                    .failed(new SensorThingsMqttException("Interrupted while querying SensiNact", e));
        }
    }

    protected Promise<ResourceSnapshot> getResource(final String providerName, final String serviceName,
            final String resourceName) {
        return getProvider(providerName).map(p -> {
            ResourceSnapshot rc = p.getResource(serviceName, resourceName);
            if (rc != null) {
                return rc;
            }
            throw new NotFoundException(String.join("~", providerName, serviceName, resourceName));
        });
    }

    protected Promise<Stream<T>> wrap(T value) {
        return thread.getPromiseFactory().resolved(Optional.ofNullable(value).stream());
    }

    protected Promise<Stream<T>> decorate(Promise<T> promise) {
        return promise.map(Stream::of)
                .onFailure(t -> LOG.warn("An error occurred executing mapper {}", getClass().getSimpleName(), t))
                .recoverWith(x -> emptyStream());
    }

    protected <R> Promise<Stream<R>> mapProvider(Promise<ProviderSnapshot> p,
            Function<Promise<ResourceSnapshot>, Promise<Stream<R>>> conversion) {
        PromiseFactory pf = thread.getPromiseFactory();

        Promise<List<Stream<R>>> sensors = p.flatMap(
                ps -> getResourceSnapshots(ps).map(r -> conversion.apply(pf.resolved(r))).collect(pf.toPromise()));

        return sensors.map(l -> l.stream().flatMap(Function.identity()));

    }

    private Stream<ResourceSnapshot> getResourceSnapshots(ProviderSnapshot ps) {
        return ps.getServices().stream().flatMap(s -> s.getResources().stream());
    }

    public static SensorthingsMapper<?> create(String topicFilter, ObjectMapper jsonMapper, GatewayThread thread) {

        String[] segments = topicFilter.split("/");

        if (!"v1.1".equals(segments[0])) {
            LOG.warn("The topic filter {} is not for the v1.1 API", topicFilter);
            return new NullMapper(topicFilter, jsonMapper, thread);
        }
        String selectFilter = null;
        int idx = segments[segments.length - 1].indexOf("?$select=");
        if (idx != -1) {
            selectFilter = segments[segments.length - 1].substring(idx + 9);
            segments[segments.length - 1] = segments[segments.length - 1].substring(0, idx);
        }

        SensorthingsMapper<?> mapper = null;

        if (segments.length == 2) {
            if (COLLECTIONS.contains(segments[1])) {
                // Collection entry tracking
                try {
                    mapper = getCollectionMapper(topicFilter, segments[1], jsonMapper, thread);
                } catch (IllegalArgumentException iae) {
                    LOG.warn(
                            "The topic filter {} is not valid for the v1.1 API as it refers to an unknown collection type",
                            topicFilter, iae);
                }
            } else {
                // Entity updates:
                // https://docs.ogc.org/is/18-088/18-088.html#mqtt-entity-updates
                Matcher matcher = ENTITY_PATH.matcher(segments[1]);
                if (matcher.matches()) {
                    try {
                        mapper = getEntityMapper(topicFilter, matcher.group(1), matcher.group(2), jsonMapper, thread);
                    } catch (IllegalArgumentException iae) {
                        LOG.warn(
                                "The topic filter {} is not valid for the v1.1 API as it refers to an unknown entity type",
                                topicFilter, iae);
                    }
                } else {
                    LOG.warn("The topic filter {} is not valid for the v1.1 API as it does not select a single entity",
                            topicFilter);
                }
            }
        } else if (segments.length == 3) {
            Matcher matcher = ENTITY_PATH.matcher(segments[1]);
            if (matcher.matches()) {
                if (COLLECTIONS.contains(segments[2])) {
                    // https://docs.ogc.org/is/18-088/18-088.html#mqtt-subscribe-entity-set
                    mapper = getEntitySetMapper(topicFilter, segments[2], matcher.group(1), matcher.group(2),
                            jsonMapper, thread);
                } else {
                    // (https://docs.ogc.org/is/18-088/18-088.html#mqtt-subscribe-entity-property)
                    mapper = getPropertyMapper(topicFilter, segments[2], matcher.group(1), matcher.group(2), jsonMapper,
                            thread);
                }
            } else {
                LOG.warn("The topic filter {} is not valid for the v1.1 API as it does not select a single entity",
                        topicFilter);
            }

        } else {
            LOG.warn("The topic filter {} is not valid for the v1.1 API as it has the wrong number of segments",
                    topicFilter);
        }

        if (mapper == null) {
            mapper = new NullMapper(topicFilter, jsonMapper, thread);
        }

        if (selectFilter != null) {
            mapper = new SelectMapper(topicFilter, selectFilter, mapper, jsonMapper, thread);
        }

        return mapper;
    }

    private static SensorthingsMapper<?> getCollectionMapper(String topicFilter, String entity, ObjectMapper mapper,
            GatewayThread thread) {
        switch (entity) {
        case "Things":
            return new ThingsMapper(topicFilter, mapper, thread);
        case "Locations":
            return new LocationsMapper(topicFilter, mapper, thread);
        case "HistoricalLocations":
            return new HistoricalLocationsMapper(topicFilter, mapper, thread);
        case "Datastreams":
            return new DatastreamsMapper(topicFilter, mapper, thread);
        case "Sensors":
            return new SensorsMapper(topicFilter, mapper, thread);
        case "Observations":
            return new ObservationsMapper(topicFilter, mapper, thread);
        case "ObservedProperties":
            return new ObservedPropertiesMapper(topicFilter, mapper, thread);
        case "FeaturesOfInterest":
            return new FeaturesOfInterestMapper(topicFilter, mapper, thread);
        default:
            LOG.warn("The collection type {} is not recognised", entity);
            return null;
        }
    }

    private static SensorthingsMapper<?> getEntityMapper(String topicFilter, String entity, String id,
            ObjectMapper mapper, GatewayThread thread) {
        switch (entity) {
        case "Things":
            return new ThingMapper(topicFilter, id, mapper, thread);
        case "Locations":
            return new LocationMapper(topicFilter, id, mapper, thread);
        case "HistoricalLocations":
            return new HistoricalLocationMapper(topicFilter, id, mapper, thread);
        case "Datastreams":
            return new DatastreamMapper(topicFilter, id, mapper, thread);
        case "Sensors":
            return new SensorMapper(topicFilter, id, mapper, thread);
        case "Observations":
            return new ObservationMapper(topicFilter, id, mapper, thread);
        case "ObservedProperties":
            return new ObservedPropertyMapper(topicFilter, id, mapper, thread);
        case "FeaturesOfInterest":
            return new FeatureOfInterestMapper(topicFilter, id, mapper, thread);
        default:
            LOG.warn("The entity type {} is not recognised", entity);
            return null;
        }
    }

    private static SensorthingsMapper<?> getEntitySetMapper(String topicFilter, String collection, String parentType,
            String parentId, ObjectMapper mapper, GatewayThread thread) {
        switch (collection) {
        case "Things":
            return getThingsSubCollection(topicFilter, parentType, parentId, mapper, thread);
        case "Locations":
            return getLocationsSubCollection(topicFilter, parentType, parentId, mapper, thread);
        case "HistoricalLocations":
            return getHistoricalLocationsSubCollection(topicFilter, parentType, parentId, mapper, thread);
        case "Datastreams":
            return getDatastreamsSubCollection(topicFilter, parentType, parentId, mapper, thread);
        case "Observations":
            return getObservationsSubCollection(topicFilter, parentType, parentId, mapper, thread);
        case "Sensors":
        case "ObservedProperties":
        case "FeaturesOfInterest":
            LOG.warn("The collection type {} is not a sub collection of any recognised type", collection);
            return null;
        default:
            LOG.warn("The collection type {} is not recognised", collection);
            return null;
        }
    }

    private static SensorthingsMapper<?> getThingsSubCollection(String topicFilter, String parentType, String parentId,
            ObjectMapper mapper, GatewayThread thread) {
        if ("Locations".equals(parentType)) {
            return new ThingMapper(topicFilter, parentId.split("~")[0], mapper, thread);
        } else {
            LOG.warn("The entity type {} does not contain a set of Things", parentType);
            return null;
        }
    }

    private static SensorthingsMapper<?> getLocationsSubCollection(String topicFilter, String parentType,
            String parentId, ObjectMapper mapper, GatewayThread thread) {
        if ("Things".equals(parentType) || "HistoricalLocations".equals(parentType)) {
            return new LocationMapper(topicFilter, parentId, mapper, thread);
        } else {
            LOG.warn("The entity type {} does not contain a set of Locations", parentType);
            return null;
        }
    }

    private static SensorthingsMapper<?> getHistoricalLocationsSubCollection(String topicFilter, String parentType,
            String parentId, ObjectMapper mapper, GatewayThread thread) {
        if ("Locations".equals(parentType)) {
            return new HistoricalLocationMapper(topicFilter, parentId.split("~")[0], mapper, thread);
        } else {
            LOG.warn("The entity type {} does not contain a set of HistoricalLocations", parentType);
            return null;
        }
    }

    private static SensorthingsMapper<?> getDatastreamsSubCollection(String topicFilter, String parentType,
            String parentId, ObjectMapper mapper, GatewayThread thread) {
        if ("Things".equals(parentType)) {
            return new DatastreamsMapper(topicFilter, mapper, thread) {
                @Override
                public Promise<Stream<Datastream>> toPayload(ResourceNotification notification) {
                    return parentId.equals(notification.provider()) ? super.toPayload(notification) : emptyStream();
                }
            };
        } else if ("Sensors".equals(parentType) || "ObservedProperties".equals(parentType)) {
            return new DatastreamMapper(topicFilter, parentId, mapper, thread);
        } else {
            LOG.warn("The entity type {} does not contain a set of Datastreams", parentType);
            return null;
        }
    }

    private static SensorthingsMapper<?> getObservationsSubCollection(String topicFilter, String parentType,
            String parentId, ObjectMapper mapper, GatewayThread thread) {
        if ("Datastreams".equals(parentType) || "FeaturesOfInterest".equals(parentType)) {
            return new ObservationMapper(topicFilter, parentId, mapper, thread);
        } else {
            LOG.warn("The entity type {} does not contain a set of Observations", parentType);
            return null;
        }
    }

    private static SensorthingsMapper<?> getPropertyMapper(String topicFilter, String property, String parentType,
            String parentId, ObjectMapper mapper, GatewayThread thread) {

        SensorthingsMapper<?> parent = getEntityMapper(topicFilter, parentType, parentId, mapper, thread);

        switch (property) {
        case "name":
            return (Thing.class.equals(parent.getPayloadType()))
                    ? getPropertyMapper(NameDescription.class::isAssignableFrom, property, null, "admin",
                            "friendlyName", parent, mapper, thread)
                    : getPropertyMapper(NameDescription.class::isAssignableFrom, property, null, null, null, parent,
                            mapper, thread);
        case "description":
            return getPropertyMapper(NameDescription.class::isAssignableFrom, property, null, "admin", "description",
                    parent, mapper, thread);
        case "unitOfMeasurement":
            // TODO Metadata change?
            return getPropertyMapper(Datastream.class::equals, property, null, null, null, parent, mapper, thread);
        case "observedArea":
            return getPropertyMapper(Datastream.class::equals, property, null, "admin", "location", parent, mapper,
                    thread);
        case "resultTime":
        case "phenomenonTime":
            if (Datastream.class.equals(parent.getPayloadType()))
                return null;
            return getPropertyMapper(Observation.class::equals, property, null, null, null, parent, mapper, thread);
        case "properties":
            // TODO Metadata change?
            return getPropertyMapper(
                    Set.of(Datastream.class, ObservedProperty.class, Sensor.class, Thing.class)::contains, property,
                    null, null, null, parent, mapper, thread);
        case "feature":
            return getPropertyMapper(FeatureOfInterest.class::equals, property, null, "admin", "location", parent,
                    mapper, thread);
        case "time":
            // TODO - can this actually change?
            return getPropertyMapper(HistoricalLocation.class::equals, property, null, null, null, parent, mapper,
                    thread);
        case "encodingType":
            if (Location.class.equals(parent.getPayloadType()))
                return null;
            // TODO Metadata change?
            return getPropertyMapper(Sensor.class::equals, property, null, null, null, parent, mapper, thread);
        case "location":
            return getPropertyMapper(Location.class::equals, property, null, "admin", "location", parent, mapper,
                    thread);
        case "result":
            return getPropertyMapper(Observation.class::equals, property, null, null, null, parent, mapper, thread);
        case "definition":
            // TODO Metadata change?
            return getPropertyMapper(ObservedProperty.class::equals, property, null, null, null, parent, mapper,
                    thread);
        case "metadata":
            // TODO Metadata change?
            return getPropertyMapper(Sensor.class::equals, property, null, null, null, parent, mapper, thread);
        case "observationType":
        case "resultQuality":
        case "validTime":
        case "parameters":
            // These cannot change
            return null;
        default:
            LOG.warn("The property {} is not recognised", property);
            return null;
        }
    }

    private static SensorthingsMapper<?> getPropertyMapper(Predicate<Class<?>> valid, String property, String provider,
            String service, String resource, SensorthingsMapper<?> parent, ObjectMapper mapper, GatewayThread thread) {
        if (valid.test(parent.getPayloadType())) {
            return new PropertyMapper(parent.getTopicFilter(), property, provider, service, resource, parent, mapper,
                    thread);
        } else {
            throw new IllegalArgumentException("The property " + property + " from filter " + parent.getTopicFilter()
                    + " cannot be found in the target object");
        }
    }

    private static class NullMapper extends SensorthingsMapper<Object> {

        public NullMapper(String topic, ObjectMapper mapper, GatewayThread thread) {
            super(topic, mapper, thread);
        }

        @Override
        public Promise<Stream<Object>> toPayload(ResourceDataNotification notification) {
            return emptyStream();
        }

        @Override
        protected Class<Object> getPayloadType() {
            return Object.class;
        }
    }
}
