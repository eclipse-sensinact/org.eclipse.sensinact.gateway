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
import static org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage.Literals.SENSOR_THING_DEVICE;
import static org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage.Literals.DATA_STREAM_SERVICE;

import java.time.Instant;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.annotation.dto.Data;
import org.eclipse.sensinact.core.annotation.dto.Model;
import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.core.annotation.dto.Service;
import org.eclipse.sensinact.core.annotation.dto.ServiceModel;
import org.eclipse.sensinact.core.annotation.dto.Timestamp;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.sensorthings.sensing.dto.TimeInterval;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;

/**
 * update record for datastream
 */
public record DatastreamUpdate(@Model EClass model, @ServiceModel EClass service, @Provider String providerId,
        @Service String serviceName, @Data(onDuplicate = UPDATE_IF_DIFFERENT) Object sensorThingId,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT) String name,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT) String description, @Timestamp Instant timestamp,
        // sensor
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String sensorId,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String sensorName,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String sensorDescription,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String sensorEncodingType,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) Object sensorMetadata,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) Map<String, Object> sensorProperties,

        // observed property
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String observedPropertyId,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String observedPropertyName,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String observedPropertyDescription,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String observedPropertyDefinition,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) Map<String, Object> observedPropertyProperties,

        // unit

        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String unitName,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String unitSymbol,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String unitDefinition,
        // observation

        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String observationId,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) Object observationResult,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) Instant observationPhenomenonTime,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) Instant observationResultTime,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) TimeInterval observationValidTime,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) Object observationResultQuality,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) Map<String, Object> observationParameters,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) Map<String, Object> observationProperties,

        // observation feature of interest
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String observationFoiId,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String observationFoiName,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String observationFoiDescription,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String observationFoiEncodingType,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) GeoJsonObject observationFoiFeature

) implements SensorThingsUpdate {

    public DatastreamUpdate {
        if (model == null) {
            model = SENSOR_THING_DEVICE;
        }
        if (model != SENSOR_THING_DEVICE) {
            throw new IllegalArgumentException("The model for the provider must be " + SENSOR_THING_DEVICE.getName());
        }
        if (service == null) {
            service = DATA_STREAM_SERVICE;
        }
        if (service != DATA_STREAM_SERVICE) {
            throw new IllegalArgumentException("The model for the datastream must be " + DATA_STREAM_SERVICE.getName());
        }

    }

    public DatastreamUpdate(String providerId, String serviceName, Object sensorThingsId, String name,
            String description, Instant timestamp, String sensorId, String sensorName, String sensorDescription,
            String sensorEncodingType, Object sensorMetadata, Map<String, Object> sensorProperties,
            // observed property
            String observedPropertyId, String observedPropertyName, String observedPropertyDescription,
            String observedPropertyDefinition, Map<String, Object> observedPropertyProperties,
            // unit
            String unitName, String unitSymbol, String unitDefinition, String obsId, Object obsResult,
            Instant obsPheTime, Instant obsResTime, TimeInterval obsValidTime, Object obsResultQuality,
            Map<String, Object> obsParameters, Map<String, Object> obsProperties, String obsFoiId, String obsFoiName,
            String obsFoiDescription, String obsFoiEncodingType, GeoJsonObject obsFoiFeature) {
        this(SENSOR_THING_DEVICE, DATA_STREAM_SERVICE, providerId, serviceName, sensorThingsId, name, description,
                timestamp, sensorId, sensorName, sensorDescription, sensorEncodingType, sensorMetadata,
                sensorProperties, observedPropertyId, observedPropertyName, observedPropertyDescription,
                observedPropertyDefinition, observedPropertyProperties, unitName, unitSymbol, unitDefinition, obsId,
                obsResult, obsPheTime, obsResTime, obsValidTime, obsResultQuality, obsParameters, obsProperties,
                obsFoiId, obsFoiName, obsFoiDescription, obsFoiEncodingType, obsFoiFeature);
    }

}
