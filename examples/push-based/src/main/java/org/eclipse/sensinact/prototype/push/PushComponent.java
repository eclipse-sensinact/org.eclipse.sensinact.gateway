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
package org.eclipse.sensinact.prototype.push;

import org.eclipse.sensinact.core.push.EventTopicNames;
import org.eclipse.sensinact.core.push.PrototypePush;
import org.eclipse.sensinact.prototype.push.dto._01_SimpleDTO;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.typedevent.TypedEventBus;

/**
 * This components show to push an update event to sensiNact using a custom DTO.
 */
@Component
public class PushComponent {

    /**
     * Service to send the update event through the typed event bus
     */
    @Reference
    private TypedEventBus bus;

    /**
     * Service to send the update event through the update DTO handler
     */
    @Reference
    private PrototypePush sensiNact;

    /**
     * Message coming in from the sensor
     */
    public void onMessage(String message) {
        // Create the DTO
        _01_SimpleDTO dto = toDTO(message);

        // Send the DTO to sensiNact core, either via:
        // ... Typed Events
        bus.deliver(EventTopicNames.DTO_UPDATE_EVENTS, dto);

        // ... Direct to core using the DTO handler
        sensiNact.pushUpdate(dto);
    }

    /**
     * Converts the sensor data into an update DTO
     *
     * @param message Sensor data
     * @return A custom DTO
     */
    _01_SimpleDTO toDTO(String message) {
        _01_SimpleDTO dto = new _01_SimpleDTO();
        // Populate the DTO: model, provider, service, resource, value and timestamp
        return dto;
    }

}
