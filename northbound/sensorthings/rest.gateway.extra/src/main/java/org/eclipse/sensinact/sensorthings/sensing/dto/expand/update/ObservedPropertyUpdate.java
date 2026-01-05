package org.eclipse.sensinact.sensorthings.sensing.dto.expand.update;

import static org.eclipse.sensinact.core.annotation.dto.DuplicateAction.UPDATE_IF_DIFFERENT;
import static org.eclipse.sensinact.core.annotation.dto.NullAction.UPDATE_IF_PRESENT;
import static org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage.Literals.SENSOR_THING_OBSERVED_PROPERTY;

import java.time.Instant;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.annotation.dto.Data;
import org.eclipse.sensinact.core.annotation.dto.Model;
import org.eclipse.sensinact.core.annotation.dto.Timestamp;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;

public record ObservedPropertyUpdate(@Model EClass model, @Data(onDuplicate = UPDATE_IF_DIFFERENT) String type,

        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String id,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String name,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String description,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String definition,
        @Timestamp Instant timestamp) implements SensorThingsUpdate {

    public ObservedPropertyUpdate {
        if (model == null) {
            model = SENSOR_THING_OBSERVED_PROPERTY;
        }
    }
}
