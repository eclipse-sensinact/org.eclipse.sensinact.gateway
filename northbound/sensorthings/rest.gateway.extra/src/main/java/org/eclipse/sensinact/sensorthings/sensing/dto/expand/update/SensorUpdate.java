package org.eclipse.sensinact.sensorthings.sensing.dto.expand.update;

import static org.eclipse.sensinact.core.annotation.dto.DuplicateAction.UPDATE_IF_DIFFERENT;
import static org.eclipse.sensinact.core.annotation.dto.NullAction.UPDATE_IF_PRESENT;
import static org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsPackage.Literals.DATA_STREAM_SERVICE__SENSOR;
import static org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsPackage.Literals.SENSOR_THINGS_DEVICE;

import java.time.Instant;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.annotation.dto.Data;
import org.eclipse.sensinact.core.annotation.dto.Model;
import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.core.annotation.dto.Service;
import org.eclipse.sensinact.core.annotation.dto.ServiceModel;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;

public record SensorUpdate(@Model EClass model, @ServiceModel EClass service, @Provider String providerId,
        @Service String serviceName, @Data(onDuplicate = UPDATE_IF_DIFFERENT) Object sensorThingsId,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String name,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String description)
        implements SensorThingsUpdate {
    public SensorUpdate {
        if (model == null) {
            model = SENSOR_THINGS_DEVICE;
        }
        if (model != SENSOR_THINGS_DEVICE) {
            throw new IllegalArgumentException("The model for the provider must be " + SENSOR_THINGS_DEVICE.getName());
        }
        if (service == null) {
            service = DATA_STREAM_SERVICE__SENSOR; // need new EMF model
        }
        if (service != DATA_STREAM_SERVICE__SENSOR) {
            throw new IllegalArgumentException("The model for the datastream must be " + DATA_STREAM_SERVICE.getName());
        }

    }

    public SensorUpdate(String providerId, String serviceName, Object sensorThingsId, String name, String description,
            Object latestObservation, Instant timestamp, Map<String, Object> observationParameters, String unit,
            Map<String, Object> unitMetadata, String sensor, Map<String, Object> sensorMetadata,
            String observedProperty, Map<String, Object> observedPropertyMetadata) {
        this(SENSOR_THINGS_DEVICE, null, providerId, serviceName, sensorThingsId, name, description);
    }
}