package org.eclipse.sensinact.sensorthings.sensing.dto.expand.update;

import static org.eclipse.sensinact.core.annotation.dto.DuplicateAction.UPDATE_IF_DIFFERENT;
import static org.eclipse.sensinact.core.annotation.dto.NullAction.UPDATE_IF_PRESENT;
import static org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage.Literals.SENSOR_THING_DEVICE;
import static org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage.Literals.DATA_STREAM_SERVICE;

import java.time.Instant;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.annotation.dto.Data;
import org.eclipse.sensinact.core.annotation.dto.Model;
import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.core.annotation.dto.Service;
import org.eclipse.sensinact.core.annotation.dto.ServiceModel;
import org.eclipse.sensinact.core.annotation.dto.Timestamp;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;

public record DatastreamUpdate(@Model EClass model, @ServiceModel EClass service, @Provider String providerId,
        @Service String serviceName, @Data(onDuplicate = UPDATE_IF_DIFFERENT) Object sensorThingsId,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String name,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String description,
        @Timestamp Instant timestamp,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) SensorUpdate sensor,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) ObservedPropertyUpdate observedProperty,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) UnitOfMeasureUpdate unit,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT) String type) implements SensorThingsUpdate {

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
            String description, Instant timestamp, SensorUpdate sensor, ObservedPropertyUpdate observedProperty,
            UnitOfMeasureUpdate unit) {
        this(SENSOR_THING_DEVICE, DATA_STREAM_SERVICE, providerId, serviceName, sensorThingsId, name, description,
                timestamp, sensor, observedProperty, unit, DATA_STREAM_SERVICE.getInstanceClass().getSimpleName());
    }

}
