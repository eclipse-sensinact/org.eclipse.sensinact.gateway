package org.eclipse.sensinact.sensorthings.sensing.dto.expand.update;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static org.eclipse.sensinact.core.annotation.dto.DuplicateAction.UPDATE_IF_DIFFERENT;
import static org.eclipse.sensinact.core.annotation.dto.MapAction.USE_KEYS_AS_FIELDS;
import static org.eclipse.sensinact.core.annotation.dto.NullAction.UPDATE_IF_PRESENT;
import static org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage.Literals.SENSOR_THING_OBSERVATION_SERVICE;
import static org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage.Literals.DATA_STREAM_SERVICE;
import static org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage.Literals.SENSOR_THING_DEVICE;

import java.time.Instant;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.annotation.dto.Data;
import org.eclipse.sensinact.core.annotation.dto.Metadata;
import org.eclipse.sensinact.core.annotation.dto.Model;
import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.core.annotation.dto.Service;
import org.eclipse.sensinact.core.annotation.dto.ServiceModel;
import org.eclipse.sensinact.core.annotation.dto.Timestamp;
import org.eclipse.sensinact.sensorthings.sensing.dto.TimeInterval;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ObservationUpdate(@Model EClass model, @ServiceModel EClass service, @Provider Object providerId,
        @Service String serviceName, @Data(onDuplicate = UPDATE_IF_DIFFERENT) Object id,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) Object datastreamId,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) Instant phenomenonTime,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) Instant resultTime,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) Object result, Object resultQuality,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) TimeInterval validTime,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) Map<String, Object> parameters,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT) String type) implements SensorThingsUpdate {

    public ObservationUpdate {
        if (model == null) {
            model = SENSOR_THING_DEVICE;
        }
        if (model != SENSOR_THING_DEVICE) {
            throw new IllegalArgumentException("The model for the provider must be " + SENSOR_THING_DEVICE.getName());
        }
        if (service == null) {
            service = SENSOR_THING_OBSERVATION_SERVICE;
        }
        if (service != SENSOR_THING_OBSERVATION_SERVICE) {
            throw new IllegalArgumentException("The model for the datastream must be " + DATA_STREAM_SERVICE.getName());
        }
    }

    public ObservationUpdate(Object providerId, String serviceName, Object sensorThingsId, Object datastreamId,
            Instant phenomenonTime, Instant resultTime, Object result, Object resultQuality, TimeInterval validTime,
            Map<String, Object> parameters) {
        this(DATA_STREAM_SERVICE, SENSOR_THING_OBSERVATION_SERVICE, providerId, serviceName, sensorThingsId,
                datastreamId, phenomenonTime, resultTime, result, resultQuality, validTime, parameters,
                DATA_STREAM_SERVICE.getInstanceClass().getSimpleName());
    }

}
