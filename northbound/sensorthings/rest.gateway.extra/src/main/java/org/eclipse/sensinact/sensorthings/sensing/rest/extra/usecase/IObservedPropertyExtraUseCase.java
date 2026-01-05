package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservedProperty;

public interface IObservedPropertyExtraUseCase {
    public ExpandedObservedProperty getInMemoryObservedProperty(String id);

    public ExpandedObservedProperty removeInMemoryObservedProperty(String id);

}
