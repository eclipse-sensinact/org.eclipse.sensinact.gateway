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
package org.eclipse.sensinact.sensorthings.sensing.rest.impl;

import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.IDtoMemoryCache;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensorthings.DtoMapper;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
public class DtoMapperProvider implements ContextResolver<DtoMapper> {
    @Context
    Application application;

    DtoMapper dtoMapper = null;

    @SuppressWarnings("unchecked")
    @Override
    public DtoMapper getContext(Class<?> type) {
        if (dtoMapper == null) {
            String historyProvider = (String) application.getProperties().get("sensinact.history.provider");
            int maxResult = (int) application.getProperties().get("sensinact.history.result.limit");
            IDtoMemoryCache<ExpandedObservation> cacheObs = (IDtoMemoryCache<ExpandedObservation>) application
                    .getProperties().get("cache.expanded.observation");
            dtoMapper = new DtoMapper(historyProvider, maxResult, cacheObs);
        }
        return dtoMapper;
    }

}
