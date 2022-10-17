/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.manager.component;

import org.eclipse.sensinact.gateway.app.api.function.DataItf;

import java.util.List;
import java.util.UUID;

/**
 * @author Rémi Druilhe
 */
public class Event {
    private final UUID uuid;
    private final DataItf data;
    private final List<String> route;

    public Event(UUID uuid, DataItf data, List<String> route) {
        this.uuid = uuid;
        this.data = data;
        this.route = route;
    }

    public UUID getUuid() {
        return uuid;
    }

    public DataItf getData() {
        return data;
    }

    public List<String> getRoute() {
        return route;
    }
}
