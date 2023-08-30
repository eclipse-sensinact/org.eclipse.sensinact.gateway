/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.prototype.generic;

import org.eclipse.sensinact.core.push.EventTopicNames;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.typedevent.TypedEventBus;

/**
 * This components show to push an update event to sensiNact using a
 * {@link GenericDto} object.
 */
@Component
public class GenericPushComponent {

    /**
     * Service to send the update event through the typed event bus
     */
    @Reference
    private TypedEventBus bus;

    /**
     * Service to send the update event through the update DTO handler
     */
    @Reference
    private DataUpdate sensiNact;

    /**
     * Message coming in from the sensor, just like a custom model
     */
    public void onMessage(String message) {
        // Create the DTO based on sensor data
        GenericDto dto = toDTO(message);

        // Send the DTO to sensiNact core, either via:
        // ... Typed Events
        bus.deliver(EventTopicNames.GENERIC_UPDATE_EVENTS, dto);

        // ... Direct to core using the DTO handler
        sensiNact.pushUpdate(dto);
    }

    /**
     * Internal method to convert sensor data into an update DTO
     *
     * @param message Sensor data
     * @return A resource value/metadata update DTO
     */
    GenericDto toDTO(String message) {
        GenericDto dto = new GenericDto();
        // Populate the DTO: model, provider, service, resource, value and timestamp
        return dto;
    }

}
