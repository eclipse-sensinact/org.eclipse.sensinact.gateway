package org.eclipse.sensinact.sensorthings.sensing.dto.util;

import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.osgi.service.component.annotations.Component;

@Component(service = IDtoMemoryCache.class, property = { "cache.type=expanded-observation" })
public class DtoMemoryCacheObservation extends DtoMemoryCache<ExpandedObservation> {

    public DtoMemoryCacheObservation() {
        super(ExpandedObservation.class);
    }

}
