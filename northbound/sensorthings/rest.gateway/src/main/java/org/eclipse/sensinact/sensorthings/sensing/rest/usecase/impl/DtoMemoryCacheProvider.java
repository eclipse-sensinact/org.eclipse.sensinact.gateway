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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IDtoMemoryCache;

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
    Application application;

    private Map<Class<?>, IDtoMemoryCache<?>> mapRepoCaches = new ConcurrentHashMap<Class<?>, IDtoMemoryCache<?>>();

    @Override
    public IDtoMemoryCache<?> getContext(Class<?> type) {
        if (!Id.class.isAssignableFrom(type)) {
            return null;
        }
        return mapRepoCaches.computeIfAbsent(type, this::createCacheMap);
    }

    @SuppressWarnings("unchecked")
    private IDtoMemoryCache<?> createCacheMap(Class<?> type) {
        return new DtoMemoryCache<>((Class<? extends Id>) type);
    }
}
