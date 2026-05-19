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
package org.eclipse.sensinact.sensorthings.sensing.dto.util;

import java.time.Instant;

import org.osgi.service.component.annotations.Component;

@Component(service = IDtoMemoryCache.class, property = { "cache.type=historical-location" })
public class DtoMemoryCacheHistoricalLocation extends DtoMemoryCache<Instant> {

    public DtoMemoryCacheHistoricalLocation() {
        super(Instant.class);
    }

}
