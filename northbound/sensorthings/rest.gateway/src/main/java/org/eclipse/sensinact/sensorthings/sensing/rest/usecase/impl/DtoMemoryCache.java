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
package org.eclipse.sensinact.sensorthings.sensing.rest.usecase.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.sensinact.sensorthings.sensing.rest.access.IDtoMemoryCache;

public class DtoMemoryCache<M> implements IDtoMemoryCache<M> {

    Map<String, M> dtoById = new ConcurrentHashMap<String, M>();

    private Class<M> type;

    public DtoMemoryCache(Class<M> type) {
        this.type = type;
    }

    public void addDto(String sensorId, M sensor) {
        dtoById.put(sensorId, sensor);

    }

    public void removeDto(String sensorId) {
        dtoById.remove(sensorId);
    }

    public M getDto(String sensorId) {
        return dtoById.get(sensorId);
    }

    @Override
    public Class<M> getType() {
        return type;
    }

    @Override
    public List<M> values() {
        return dtoById.values().stream().toList();
    }
}
