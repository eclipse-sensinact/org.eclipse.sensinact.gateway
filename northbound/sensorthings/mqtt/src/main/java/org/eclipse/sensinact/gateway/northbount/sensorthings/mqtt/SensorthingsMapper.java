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

    protected SensorthingsMapper(final String topicFilter, final ObjectMapper mapper, final GatewayThread gateway) {
        this.topicFilter = topicFilter;
        this.jsonMapper = mapper;
        this.thread = gateway;
    }

    public Promise<Stream<T>> toPayload(final ResourceNotification notification) {
        if (notification instanceof ResourceDataNotification) {
            return this.toPayload((ResourceDataNotification) notification).filter(Objects::nonNull);
        }
        if (notification instanceof LifecycleNotification) {
            return this.toPayload((LifecycleNotification) notification).filter(Objects::nonNull);
        }
        if (notification instanceof ResourceMetaDataNotification) {
            return this.toPayload((ResourceMetaDataNotification) notification).filter(Objects::nonNull);
        }
        SensorthingsMapper.LOG.error("Unknown notification type {}", notification.getClass());
        return this.emptyStream();
    }

    protected Promise<Stream<T>> toPayload(final LifecycleNotification lifecycleNotification) {
        return this.emptyStream();
    }

    protected Promise<Stream<T>> toPayload(final ResourceDataNotification dataNotification) {
        return this.emptyStream();
    }

    protected Promise<Stream<T>> toPayload(final ResourceMetaDataNotification metadataNotification) {
        return this.emptyStream();
    }

    public String getTopicFilter() {
        return this.topicFilter;
    }

    protected abstract Class<T> getPayloadType();

    protected final Promise<Stream<T>> emptyStream() {
        return this.thread.getPromiseFactory().resolved(Stream.empty());
    }

    protected Promise<ProviderSnapshot> getProvider(final String providerName) {
        try {
            return this.thread.execute(new AbstractTwinCommand<ProviderSnapshot>() {
                @Override
                protected Promise<ProviderSnapshot> call(final SensinactDigitalTwin twin, final PromiseFactory pf) {
                    final List<ProviderSnapshot> snapshots = twin.filteredSnapshot(null,
                            p -> p.getName().equals(providerName), null, null);
                    if (snapshots.isEmpty()) {
                        return pf.failed(new NotFoundException(providerName));
                    }
                    return pf.resolved(snapshots.get(0));
                }
            }).recoverWith(p -> {
                final PromiseFactory pf = this.thread.getPromiseFactory();
                final Throwable cause = p.getFailure();
                if (cause instanceof SensorThingsMqttException) {
                    return pf.failed(cause);
                }
                return pf.failed(new SensorThingsMqttException("Error querying SensiNact", cause));
            });
        } catch (final Exception e) {
            return this.thread.getPromiseFactory()
                    .failed(new SensorThingsMqttException("Interrupted while querying SensiNact", e));
        }
    }

    protected Promise<ResourceSnapshot> getResource(final String providerName, final String serviceName,
            final String resourceName) {
        return this.getProvider(providerName).map(p -> {
            final ResourceSnapshot rc = p.getResource(serviceName, resourceName);
            if (rc != null) {
                return rc;
            }
            throw new NotFoundException(String.join("~", providerName, serviceName, resourceName));
        });
    }

    protected Promise<Stream<T>> wrap(final T value) {
        return this.thread.getPromiseFactory().resolved(Optional.ofNullable(value).stream());
    }

    protected Promise<Stream<T>> decorate(final Promise<T> promise) {
        return promise
                .map(v -> v == null ? Stream.<T>empty() : Stream.of(v)).onFailure(t -> SensorthingsMapper.LOG
                        .warn("An error occurred executing mapper {}", this.getClass().getSimpleName(), t))
                .recoverWith(x -> this.emptyStream());
    }

    protected <R> Promise<Stream<R>> mapProvider(final Promise<ProviderSnapshot> p,
            final Function<Promise<ResourceSnapshot>, Promise<Stream<R>>> conversion) {
        final PromiseFactory pf = this.thread.getPromiseFactory();

        final Promise<List<Stream<R>>> sensors = p.flatMap(
                ps -> this.getResourceSnapshots(ps).map(r -> conversion.apply(pf.resolved(r))).collect(pf.toPromise()));

        return sensors.map(l -> l.stream().flatMap(Function.identity()));

    }

    protected <R> Promise<Stream<R>> mapProviderIfResource(final Promise<ProviderSnapshot> p,
            final Predicate<ProviderSnapshot> filter,
            final Function<Promise<ResourceSnapshot>, Promise<Stream<R>>> conversion,
            final Function<ProviderSnapshot, Promise<Stream<R>>> fallback) {

        final PromiseFactory pf = this.thread.getPromiseFactory();

        return p.flatMap(ps -> {
            if (!filter.test(ps)) {
                return fallback.apply(ps);
            }
            final Promise<List<Stream<R>>> resourceStreams = this.getResourceSnapshots(ps)
                    .map(r -> conversion.apply(pf.resolved(r))).collect(pf.toPromise());

            return resourceStreams.map(list -> list.stream().flatMap(Function.identity()));
        });
    }

    protected <R> Promise<Stream<R>> mapProviderIfProvider(final Promise<ProviderSnapshot> p,
            final Predicate<ProviderSnapshot> filter,
            final Function<Promise<ProviderSnapshot>, Promise<Stream<R>>> conversion,
            final Function<ProviderSnapshot, Promise<Stream<R>>> fallback) {

        final PromiseFactory pf = this.thread.getPromiseFactory();

        return p.flatMap(ps -> {
            if (!filter.test(ps)) {
                return fallback.apply(ps);
            }
            return conversion.apply(pf.resolved(ps));

        });
    }

    private Stream<ResourceSnapshot> getResourceSnapshots(final ProviderSnapshot ps) {
        return ps.getServices().stream().flatMap(s -> s.getResources().stream());
    }

    public static SensorthingsMapper<?> create(final String topicFilter, final ObjectMapper jsonMapper,
            final GatewayThread thread) {

        final String[] segments = topicFilter.split("/");

        if (!"v1.1".equals(segments[0])) {
            SensorthingsMapper.LOG.warn("The topic filter {} is not for the v1.1 API", topicFilter);
            return new NullMapper(topicFilter, jsonMapper, thread);
        }
        String selectFilter = null;
        final int idx = segments[segments.length - 1].indexOf("?$select=");
        if (idx != -1) {
            selectFilter = segments[segments.length - 1].substring(idx + 9);
            segments[segments.length - 1] = segments[segments.length - 1].substring(0, idx);
        }

        SensorthingsMapper<?> mapper = null;

        if (segments.length == 2) {
            if (SensorthingsMapper.COLLECTIONS.contains(segments[1])) {
                // Collection entry tracking
                try {
                    mapper = SensorthingsMapper.getCollectionMapper(topicFilter, segments[1], jsonMapper, thread);
                } catch (final IllegalArgumentException iae) {
                    SensorthingsMapper.LOG.warn(
                            "The topic filter {} is not valid for the v1.1 API as it refers to an unknown collection type",
                            topicFilter, iae);
                }
            } else {
                // Entity updates:
                // https://docs.ogc.org/is/18-088/18-088.html#mqtt-entity-updates
                final Matcher matcher = SensorthingsMapper.ENTITY_PATH.matcher(segments[1]);
                if (matcher.matches()) {
                    try {
                        mapper = SensorthingsMapper.getEntityMapper(topicFilter, matcher.group(1), matcher.group(2),
                                jsonMapper, thread);
                    } catch (final IllegalArgumentException iae) {
                        SensorthingsMapper.LOG.warn(
                                "The topic filter {} is not valid for the v1.1 API as it refers to an unknown entity type",
                                topicFilter, iae);
                    }
                } else {
                    SensorthingsMapper.LOG.warn(
                            "The topic filter {} is not valid for the v1.1 API as it does not select a single entity",
                            topicFilter);
                }
            }
        } else if (segments.length == 3) {
            final Matcher matcher = SensorthingsMapper.ENTITY_PATH.matcher(segments[1]);
            if (matcher.matches()) {
                if (SensorthingsMapper.COLLECTIONS.contains(segments[2])) {
                    // https://docs.ogc.org/is/18-088/18-088.html#mqtt-subscribe-entity-set
                    mapper = SensorthingsMapper.getEntitySetMapper(topicFilter, segments[2], matcher.group(1),
                            matcher.group(2), jsonMapper, thread);
                } else {
                    // (https://docs.ogc.org/is/18-088/18-088.html#mqtt-subscribe-entity-property)
                    mapper = SensorthingsMapper.getPropertyMapper(topicFilter, segments[2], matcher.group(1),
                            matcher.group(2), jsonMapper, thread);
                }
            } else {
                SensorthingsMapper.LOG.warn(
                        "The topic filter {} is not valid for the v1.1 API as it does not select a single entity",
                        topicFilter);
            }

        } else {
            SensorthingsMapper.LOG.warn(
                    "The topic filter {} is not valid for the v1.1 API as it has the wrong number of segments",
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

    private static SensorthingsMapper<?> getCollectionMapper(final String topicFilter, final String entity,
            final ObjectMapper mapper, final GatewayThread thread) {
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
            SensorthingsMapper.LOG.warn("The collection type {} is not recognised", entity);
            return null;
        }
    }

    private static SensorthingsMapper<?> getEntityMapper(final String topicFilter, final String entity, final String id,
            final ObjectMapper mapper, final GatewayThread thread) {
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
            SensorthingsMapper.LOG.warn("The entity type {} is not recognised", entity);
            return null;
        }
    }

    private static SensorthingsMapper<?> getEntitySetMapper(final String topicFilter, final String collection,
            final String parentType, final String parentId, final ObjectMapper mapper, final GatewayThread thread) {
        switch (collection) {
        case "Things":
            return SensorthingsMapper.getThingsSubCollection(topicFilter, parentType, parentId, mapper, thread);
        case "Locations":
            return SensorthingsMapper.getLocationsSubCollection(topicFilter, parentType, parentId, mapper, thread);
        case "HistoricalLocations":
            return SensorthingsMapper.getHistoricalLocationsSubCollection(topicFilter, parentType, parentId, mapper,
                    thread);
        case "Datastreams":
            return SensorthingsMapper.getDatastreamsSubCollection(topicFilter, parentType, parentId, mapper, thread);
        case "Observations":
            return SensorthingsMapper.getObservationsSubCollection(topicFilter, parentType, parentId, mapper, thread);
        case "Sensors":
        case "ObservedProperties":
        case "FeaturesOfInterest":
            SensorthingsMapper.LOG.warn("The collection type {} is not a sub collection of any recognised type",
                    collection);
            return null;
        default:
            SensorthingsMapper.LOG.warn("The collection type {} is not recognised", collection);
            return null;
        }
    }

    private static SensorthingsMapper<?> getThingsSubCollection(final String topicFilter, final String parentType,
            final String parentId, final ObjectMapper mapper, final GatewayThread thread) {
        if ("Locations".equals(parentType)) {
            return new ThingMapper(topicFilter, parentId.split("~")[0], mapper, thread);
        }
        SensorthingsMapper.LOG.warn("The entity type {} does not contain a set of Things", parentType);
        return null;
    }

    private static SensorthingsMapper<?> getLocationsSubCollection(final String topicFilter, final String parentType,
            final String parentId, final ObjectMapper mapper, final GatewayThread thread) {
        if ("Things".equals(parentType) || "HistoricalLocations".equals(parentType)) {
            return new LocationMapper(topicFilter, parentId, mapper, thread);
        }
        SensorthingsMapper.LOG.warn("The entity type {} does not contain a set of Locations", parentType);
        return null;
    }

    private static SensorthingsMapper<?> getHistoricalLocationsSubCollection(final String topicFilter,
            final String parentType, final String parentId, final ObjectMapper mapper, final GatewayThread thread) {
        if ("Locations".equals(parentType)) {
            return new HistoricalLocationMapper(topicFilter, parentId.split("~")[0], mapper, thread);
        }
        SensorthingsMapper.LOG.warn("The entity type {} does not contain a set of HistoricalLocations", parentType);
        return null;
    }

    private static SensorthingsMapper<?> getDatastreamsSubCollection(final String topicFilter, final String parentType,
            final String parentId, final ObjectMapper mapper, final GatewayThread thread) {
        if ("Things".equals(parentType)) {
            return new DatastreamsMapper(topicFilter, mapper, thread) {
                @Override
                public Promise<Stream<Datastream>> toPayload(final ResourceNotification notification) {
                    return parentId.equals(notification.provider()) ? super.toPayload(notification)
                            : this.emptyStream();
                }
            };
        }
        if ("Sensors".equals(parentType) || "ObservedProperties".equals(parentType)) {
            return new DatastreamMapper(topicFilter, parentId, mapper, thread);
        }
        SensorthingsMapper.LOG.warn("The entity type {} does not contain a set of Datastreams", parentType);
        return null;
    }

    private static SensorthingsMapper<?> getObservationsSubCollection(final String topicFilter, final String parentType,
            final String parentId, final ObjectMapper mapper, final GatewayThread thread) {
        if ("Datastreams".equals(parentType) || "FeaturesOfInterest".equals(parentType)) {
            return new ObservationMapper(topicFilter, parentId, mapper, thread);
        }
        SensorthingsMapper.LOG.warn("The entity type {} does not contain a set of Observations", parentType);
        return null;
    }

    private static SensorthingsMapper<?> getPropertyMapper(final String topicFilter, final String property,
            final String parentType, final String parentId, final ObjectMapper mapper, final GatewayThread thread) {

        final SensorthingsMapper<?> parent = SensorthingsMapper.getEntityMapper(topicFilter, parentType, parentId,
                mapper, thread);

        switch (property) {
        case "name":
            return Thing.class.equals(parent.getPayloadType())
                    ? SensorthingsMapper.getPropertyMapper(NameDescription.class::isAssignableFrom, property, null,
                            "admin", "friendlyName", parent, mapper, thread)
                    : SensorthingsMapper.getPropertyMapper(NameDescription.class::isAssignableFrom, property, null,
                            null, null, parent, mapper, thread);
        case "description":
            return SensorthingsMapper.getPropertyMapper(NameDescription.class::isAssignableFrom, property, null,
                    "admin", "description", parent, mapper, thread);
        case "unitOfMeasurement":
            // TODO Metadata change?
            return SensorthingsMapper.getPropertyMapper(Datastream.class::equals, property, null, null, null, parent,
                    mapper, thread);
        case "observedArea":
            return SensorthingsMapper.getPropertyMapper(Datastream.class::equals, property, null, "admin", "location",
                    parent, mapper, thread);
        case "resultTime":
        case "phenomenonTime":
            if (Datastream.class.equals(parent.getPayloadType())) {
                return null;
            }
            return SensorthingsMapper.getPropertyMapper(Observation.class::equals, property, null, null, null, parent,
                    mapper, thread);
        case "properties":
            // TODO Metadata change?
            return SensorthingsMapper.getPropertyMapper(
                    Set.of(Datastream.class, ObservedProperty.class, Sensor.class, Thing.class)::contains, property,
                    null, null, null, parent, mapper, thread);
        case "feature":
            return SensorthingsMapper.getPropertyMapper(FeatureOfInterest.class::equals, property, null, "admin",
                    "location", parent, mapper, thread);
        case "time":
            // TODO - can this actually change?
            return SensorthingsMapper.getPropertyMapper(HistoricalLocation.class::equals, property, null, null, null,
                    parent, mapper, thread);
        case "encodingType":
            if (Location.class.equals(parent.getPayloadType())) {
                return null;
            }
            // TODO Metadata change?
            return SensorthingsMapper.getPropertyMapper(Sensor.class::equals, property, null, null, null, parent,
                    mapper, thread);
        case "location":
            return SensorthingsMapper.getPropertyMapper(Location.class::equals, property, null, "admin", "location",
                    parent, mapper, thread);
        case "result":
            return SensorthingsMapper.getPropertyMapper(Observation.class::equals, property, null, null, null, parent,
                    mapper, thread);
        case "definition":
            // TODO Metadata change?
            return SensorthingsMapper.getPropertyMapper(ObservedProperty.class::equals, property, null, null, null,
                    parent, mapper, thread);
        case "metadata":
            // TODO Metadata change?
            return SensorthingsMapper.getPropertyMapper(Sensor.class::equals, property, null, null, null, parent,
                    mapper, thread);
        case "observationType":
        case "resultQuality":
        case "validTime":
        case "parameters":
            // These cannot change
            return null;
        default:
            SensorthingsMapper.LOG.warn("The property {} is not recognised", property);
            return null;
        }
    }

    private static SensorthingsMapper<?> getPropertyMapper(final Predicate<Class<?>> valid, final String property,
            final String provider, final String service, final String resource, final SensorthingsMapper<?> parent,
            final ObjectMapper mapper, final GatewayThread thread) {
        if (valid.test(parent.getPayloadType())) {
            return new PropertyMapper(parent.getTopicFilter(), property, provider, service, resource, parent, mapper,
                    thread);
        }
        throw new IllegalArgumentException("The property " + property + " from filter " + parent.getTopicFilter()
                + " cannot be found in the target object");
    }

    private static class NullMapper extends SensorthingsMapper<Object> {

        public NullMapper(final String topic, final ObjectMapper mapper, final GatewayThread thread) {
            super(topic, mapper, thread);
        }

        @Override
        public Promise<Stream<Object>> toPayload(final ResourceDataNotification notification) {
            return this.emptyStream();
        }

        @Override
        protected Class<Object> getPayloadType() {
            return Object.class;
        }
    }
}
