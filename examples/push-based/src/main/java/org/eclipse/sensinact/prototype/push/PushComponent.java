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

@Component
public class PushComponent {

    @Reference
    private TypedEventBus bus;

    @Reference
    private PrototypePush sensiNact;

    /**
     * Message coming in from the sensor
     */
    public void onMessage(String message) {
        // Create the DTO
        _01_SimpleDTO dto = toDTO(message);

        // Send the dto data to sensiNact core somehow?

        // e.g. Typed Events
        bus.deliver(EventTopicNames.DTO_UPDATE_EVENTS, dto);

        // e.g. Direct to core
        sensiNact.pushUpdate(dto);
    }

    _01_SimpleDTO toDTO(String message) {
        _01_SimpleDTO dto = new _01_SimpleDTO();
        // Populate the DTO
        return dto;
    }

}
