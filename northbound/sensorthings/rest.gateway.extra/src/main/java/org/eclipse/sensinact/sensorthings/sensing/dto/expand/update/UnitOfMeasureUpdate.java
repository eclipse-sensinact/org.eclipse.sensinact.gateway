package org.eclipse.sensinact.sensorthings.sensing.dto.expand.update;

import static org.eclipse.sensinact.core.annotation.dto.DuplicateAction.UPDATE_IF_DIFFERENT;
import static org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage.Literals.UNIT_OF_MEASUREMENT;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.annotation.dto.Data;
import org.eclipse.sensinact.core.annotation.dto.Model;

import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;

public record UnitOfMeasureUpdate(@Model EClass model, @Data(onDuplicate = UPDATE_IF_DIFFERENT) String name,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT) String symbol,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT) String definition) implements SensorThingsUpdate {

    public UnitOfMeasureUpdate {
        if (model == null) {
            model = UNIT_OF_MEASUREMENT;
        }
        if (model != UNIT_OF_MEASUREMENT) {
            throw new IllegalArgumentException("The model for the provider must be " + UNIT_OF_MEASUREMENT.getName());
        }

    }

    public UnitOfMeasureUpdate(String name, String synmbol, String definition) {
        this(UNIT_OF_MEASUREMENT, name, synmbol, definition);
    }

}
