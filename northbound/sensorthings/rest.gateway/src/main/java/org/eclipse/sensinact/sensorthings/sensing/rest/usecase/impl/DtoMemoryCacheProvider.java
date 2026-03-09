/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMemoryCache;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.IDtoMemoryCache;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

/**
 * Provider for Memory Dto repository
 */
@SuppressWarnings("rawtypes")
@Provider
public class DtoMemoryCacheProvider implements ContextResolver<IDtoMemoryCache> {

    @Context
    public Application application;

    private Map<Class<?>, IDtoMemoryCache<?>> mapRepoCaches = new ConcurrentHashMap<Class<?>, IDtoMemoryCache<?>>();

    @SuppressWarnings("unchecked")
    @Override
    public IDtoMemoryCache<?> getContext(Class<?> type) {
        if (type.equals(ExpandedObservation.class)) {
            return (IDtoMemoryCache<ExpandedObservation>) application.getProperties().get("cache.expanded.observation");
        }
        if (type.equals(Instant.class)) {
            return (IDtoMemoryCache<Instant>) application.getProperties().get("cache.historical.location");
        }

        return mapRepoCaches.computeIfAbsent(type, this::createCacheMap);
    }

    private IDtoMemoryCache<?> createCacheMap(Class<?> type) {
        return new DtoMemoryCache<>(type);
    }
}
