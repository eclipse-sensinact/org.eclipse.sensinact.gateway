package org.eclipse.sensinact.sensorthings.sensing.dto.util;

import java.time.Instant;

import org.osgi.service.component.annotations.Component;

@Component(service = IDtoMemoryCache.class, property = { "cache.type=historical-location" })
public class DtoCacheMemoryHistoricalLocation extends DtoMemoryCache<Instant> {

    public DtoCacheMemoryHistoricalLocation() {
        super(Instant.class);
    }

}
