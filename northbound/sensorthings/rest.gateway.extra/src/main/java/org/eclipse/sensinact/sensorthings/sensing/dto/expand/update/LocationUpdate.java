package org.eclipse.sensinact.sensorthings.sensing.dto.expand.update;

import static org.eclipse.sensinact.core.annotation.dto.DuplicateAction.UPDATE_IF_DIFFERENT;
import static org.eclipse.sensinact.core.annotation.dto.NullAction.UPDATE_IF_PRESENT;
import static org.eclipse.sensinact.sensorthings.models.sensorthings.extended.SensorthingsExtendedPackage.Literals.SENSORTHING_DEVICE_EXTENDED;

import java.time.Instant;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.annotation.dto.Data;
import org.eclipse.sensinact.core.annotation.dto.Model;
import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.core.annotation.dto.ServiceModel;
import org.eclipse.sensinact.core.annotation.dto.Timestamp;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;

/**
 * Update record for the Location of a SensorThing / Device
 */
public record LocationUpdate(@Model EClass model, @ServiceModel EClass service, @Provider String providerId,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT) Object sensorThingsId,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) Double latitude,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) Double longitude,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) Double altitude,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String description,
        @Timestamp Instant timestamp) implements SensorThingsUpdate {

    public LocationUpdate {
        if (model == null) {
            model = SENSORTHING_DEVICE_EXTENDED;
        }
        if (model != SENSORTHING_DEVICE_EXTENDED) {
            throw new IllegalArgumentException(
                    "The model for the provider must be " + SENSORTHING_DEVICE_EXTENDED.getName());
        }
        service = null; // Location is part of the Thing, not a separate service
    }

    public LocationUpdate(String providerId, Object sensorThingsId, Double latitude, Double longitude, Double altitude,
            String description, Instant timestamp) {
        this(SENSORTHING_DEVICE_EXTENDED, null, providerId, sensorThingsId, latitude, longitude, altitude, description,
                timestamp);
    }
}
