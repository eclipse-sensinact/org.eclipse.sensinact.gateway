package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IDtoMemoryCache;

public class DtoMemoryCache<M extends Id> implements IDtoMemoryCache<M> {

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
}
