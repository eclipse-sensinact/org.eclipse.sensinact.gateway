package org.eclipse.sensinact.sensorthings.sensing.dto.expand.update;

import static org.eclipse.sensinact.core.annotation.dto.DuplicateAction.UPDATE_IF_DIFFERENT;
import static org.eclipse.sensinact.sensorthings.models.sensorthings.extended.SensorthingsExtendedPackage.Literals.OBSERVATION_EXTENDED;

import java.time.Instant;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.annotation.dto.Data;
import org.eclipse.sensinact.core.annotation.dto.Model;
import org.eclipse.sensinact.core.annotation.dto.Timestamp;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;

public record ObservationUpdate(@Model EClass model, @Data(onDuplicate = UPDATE_IF_DIFFERENT) String id,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT) Object result,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT) Instant phenomenonTime,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT) FeatureOfInterestUpdate featureOfInterest,
        @Timestamp Instant timestamp) implements SensorThingsUpdate {

    public ObservationUpdate {
        if (model == null) {
            model = OBSERVATION_EXTENDED;
        }
    }
}
