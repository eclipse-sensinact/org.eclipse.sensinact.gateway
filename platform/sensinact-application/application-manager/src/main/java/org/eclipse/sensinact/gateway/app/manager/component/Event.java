/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
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
