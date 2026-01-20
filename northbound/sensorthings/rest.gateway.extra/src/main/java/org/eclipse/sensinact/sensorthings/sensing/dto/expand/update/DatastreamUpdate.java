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
package org.eclipse.sensinact.sensorthings.sensing.dto.expand.update;

import static org.eclipse.sensinact.core.annotation.dto.DuplicateAction.UPDATE_IF_DIFFERENT;
import static org.eclipse.sensinact.core.annotation.dto.NullAction.UPDATE_IF_PRESENT;
import static org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage.Literals.SENSOR_THING_DATASTREAM;
import java.time.Instant;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.annotation.dto.Data;
import org.eclipse.sensinact.core.annotation.dto.Model;
import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.core.annotation.dto.Service;
import org.eclipse.sensinact.core.annotation.dto.Timestamp;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;

/**
 * update record for datastream
 */
@Service("admin")
public record DatastreamUpdate(@Model EClass model, @Provider String providerId,
        @Service("datastream") @Data(onDuplicate = UPDATE_IF_DIFFERENT) Object id,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT) String friendlyName,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT) String description, @Timestamp Instant timestamp,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT) GeoJsonObject location,
        @Service("datastream") @Data(onDuplicate = UPDATE_IF_DIFFERENT) String thingId,
// sensor
        @Service("datastream") @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String sensorId,
        @Service("datastream") @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String sensorName,
        @Service("datastream") @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String sensorDescription,
        @Service("datastream") @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String sensorEncodingType,
        @Service("datastream") @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) Object sensorMetadata,
        @Service("datastream") @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) Map<String, Object> sensorProperties,

// observed property
        @Service("datastream") @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String observedPropertyId,
        @Service("datastream") @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String observedPropertyName,
        @Service("datastream") @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String observedPropertyDescription,
        @Service("datastream") @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String observedPropertyDefinition,
        @Service("datastream") @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) Map<String, Object> observedPropertyProperties,
// unit
        @Service("datastream") @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String unitName,
        @Service("datastream") @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String unitSymbol,
        @Service("datastream") @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String unitDefinition,
// observation
        @Service("datastream") @Data(onDuplicate = UPDATE_IF_DIFFERENT) String lastObservation

) implements SensorThingsUpdate {

    public DatastreamUpdate {
        if (model == null) {
            model = SENSOR_THING_DATASTREAM;
        }
        if (model != SENSOR_THING_DATASTREAM) {
            throw new IllegalArgumentException(
                    "The model for the provider must be " + SENSOR_THING_DATASTREAM.getName());
        }

    }

    public DatastreamUpdate(String providerId, Object id, String name, String description, Instant timestamp,
            GeoJsonObject observedArea, String thingId, String sensorId, String sensorName, String sensorDescription,
            String sensorEncodingType, Object sensorMetadata, Map<String, Object> sensorProperties,
            // observed property
            String observedPropertyId, String observedPropertyName, String observedPropertyDescription,
            String observedPropertyDefinition, Map<String, Object> observedPropertyProperties,
            // unit
            String unitName, String unitSymbol, String unitDefinition, String lastObservsation) {
        this(SENSOR_THING_DATASTREAM, providerId, id, name, description, timestamp, observedArea, thingId, sensorId,
                sensorName, sensorDescription, sensorEncodingType, sensorMetadata, sensorProperties, observedPropertyId,
                observedPropertyName, observedPropertyDescription, observedPropertyDefinition,
                observedPropertyProperties, unitName, unitSymbol, unitDefinition, lastObservsation);
    }

}
