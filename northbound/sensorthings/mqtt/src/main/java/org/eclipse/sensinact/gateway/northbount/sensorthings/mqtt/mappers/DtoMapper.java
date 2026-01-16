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
package org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.mappers;

import java.time.Instant;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.NotFoundException;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DtoMapper {

    private static Optional<? extends ResourceSnapshot> getProviderAdminField(final ProviderSnapshot provider,
            final String resource) {
        final ServiceSnapshot adminSvc = provider.getServices().stream().filter(s -> "admin".equals(s.getName()))
                .findFirst().get();
        return adminSvc.getResources().stream().filter(r -> resource.equals(r.getName())).findFirst();
    }

    public static Thing toThing(final ObjectMapper mapper, final ProviderSnapshot provider) {
        if (!DtoMapperSimple.isThing(provider)) {
            return null;
        }
        return DtoMapperSimple.toThing(provider, null);

    }

    public static Location toLocation(final ObjectMapper mapper, final ProviderSnapshot provider) {
        if (!DtoMapperSimple.isLocation(provider)) {
            return null;
        }

        return DtoMapperSimple.toLocation(mapper, provider, null);
    }

    public static HistoricalLocation toHistoricalLocation(final ObjectMapper mapper, final ProviderSnapshot provider) {
        if (!DtoMapperSimple.isThing(provider)) {
            return null;
        }
        final TimedValue<GeoJsonObject> location = DtoMapper.getLocation(provider, mapper, true);
        final Instant time;
        if (location.getTimestamp() == null) {
            time = Instant.EPOCH;
        } else {
            time = location.getTimestamp();
        }
        final String id = String.format("%s~%s", provider.getName(), Long.toString(time.toEpochMilli(), 16));
        return new HistoricalLocation(null, id, time, null, null);
    }

    public static Datastream toDatastream(final ObjectMapper mapper, final ProviderSnapshot provider)
            throws NotFoundException {
        if (!DtoMapperSimple.isDatastream(provider)) {
            return null;
        }
        return DtoMapperSimple.toDatastream(provider, null);
    }

    public static Datastream toDatastream(final ObjectMapper mapper, final ResourceSnapshot resource)
            throws NotFoundException {
        if (resource == null) {
            throw new NotFoundException();
        }

        return toDatastream(mapper, resource.getService().getProvider());

    }

    public static Sensor toSensor(final ResourceSnapshot resource) throws NotFoundException {
        if (resource == null) {
            throw new NotFoundException();
        }
        ProviderSnapshot provider = resource.getService().getProvider();
        if (!DtoMapperSimple.isDatastream(provider)) {
            return null;
        }

        return DtoMapperSimple.toSensor(provider, null);

    }

    public static Observation toObservation(final ResourceSnapshot resource, final TimedValue<?> tv) {
        final TimedValue<?> t = tv != null ? tv : resource.getValue();
        ProviderSnapshot provider = resource.getService().getProvider();
        if (!DtoMapperSimple.isDatastream(provider)) {
            return null;
        }
        return DtoMapperSimple.toObservation(provider.getName(), t, null);
    }

    public static Observation toObservation(final String provider, final TimedValue<?> tv) {
        return DtoMapperSimple.toObservation(provider, tv, null);

    }

    public static ObservedProperty toObservedProperty(final ProviderSnapshot provider) {
        if (!DtoMapperSimple.isDatastream(provider)) {
            return null;
        }
        return DtoMapperSimple.toObservedProperty(provider, null);
    }

    public static ObservedProperty toObservedProperty(final ResourceSnapshot resource) {
        return toObservedProperty(resource.getService().getProvider());
    }

    private static TimedValue<GeoJsonObject> getLocation(final ProviderSnapshot provider, final ObjectMapper mapper,
            final boolean allowNull) {

        final Optional<? extends ResourceSnapshot> locationResource = DtoMapper.getProviderAdminField(provider,
                "location");

        final Instant time;
        final Object rawValue;
        if (locationResource.isEmpty()) {
            time = Instant.EPOCH;
            rawValue = null;
        } else {
            final TimedValue<?> timedValue = locationResource.get().getValue();
            if (timedValue == null) {
                time = Instant.EPOCH;
                rawValue = null;
            } else {
                time = timedValue.getTimestamp() != null ? timedValue.getTimestamp() : Instant.EPOCH;
                rawValue = timedValue.getValue();
            }
        }

        final GeoJsonObject parsedLocation;
        if (rawValue == null) {
            if (allowNull) {
                parsedLocation = null;
            } else {
                parsedLocation = new Point(Coordinates.EMPTY, null, null);
            }
        } else if (rawValue instanceof GeoJsonObject) {
            parsedLocation = (GeoJsonObject) rawValue;
        } else if (rawValue instanceof String) {
            try {
                parsedLocation = mapper.readValue((String) rawValue, GeoJsonObject.class);
            } catch (final JsonProcessingException ex) {
                if (allowNull) {
                    return null;
                }
                throw new RuntimeException("Invalid resource location content", ex);
            }
        } else {
            parsedLocation = mapper.convertValue(rawValue, GeoJsonObject.class);
        }

        return new DefaultTimedValue<>(parsedLocation, time);
    }

    public static FeatureOfInterest toFeatureOfInterest(final ProviderSnapshot provider) {
        if (!DtoMapperSimple.isDatastream(provider)) {
            return null;
        }
        final ServiceSnapshot serviceSnapshot = DtoMapperSimple.getDatastreamService(provider);
        final ExpandedObservation lastObservation = DtoMapperSimple.getResourceField(serviceSnapshot, "lastObservation",
                ExpandedObservation.class);
        return DtoMapperSimple.toFeatureOfInterest(provider, lastObservation, null);

    }
}
