/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.twin.TimedValue;
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
import org.eclipse.sensinact.sensorthings.sensing.dto.util.RequireFieldException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DtoMapperSensorthing {

    public static Thing toThing(ProviderSnapshot provider) {
        try {
            Thing thing = DtoMapperSimple.toThing(provider, provider.getName(), null, null, null, null);
            return thing;
        } catch (RequireFieldException e) {
            return null;
        }
    }

    public static Location toLocation(ObjectMapper mapper, ProviderSnapshot provider) {
        try {
            Location location = DtoMapperSimple.toLocation(mapper, provider, null, null, null);
            return location;
        } catch (RequireFieldException e) {
            return null;
        }
    }

    public static HistoricalLocation toHistoricalLocation(ObjectMapper mapper, ProviderSnapshot provider) {
        try {
            ResourceSnapshot resource = provider.getResource(DtoMapperSimple.SERVICE_ADMIN, "location");
            if (resource != null) {
                HistoricalLocation hl = DtoMapperSimple.toHistoricalLocation(provider, Optional.of(resource.getValue()),
                        null, null, null);
                return hl;
            }
            return null;
        } catch (RequireFieldException e) {
            return null;
        }
    }

    public static Datastream toDatastream(ObjectMapper mapper, ProviderSnapshot provider) throws NotFoundException {

        try {
            Datastream datastream = DtoMapperSimple.toDatastream(provider, null, null, null, null, null);
            return datastream;
        } catch (RequireFieldException e) {
            return null;
        }
    }

    public static Datastream toDatastream(ObjectMapper mapper, ResourceSnapshot resource) throws NotFoundException {
        if (resource == null) {
            throw new NotFoundException();
        }
        if (resource.getName().equals("lastObservation")) {
            return null;
        }
        return toDatastream(mapper, resource.getService().getProvider());

    }

    public static Sensor toSensor(ResourceSnapshot resource) throws NotFoundException {
        if (resource == null) {
            throw new NotFoundException();
        }
        return toSensor(resource.getService().getProvider());

    }

    public static Sensor toSensor(ProviderSnapshot provider) throws NotFoundException {

        try {
            Sensor sensor = DtoMapperSimple.toSensor(provider, null, null);
            return sensor;
        } catch (RequireFieldException e) {
            return null;
        }
    }

    public static Observation toObservation(ObjectMapper mapper, ResourceSnapshot resource) {
        try {
            TimedValue<?> tv = resource.getValue();

            Object val = tv.getValue();
            if (val != null && val instanceof String) {
                ExpandedObservation obs = DtoMapperSimple.parseExpandObservation(mapper, val);
                String id = String.format("%s~%s", obs.id(), DtoMapperSimple.stampToId(tv.getTimestamp()));

                Observation observation = DtoMapperSimple.toObservation(id, null, null, null, obs);
                return observation;
            }
            return null;
        } catch (RequireFieldException e) {
            return null;
        }
    }

    public static ObservedProperty toObservedProperty(ProviderSnapshot provider) {

        try {
            ObservedProperty op = DtoMapperSimple.toObservedProperty(provider, null, null);
            return op;
        } catch (RequireFieldException e) {
            return null;
        }
    }

    public static ObservedProperty toObservedProperty(ResourceSnapshot resource) {
        final ProviderSnapshot provider = resource.getService().getProvider();
        return toObservedProperty(provider);

    }

    public static FeatureOfInterest toFeatureOfInterest(ProviderSnapshot provider) {
        try {
            FeatureOfInterest foi = DtoMapperSimple.toFeatureOfInterest(provider, provider.getName(), null, null);
            return foi;
        } catch (RequireFieldException e) {
            return null;
        }
    }
}
