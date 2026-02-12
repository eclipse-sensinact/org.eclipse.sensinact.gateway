package org.eclipse.sensinact.sensorthings.sensing.dto.util;

import java.time.Instant;

import org.osgi.service.component.annotations.Component;

@Component(service = IDtoMemoryCache.class, property = { "cache.type=historical-location" })
public class DtoMemoryCacheHistoricalLocation extends DtoMemoryCache<Instant> {

    public DtoMemoryCacheHistoricalLocation() {
        super(Instant.class);
    }

}
