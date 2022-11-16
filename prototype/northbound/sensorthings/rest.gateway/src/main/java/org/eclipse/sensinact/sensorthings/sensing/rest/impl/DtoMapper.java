/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.sensorthings.sensing.rest.impl;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.eclipse.sensinact.prototype.ResourceDescription;
import org.eclipse.sensinact.prototype.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.geojson.Point;
import org.geojson.Polygon;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.UriInfo;

public class DtoMapper {

    private static final String NO_DESCRIPTION = "No description";

    private static ResourceDescription getProviderAdminField(SensiNactSession userSession, String provider,
            String resource) {
        return userSession.describeResource(provider, "admin", resource);
    }

    private static String toString(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    public static Thing toThing(SensiNactSession userSession, UriInfo uriInfo, String providerName) {
        Thing thing = new Thing();
        thing.id = providerName;

        String friendlyName = toString(getProviderAdminField(userSession, providerName, "friendlyName").value);
        thing.name = Objects.requireNonNullElse(friendlyName, providerName);

        String description = toString(getProviderAdminField(userSession, providerName, "description").value);
        thing.description = Objects.requireNonNullElse(description, NO_DESCRIPTION);

        thing.selfLink = uriInfo.getBaseUriBuilder().path("v1.1").path("Things({id})")
                .resolveTemplate("id", providerName).build().toString();
        thing.datastreamsLink = uriInfo.getBaseUriBuilder().uri(thing.selfLink).path("Datastreams").build().toString();
        thing.historicalLocationsLink = uriInfo.getBaseUriBuilder().uri(thing.selfLink).path("HistoricalLocations")
                .build().toString();
        thing.locationsLink = uriInfo.getBaseUriBuilder().uri(thing.selfLink).path("Locations").build().toString();

        return thing;
    }

    private static String getProperty(GeoJsonObject location, String propName) {
        if (location instanceof Feature) {
            Feature f = (Feature) location;
            return toString(f.getProperty(propName));
        } else if (location instanceof FeatureCollection) {
            FeatureCollection fc = (FeatureCollection) location;
            return fc.getFeatures().stream().map(f -> toString(f.getProperty(propName))).filter(p -> p != null)
                    .findFirst().orElse(null);
        }
        return null;
    }

    public static Location toLocation(SensiNactSession userSession, UriInfo uriInfo, ObjectMapper mapper,
            String providerName) {
        Location location = new Location();

        ResourceDescription locationResource = getProviderAdminField(userSession, providerName, "location");

        Instant time;
        GeoJsonObject object;
        if (locationResource.value == null) {
            object = new Point(0, 0);
            time = Instant.EPOCH;
        } else {
            object = GeoJsonObject.class.isInstance(locationResource.value) ? (GeoJsonObject) locationResource.value
                    : mapper.convertValue(locationResource.value, GeoJsonObject.class);
            time = locationResource.timestamp;
        }

        location.id = String.format("%s~%s", providerName, Long.toString(time.toEpochMilli(), 16));

        String friendlyName = getProperty(object, "name");
        location.name = Objects.requireNonNullElse(friendlyName, providerName);

        String description = getProperty(object, "description");
        location.description = Objects.requireNonNullElse(description, NO_DESCRIPTION);

        location.encodingType = "application/vnd.geo+json";
        location.location = object;

        location.selfLink = uriInfo.getBaseUriBuilder().path("v1.1").path("Locations({id})")
                .resolveTemplate("id", location.id).build().toString();
        location.thingsLink = uriInfo.getBaseUriBuilder().uri(location.selfLink).path("Things").build().toString();
        location.historicalLocationsLink = uriInfo.getBaseUriBuilder().uri(location.selfLink)
                .path("HistoricalLocations").build().toString();

        return location;
    }

    public static HistoricalLocation toHistoricalLocation(SensiNactSession userSession, UriInfo uriInfo,
            String providerName) {
        HistoricalLocation historicalLocation = new HistoricalLocation();

        ResourceDescription locationResource = getProviderAdminField(userSession, providerName, "location");

        Instant time;
        if (locationResource.value == null) {
            time = Instant.EPOCH;
        } else {
            time = locationResource.timestamp;
        }

        historicalLocation.id = String.format("%s~%s", providerName, Long.toString(time.toEpochMilli(), 16));

        historicalLocation.selfLink = uriInfo.getBaseUriBuilder().path("v1.1").path("HistoricalLocations({id})")
                .resolveTemplate("id", historicalLocation.id).build().toString();
        historicalLocation.thingLink = uriInfo.getBaseUriBuilder().uri(historicalLocation.selfLink).path("Thing")
                .build().toString();
        historicalLocation.locationsLink = uriInfo.getBaseUriBuilder().uri(historicalLocation.selfLink)
                .path("Locations").build().toString();

        return historicalLocation;
    }

    private static Polygon getObservedArea(GeoJsonObject object) {

        if (object instanceof Feature) {
            object = ((Feature) object).getGeometry();
        } else if (object instanceof FeatureCollection) {
            // TODO is there a better mapping?
            object = ((FeatureCollection) object).getFeatures().stream().map(Feature::getGeometry)
                    .filter(Polygon.class::isInstance).map(Polygon.class::cast).findFirst().orElse(null);
        }
        return object instanceof Polygon ? (Polygon) object : null;
    }

    public static Datastream toDatastream(SensiNactSession userSession, UriInfo uriInfo, ResourceDescription resource) {
        Datastream datastream = new Datastream();

        datastream.id = String.format("%s~%s~%s", resource.provider, resource.service, resource.resource);

        datastream.name = toString(resource.metadata.getOrDefault("friendlyName", resource.resource));
        datastream.description = toString(resource.metadata.getOrDefault("description", NO_DESCRIPTION));

        // TODO can we make this more fine-grained
        datastream.observationType = "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Observation";

        datastream.unitOfMeasurement = Map.of("symbol", resource.metadata.get("unit"), "name",
                resource.metadata.get("sensorthings.unit.name"), "definition",
                resource.metadata.get("sensorthings.unit.definition"));

        datastream.observedArea = getObservedArea(
                (GeoJsonObject) getProviderAdminField(userSession, resource.provider, "location").value);
        datastream.properties = resource.metadata;

        datastream.selfLink = uriInfo.getBaseUriBuilder().path("v1.1").path("Sensors({id})")
                .resolveTemplate("id", datastream.id).build().toString();
        datastream.observationsLink = uriInfo.getBaseUriBuilder().uri(datastream.selfLink).path("Observations").build()
                .toString();
        datastream.observedPropertyLink = uriInfo.getBaseUriBuilder().uri(datastream.selfLink).path("ObservedProperty")
                .build().toString();
        datastream.sensorLink = uriInfo.getBaseUriBuilder().uri(datastream.selfLink).path("Sensor").build().toString();
        datastream.thingLink = uriInfo.getBaseUriBuilder().uri(datastream.selfLink).path("Thing").build().toString();

        return datastream;
    }

    public static Sensor toSensor(UriInfo uriInfo, ResourceDescription resource) {
        Sensor sensor = new Sensor();

        sensor.id = String.format("%s~%s~%s", resource.provider, resource.service, resource.resource);

        sensor.name = toString(resource.metadata.getOrDefault("friendlyName", resource.resource));
        sensor.description = toString(resource.metadata.getOrDefault("description", NO_DESCRIPTION));
        sensor.properties = resource.metadata;

        sensor.metadata = resource.metadata.getOrDefault("sensorthings.sensor.metadata", "No metadata");
        sensor.encodingType = toString(
                resource.metadata.getOrDefault("sensorthings.sensor.encodingType", "text/plain"));

        sensor.selfLink = uriInfo.getBaseUriBuilder().path("v1.1").path("Sensors({id})")
                .resolveTemplate("id", sensor.id).build().toString();
        sensor.datastreamsLink = uriInfo.getBaseUriBuilder().uri(sensor.selfLink).path("Datastreams").build()
                .toString();

        return sensor;
    }

    public static Observation toObservation(UriInfo uriInfo, ResourceDescription resource) {
        Observation observation = new Observation();

        observation.id = String.format("%s~%s~%s~s", resource.provider, resource.service, resource.resource,
                Long.toString(resource.timestamp.toEpochMilli(), 16));

        observation.resultTime = resource.timestamp;
        observation.result = resource.value;
        observation.phenomenonTime = resource.timestamp;
        observation.resultQuality = resource.metadata.get("sensorthings.observation.quality");

        observation.selfLink = uriInfo.getBaseUriBuilder().path("v1.1").path("Observations({id})")
                .resolveTemplate("id", observation.id).build().toString();
        observation.datastreamLink = uriInfo.getBaseUriBuilder().uri(observation.selfLink).path("Datastream").build()
                .toString();
        observation.featureOfInterestLink = uriInfo.getBaseUriBuilder().uri(observation.selfLink)
                .path("FeatureOfInterest").build().toString();

        return observation;
    }

    public static ObservedProperty toObservedProperty(UriInfo uriInfo, ResourceDescription resource) {
        ObservedProperty observedProperty = new ObservedProperty();

        observedProperty.id = String.format("%s~%s~%s", resource.provider, resource.service, resource.resource);

        observedProperty.name = toString(resource.metadata.getOrDefault("friendlyName", resource.resource));
        observedProperty.description = toString(resource.metadata.getOrDefault("description", NO_DESCRIPTION));
        observedProperty.properties = resource.metadata;

        observedProperty.definition = toString(
                resource.metadata.getOrDefault("sensorthings.observedproperty.definition", "No definition"));

        observedProperty.selfLink = uriInfo.getBaseUriBuilder().path("v1.1").path("ObservedProperties({id})")
                .resolveTemplate("id", observedProperty.id).build().toString();
        observedProperty.datastreamsLink = uriInfo.getBaseUriBuilder().uri(observedProperty.selfLink)
                .path("Datastreams").build().toString();

        return observedProperty;
    }

    public static FeatureOfInterest toFeatureOfInterest(SensiNactSession userSession, UriInfo uriInfo,
            ObjectMapper mapper, String providerName) {
        FeatureOfInterest featureOfInterest = new FeatureOfInterest();

        ResourceDescription locationResource = getProviderAdminField(userSession, providerName, "location");

        Instant time;
        GeoJsonObject object;
        if (locationResource.value == null) {
            object = new Point(0, 0);
            time = Instant.EPOCH;
        } else {
            object = GeoJsonObject.class.isInstance(locationResource.value) ? (GeoJsonObject) locationResource.value
                    : mapper.convertValue(locationResource.value, GeoJsonObject.class);
            time = locationResource.timestamp;
        }

        featureOfInterest.id = String.format("%s~%s", providerName, Long.toString(time.toEpochMilli(), 16));

        String friendlyName = getProperty(object, "name");
        featureOfInterest.name = Objects.requireNonNullElse(friendlyName, providerName);

        String description = getProperty(object, "description");
        featureOfInterest.description = Objects.requireNonNullElse(description, NO_DESCRIPTION);

        featureOfInterest.encodingType = "application/vnd.geo+json";
        featureOfInterest.feature = object;

        featureOfInterest.selfLink = uriInfo.getBaseUriBuilder().path("v1.1").path("FeaturesOfInterest({id})")
                .resolveTemplate("id", featureOfInterest.id).build().toString();
        featureOfInterest.observationsLink = uriInfo.getBaseUriBuilder().uri(featureOfInterest.selfLink)
                .path("Observations").build().toString();

        return featureOfInterest;
    }

    public static String extractFirstIdSegment(String id) {
        int idx = id.indexOf('~');
        if(idx < 1 || idx == id.length() - 1) {
            throw new BadRequestException("Invalid id");
        }
        return id.substring(0, idx);
    }

    public static Instant getTimestampFromId(String id) {
        int idx = id.lastIndexOf('~');
        if(idx < 0 || idx == id.length() - 1) {
            throw new BadRequestException("Invalid id");
        }
        try {
            return Instant.ofEpochMilli(Long.parseLong(id.substring(idx + 1), 16));
        } catch (Exception e) {
            throw new BadRequestException("Invalid id");
        }
    }
}
