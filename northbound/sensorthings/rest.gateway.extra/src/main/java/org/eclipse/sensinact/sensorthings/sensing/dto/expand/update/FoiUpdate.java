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
import static org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage.Literals.SENSOR_THING_FOI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.annotation.dto.Data;
import org.eclipse.sensinact.core.annotation.dto.Model;
import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.core.annotation.dto.Service;
import org.eclipse.sensinact.core.annotation.dto.Timestamp;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;

/**
 * update record for datastream
 */
@Service("admin")
public record FoiUpdate(@Model EClass model, @Provider String providerId,
        @Service("foi") @Data(onDuplicate = UPDATE_IF_DIFFERENT) Object id,
        @Service("foi") @Data(onDuplicate = UPDATE_IF_DIFFERENT) String encodingType,
        @Service("foi") @Data(onDuplicate = UPDATE_IF_DIFFERENT) Map<String, Object> properties,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT) String friendlyName,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT) String description, @Timestamp Instant timestamp,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT) GeoJsonObject location,
        @Service("foi") @Data(onDuplicate = UPDATE_IF_DIFFERENT) List<String> datastreamIds,
        @Service("foi") @Data(onDuplicate = UPDATE_IF_DIFFERENT) boolean hasObs

) implements SensorThingsUpdate {

    public FoiUpdate {
        if (model == null) {
            model = SENSOR_THING_FOI;
        }
        if (model != SENSOR_THING_FOI) {
            throw new IllegalArgumentException("The model for the provider must be " + SENSOR_THING_FOI.getName());
        }

    }

    public FoiUpdate(String providerId, Object id, String name, String description, String encodingType,
            Map<String, Object> properties, Instant timestamp, GeoJsonObject location, List<String> datastreamIds,
            boolean hasObs) {
        this(SENSOR_THING_FOI, providerId, id, encodingType, properties, name, description, timestamp, location,
                datastreamIds, hasObs);
    }

}
