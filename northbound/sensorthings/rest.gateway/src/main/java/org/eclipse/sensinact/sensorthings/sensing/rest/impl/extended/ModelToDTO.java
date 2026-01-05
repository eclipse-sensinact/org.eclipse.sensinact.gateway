package org.eclipse.sensinact.sensorthings.sensing.rest.impl.extended;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin.SnapshotOption;
import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.gateway.geojson.Polygon;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.UnitOfMeasurement;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.UriInfo;

public class ModelToDTO {
    private static final String ADMIN = "admin";
    private static final String DESCRIPTION = "description";
    private static final String FRIENDLY_NAME = "name";
    private static final String LOCATION = "location";
    private static final String DEFAULT_ENCODING_TYPE = "text/plain";
    private static final String ENCODING_TYPE_VND_GEO_JSON = "application/vnd.geo+json";
    public static final String VERSION = "v1.1";
    private static final String TYPE = "type";
    private static final String NO_DESCRIPTION = "No description";
    private static final String NO_DEFINITION = "No definition";

    public static ServiceSnapshot getServiceSnapshot(ProviderSnapshot provider, String name) {
        return provider.getServices().stream().filter(s -> name.equals(s.getName())).findFirst().get();
    }

    public static Optional<ProviderSnapshot> getProviderSnapshot(SensiNactSession session, String id) {
        return Optional.ofNullable(session.providerSnapshot(id, EnumSet.noneOf(SnapshotOption.class)));
    }

    public static Thing toThing(SensiNactSession userSession, Application application, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ProviderSnapshot provider) {
        String id = provider.getName();

        ServiceSnapshot serviceAdmin = getServiceSnapshot(provider, ADMIN);
        ServiceSnapshot serviceThing = getServiceSnapshot(provider, "thing");
        String name = Objects.requireNonNullElse(getResourceField(serviceAdmin, FRIENDLY_NAME, String.class),
                provider.getName());

        String description = Objects.requireNonNullElse(getResourceField(serviceAdmin, DESCRIPTION, String.class),
                NO_DESCRIPTION);

        String selfLink = getLink(uriInfo, VERSION, "Things({id})", id);
        String datastreamsLink = getLink(uriInfo, selfLink, "Datastreams");
        String historicalLocationsLink = getLink(uriInfo, selfLink, "HistoricalLocations");
        String locationsLink = getLink(uriInfo, selfLink, "Locations");
        @SuppressWarnings("unchecked")
        List<String> locationIds = getResourceField(serviceThing, "locationIds", List.class);
        Thing thing = new Thing(selfLink, id, name, description, null, datastreamsLink, historicalLocationsLink,
                locationsLink);

        if (expansions.shouldExpand("Datastreams", thing)) {
            expansions.addExpansion("Datastreams", thing, toDatastreams(userSession, application, mapper, uriInfo,
                    expansions.getExpansionSettings("Datastreams"), filter, provider));
        }

        if (expansions.shouldExpand("HistoricalLocations", thing)) {
            Optional<HistoricalLocation> historicalLocation = DtoMapper.toHistoricalLocation(userSession, application,
                    mapper, uriInfo, expansions.getExpansionSettings("HistoricalLocations"), filter, provider);
            if (historicalLocation.isPresent()) {
                ResultList<HistoricalLocation> list = new ResultList<>(null, null, List.of(historicalLocation.get()));
                expansions.addExpansion("HistoricalLocations", thing, list);
            }
        }
        if (expansions.shouldExpand("Locations", thing)) {
            if (locationIds != null) {

                List<ServiceSnapshot> services = locationIds.stream()
                        .map(idLocation -> getProviderSnapshot(userSession, idLocation)).flatMap(Optional::stream)
                        .map(p -> p.getServices()).flatMap(List::stream).toList();
                List<Location> locations = services.stream().filter(service -> service.getName() != ADMIN)
                        .map(s -> toLocation(userSession, application, mapper, uriInfo, expansions, filter, s))
                        .toList();
                ResultList<Location> list = new ResultList<>(null, null, locations);
                expansions.addExpansion("Locations", thing, list);
            }
        }

        return thing;
    }

    public static Datastream toDatastream(SensiNactSession userSession, Application application, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ServiceSnapshot service) {

        String providerName = service.getProvider().getName();
        String id = String.format("%s~%s", providerName, service.getName());

        String name = getResourceField(service, FRIENDLY_NAME, String.class);
        String description = getResourceField(service, DESCRIPTION, String.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = getResourceField(service, "properties", Map.class);
        UnitOfMeasurement unit = toUnitOfMeasure(userSession, application, mapper, uriInfo, expansions, filter,
                service);

        Polygon observedArea = null; // TODO

        String selfLink = getLink(uriInfo, VERSION, "Datastreams({id})", id);

        String observationsLink = getLink(uriInfo, selfLink, "Observations");
        String observedPropertyLink = getLink(uriInfo, selfLink, "ObservedProperty");
        String sensorLink = getLink(uriInfo, selfLink, "Sensor");

        String thingLink = getLink(uriInfo, selfLink, "Thing");

        Datastream datastream = new Datastream(selfLink, id, name, description,
                "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Observation", unit, observedArea, null, null,
                metadata, observationsLink, observedPropertyLink, sensorLink, thingLink);
        if (expansions.shouldExpand("Observations", datastream)) {
            expansions.addExpansion("Observations", datastream, ModelToDTO.toObservations(userSession, application,
                    mapper, uriInfo, expansions.getExpansionSettings("Observations"), filter, service));
        }

        if (expansions.shouldExpand("ObservedProperty", datastream)) {
            expansions.addExpansion("ObservedProperty", datastream, toObservedProperty(userSession, application, mapper,
                    uriInfo, expansions.getExpansionSettings("ObservedProperty"), filter, service, selfLink));
        }

        if (expansions.shouldExpand("Sensor", datastream)) {
            expansions.addExpansion("Sensor", datastream, toSensor(userSession, application, mapper, uriInfo,
                    expansions.getExpansionSettings("Sensor"), filter, service, selfLink));
        }

        if (expansions.shouldExpand("Thing", datastream)) {
            expansions.addExpansion("Thing", datastream, toThing(userSession, application, mapper, uriInfo,
                    expansions.getExpansionSettings("Thing"), filter, service.getProvider()));
        }

        return datastream;
    }

    public static Sensor toSensor(SensiNactSession userSession, Application application, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ServiceSnapshot service,
            String datastreamLink) {
        String sensorId = getResourceField(service, "sensorId", String.class);
        String sensorName = getResourceField(service, "sensorName", String.class);
        String sensorDescription = getResourceField(service, "sensorDescription", String.class);
        String sensorEncodingType = getResourceField(service, "sensorEncodingType", String.class);
        Object sensorMetadata = getResourceField(service, "sensorMetadata", Object.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> sensorProperty = getResourceField(service, "sensorProperty", Map.class);

        String sensorLink = getLink(uriInfo, datastreamLink, "/Sensor({id})", sensorId);

        Sensor sensor = new Sensor(sensorLink, sensorId, sensorName, sensorDescription, sensorEncodingType,
                sensorMetadata, sensorProperty, datastreamLink);

        return sensor;
    }

    private static String getLink(UriInfo uriInfo, String baseUri, String path) {
        String sensorLink = uriInfo.getBaseUriBuilder().uri(baseUri).path(path).build().toString();
        return sensorLink;
    }

    public static String getLink(UriInfo uriInfo, String baseUri, String path, String id) {
        String sensorLink = uriInfo.getBaseUriBuilder().uri(baseUri).path(path).resolveTemplate("id", id).build()
                .toString();
        return sensorLink;
    }

    public static ObservedProperty toObservedProperty(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ServiceSnapshot service, String datastreamLink) {
        String observedPropertyId = getResourceField(service, "observedPropertyId", String.class);
        String observedPropertyName = getResourceField(service, "observedPropertyName", String.class);
        String observedPropertyDescription = getResourceField(service, "observedPropertyDescription", String.class);
        String observedPropertyDefinition = getResourceField(service, "observedPropertyDefinition", String.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> observedPropertyProperty = getResourceField(service, "observedPropertyProperties",
                Map.class);

        String observedPropertyLink = getLink(uriInfo, datastreamLink, "/ObservedProperty({id})", observedPropertyId);

        ObservedProperty observedProperty = new ObservedProperty(observedPropertyLink, observedPropertyId,
                observedPropertyName, observedPropertyDescription, observedPropertyDefinition, observedPropertyProperty,
                datastreamLink);

        return observedProperty;
    }

    public static Observation toObservation(SensiNactSession userSession, Application application, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ResourceSnapshot resource) {

        Observation observation = null;

        return observation;
    }

    public static List<Observation> toObservations(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ServiceSnapshot service) {

        List<Observation> observations = null;

        return observations;
    }

    private static TimedValue<GeoJsonObject> getLocation(ServiceSnapshot service, ObjectMapper mapper,
            boolean allowNull) {
        final ResourceSnapshot locationResource = service.getResource(LOCATION);

        final Instant time;
        final Object rawValue;
        if (locationResource == null) {
            time = Instant.EPOCH;
            rawValue = null;
        } else {
            final TimedValue<?> timedValue = locationResource.getValue();
            if (timedValue == null) {
                time = Instant.EPOCH;
                rawValue = null;
            } else {
                time = timedValue.getTimestamp() != null ? timedValue.getTimestamp() : Instant.EPOCH;
                rawValue = timedValue.getValue();
            }
        }
        return getLocation(mapper, rawValue, time, allowNull);
    }

    private static TimedValue<GeoJsonObject> getLocation(ObjectMapper mapper, Object rawValue, Instant time,
            boolean allowNull) {

        final GeoJsonObject parsedLocation;
        if (rawValue == null) {
            if (allowNull) {
                parsedLocation = null;
            } else {
                parsedLocation = new Point(Coordinates.EMPTY, null, null);
            }
        } else {
            if (rawValue instanceof GeoJsonObject) {
                parsedLocation = (GeoJsonObject) rawValue;
            } else if (rawValue instanceof String) {
                try {
                    parsedLocation = mapper.readValue((String) rawValue, GeoJsonObject.class);
                } catch (JsonProcessingException ex) {
                    if (allowNull) {
                        return null;
                    }
                    throw new RuntimeException("Invalid resource location content", ex);
                }
            } else {
                parsedLocation = mapper.convertValue(rawValue, GeoJsonObject.class);
            }
        }

        return new DefaultTimedValue<>(parsedLocation, time);
    }

    private static <T> T getResourceField(ServiceSnapshot service, String resourceName, Class<T> expectedType) {

        return service.getResource(resourceName) != null && service.getResource(resourceName).getValue() != null
                ? expectedType.cast(service.getResource(resourceName).getValue().getValue())
                : null;
    }

    public static Location toLocation(SensiNactSession userSession, Application application, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ServiceSnapshot service) {
        // check service is container correct type

        final TimedValue<GeoJsonObject> rcLocation = getLocation(service, mapper, false);
        final Instant time = rcLocation.getTimestamp();
        final GeoJsonObject object = rcLocation.getValue();

        String id = getResourceField(service, "sensorThingId", String.class);

        String name = Objects.requireNonNullElse(getResourceField(service, FRIENDLY_NAME, String.class), "");

        String description = Objects.requireNonNullElse(getResourceField(service, DESCRIPTION, String.class),
                NO_DESCRIPTION);

        String selfLink = getLink(uriInfo, VERSION, "Locations({id})", id);
        String thingsLink = getLink(uriInfo, selfLink, "Things");
        String historicalLocationsLink = getLink(uriInfo, selfLink, "HistoricalLocations");
        Location location = new Location(selfLink, id, name, description, ENCODING_TYPE_VND_GEO_JSON, object,
                thingsLink, historicalLocationsLink);
        if (expansions.shouldExpand("Things", location)) {
            ResultList<Thing> list = new ResultList<>(null, null, List.of(ModelToDTO.toThing(userSession, application,
                    mapper, uriInfo, expansions.getExpansionSettings("Thing"), filter, service.getProvider())));
            expansions.addExpansion("Things", location, list);
        }
        if (expansions.shouldExpand("HistoricalLocations", location)) {
            HistoricalLocation historicalLocation = ModelToDTO.toHistoricalLocation(userSession, application, mapper,
                    uriInfo, expansions.getExpansionSettings("HistoricalLocations"), filter, service.getProvider());
            if (historicalLocation != null) {
                ResultList<HistoricalLocation> list = new ResultList<>(null, null, List.of(historicalLocation));
                expansions.addExpansion("HistoricalLocations", location, list);
            }
        }

        return location;
    }

    public static HistoricalLocation toHistoricalLocation(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ProviderSnapshot provider) {
        if (provider == null) {
            throw new NotFoundException();
        }

        HistoricalLocation historicalLocation = null;

        return historicalLocation;
    }

    public static UnitOfMeasurement toUnitOfMeasure(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ServiceSnapshot service) {
        if (service == null) {
            throw new NotFoundException();
        }

        UnitOfMeasurement unit = getResourceField(service, "unit", UnitOfMeasurement.class);

        return unit;
    }

    public static List<Datastream> toDatastreams(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ProviderSnapshot provider) {
        if (provider == null) {
            throw new NotFoundException();
        }
        return provider.getServices().stream()
                .map(s -> toDatastream(userSession, application, mapper, uriInfo, expansions, filter, s)).toList();

    }

    public static FeatureOfInterest toFeatureOfInterest(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ProviderSnapshot provider) {
        if (provider == null) {
            throw new NotFoundException();
        }

        FeatureOfInterest featureOfInterest = null;

        return featureOfInterest;
    }
}
