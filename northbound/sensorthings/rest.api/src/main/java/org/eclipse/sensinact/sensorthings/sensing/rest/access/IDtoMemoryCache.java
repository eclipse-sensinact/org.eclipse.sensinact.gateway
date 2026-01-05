package org.eclipse.sensinact.sensorthings.sensing.rest.access;

import org.eclipse.sensinact.sensorthings.sensing.dto.Id;

public interface IDtoMemoryCache<M extends Id> {

    public void addDto(String id, M dto);

    public void removeDto(String id);

    public M getDto(String id);

    public Class<M> getType();
}
