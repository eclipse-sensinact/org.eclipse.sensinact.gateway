package org.eclipse.sensinact.sensorthings.sensing.dto.expand.update;

import static org.eclipse.sensinact.core.annotation.dto.DuplicateAction.UPDATE_IF_DIFFERENT;
import static org.eclipse.sensinact.core.annotation.dto.NullAction.UPDATE_IF_PRESENT;
import static org.eclipse.sensinact.sensorthings.models.sensorthings.extended.SensorthingsExtendedPackage.Literals.DATA_STREAM_SERVICE_EXTENDED;
import static org.eclipse.sensinact.sensorthings.models.sensorthings.extended.SensorthingsExtendedPackage.Literals.SENSORTHING_DEVICE_EXTENDED;

import java.time.Instant;
import java.util.List;

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
        @Data(onDuplicate = UPDATE_IF_DIFFERENT) SensorUpdate sensor,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT) ObservedPropertyUpdate observedProperty,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT) ObservationUpdate latestObservation,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT) List<ObservationUpdate> observations, @Timestamp Instant timestamp)
        implements SensorThingsUpdate {

    public DatastreamUpdate {
        if (model == null) {
            model = SENSORTHING_DEVICE_EXTENDED;
        }
        if (model != SENSORTHING_DEVICE_EXTENDED) {
            throw new IllegalArgumentException(
                    "The model for the provider must be " + SENSORTHING_DEVICE_EXTENDED.getName());
        }
        if (service == null) {
            service = DATA_STREAM_SERVICE_EXTENDED;
        }
        if (service != DATA_STREAM_SERVICE_EXTENDED) {
            throw new IllegalArgumentException(
                    "The model for the datastream must be " + DATA_STREAM_SERVICE_EXTENDED.getName());
        }
    }

    public DatastreamUpdate(String providerId, String serviceName, Object sensorThingsId, String name,
            String description, SensorUpdate sensor, ObservedPropertyUpdate observedProperty,
            ObservationUpdate latestObservation, List<ObservationUpdate> observations, Instant timestamp) {
        this(SENSORTHING_DEVICE_EXTENDED, DATA_STREAM_SERVICE_EXTENDED, providerId, serviceName, sensorThingsId, name,
                description, sensor, observedProperty, latestObservation, observations, timestamp);
    }
}
