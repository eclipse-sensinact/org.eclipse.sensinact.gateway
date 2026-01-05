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
import static org.eclipse.sensinact.core.annotation.dto.MapAction.USE_KEYS_AS_FIELDS;
import static org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage.Literals.SENSOR_THING_DEVICE;

import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.annotation.dto.Data;
import org.eclipse.sensinact.core.annotation.dto.Metadata;
import org.eclipse.sensinact.core.annotation.dto.Model;
import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.core.annotation.dto.Resource;
import org.eclipse.sensinact.core.annotation.dto.Service;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;

/**
 * update record for thing
 */
@Service("admin")
public record ThingUpdate(@Model EClass model, @Provider String providerId,
        @Service("thing") @Data(onDuplicate = UPDATE_IF_DIFFERENT) String name,
        @Service("thing") @Data(onDuplicate = UPDATE_IF_DIFFERENT) String description,
        @Service("thing") @Resource("id") @Data(onDuplicate = UPDATE_IF_DIFFERENT) Object thingId,
        @Service("thing") @Resource("id") @Metadata(onMap = {
                USE_KEYS_AS_FIELDS }) Map<String, Object> properties,
        // link to thing
        @Service("thing") @Resource("locationIds") @Data(onDuplicate = UPDATE_IF_DIFFERENT) List<String> locationIds,
        @Service("thing") @Resource("datastreamIds") @Data(onDuplicate = UPDATE_IF_DIFFERENT) List<String> datastreamIds)
        implements SensorThingsUpdate{

    public ThingUpdate {
        if (model == null) {
            model = SENSOR_THING_DEVICE;
        }
        if (model != SENSOR_THING_DEVICE) {
            throw new IllegalArgumentException("The model for the provider must be " + SENSOR_THING_DEVICE.getName());
        }
        if (properties == null) {
            properties = Map.of();
        }

    }

    public ThingUpdate(String providerId, String friendlyName, String description, Object thingId,
            Map<String, Object> properties, List<String> locationIds, List<String> datastreamIds) {
        this(SENSOR_THING_DEVICE, providerId, friendlyName, description, thingId, properties, locationIds,
                datastreamIds);
    }

}
