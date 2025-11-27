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
package org.eclipse.sensinact.sensorthings.sensing.dto.expand;

import static org.eclipse.sensinact.core.annotation.dto.DuplicateAction.UPDATE_IF_DIFFERENT;
import static org.eclipse.sensinact.core.annotation.dto.MapAction.USE_KEYS_AS_FIELDS;
import static org.eclipse.sensinact.core.annotation.dto.NullAction.UPDATE_IF_PRESENT;
import static org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsPackage.Literals.DATA_STREAM_SERVICE;
import static org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsPackage.Literals.SENSOR_THINGS_DEVICE;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.annotation.dto.Data;
import org.eclipse.sensinact.core.annotation.dto.Metadata;
import org.eclipse.sensinact.core.annotation.dto.Model;
import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.core.annotation.dto.Resource;
import org.eclipse.sensinact.core.annotation.dto.Service;
import org.eclipse.sensinact.core.annotation.dto.ServiceModel;
import org.eclipse.sensinact.core.annotation.dto.Timestamp;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExpandedDataStream extends Datastream {

    public record DatastreamUpdate(@Model EClass model, @ServiceModel EClass service, @Provider String providerId,
            @Service String serviceName, @Data(onDuplicate = UPDATE_IF_DIFFERENT) Object sensorThingsId,
            @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String name,
            @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String description,
            @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) Object latestObservation,
            @Timestamp Instant timestamp, @Resource("latestObservation") @Metadata(onMap = {
                    USE_KEYS_AS_FIELDS }) Map<String, Object> observationParameters,
            @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String unit,
            @Resource("unit") @Metadata(onMap = { USE_KEYS_AS_FIELDS }) Map<String, Object> unitMetadata,
            @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) Object sensor,
            @Resource("sensor") @Metadata(onMap = { USE_KEYS_AS_FIELDS }) Map<String, Object> sensorMetadata,
            @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) Object observedProperty,
            @Resource("observedProperty") @Metadata(onMap = {
                    USE_KEYS_AS_FIELDS }) Map<String, Object> observedPropertyMetadata)
            implements SensorThingsUpdate{
        public DatastreamUpdate {
            if (model == null) {
                model = SENSOR_THINGS_DEVICE;
            }
            if (model != SENSOR_THINGS_DEVICE) {
                throw new IllegalArgumentException(
                        "The model for the provider must be " + SENSOR_THINGS_DEVICE.getName());
            }
            if (service == null) {
                service = DATA_STREAM_SERVICE;
            }
            if (service != DATA_STREAM_SERVICE) {
                throw new IllegalArgumentException(
                        "The model for the datastream must be " + DATA_STREAM_SERVICE.getName());
            }
        }

        public DatastreamUpdate(String providerId, String serviceName, Object sensorThingsId, String name,
                String description, Object latestObservation, Instant timestamp,
                Map<String, Object> observationParameters, String unit, Map<String, Object> unitMetadata, Object sensor,
                Map<String, Object> sensorMetadata, Object observedProperty,
                Map<String, Object> observedPropertyMetadata) {
            this(SENSOR_THINGS_DEVICE, DATA_STREAM_SERVICE, providerId, serviceName, sensorThingsId, name, description,
                    latestObservation, timestamp, observationParameters, unit, unitMetadata, sensor, sensorMetadata,
                    observedProperty, observedPropertyMetadata);
        }
    }

    @JsonProperty("Observations")
    public List<Observation> observations;

    @JsonProperty("ObservedProperty")
    public ObservedProperty observedProperty;

    @JsonProperty("Sensor")
    public Sensor sensor;

    @JsonProperty("Observations@iot.nextLink")
    public String nextObservation;

}
