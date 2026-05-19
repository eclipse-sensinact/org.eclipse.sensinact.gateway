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
import static org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage.Literals.SENSOR_THING_OBSERVED_PROPERTY;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.annotation.dto.Data;
import org.eclipse.sensinact.core.annotation.dto.Model;
import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.core.annotation.dto.Service;
import org.eclipse.sensinact.core.annotation.dto.Timestamp;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;

/**
 * update record for datastream
 */
@Service("admin")
public record ObservedPropertyUpdate(@Model EClass model, @Provider String providerId,
        @Service("observedproperty") @Data(onDuplicate = UPDATE_IF_DIFFERENT) Object id,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT) String friendlyName,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT) String description, @Timestamp Instant timestamp,
        @Service("observedproperty") @Data(onDuplicate = UPDATE_IF_DIFFERENT) List<String> datastreamIds,

// observed property
        @Service("observedproperty") @Data(onDuplicate = UPDATE_IF_DIFFERENT) String observedPropertyDefinition,
        @Service("observedproperty") @Data(onDuplicate = UPDATE_IF_DIFFERENT) Map<String, Object> observedPropertyProperties

) implements SensorThingsUpdate {

    public ObservedPropertyUpdate {
        if (model == null) {
            model = SENSOR_THING_OBSERVED_PROPERTY;
        }
        if (model != SENSOR_THING_OBSERVED_PROPERTY) {
            throw new IllegalArgumentException(
                    "The model for the provider must be " + SENSOR_THING_OBSERVED_PROPERTY.getName());
        }

    }

    public ObservedPropertyUpdate(String providerId, Object id, String name, String description, Instant timestamp,
            List<String> datastreamIds,
            // observed property
            String observedPropertyDefinition, Map<String, Object> observedPropertyProperties) {
        this(SENSOR_THING_OBSERVED_PROPERTY, providerId, id, name, description, timestamp, datastreamIds,
                observedPropertyDefinition, observedPropertyProperties);
    }

}
