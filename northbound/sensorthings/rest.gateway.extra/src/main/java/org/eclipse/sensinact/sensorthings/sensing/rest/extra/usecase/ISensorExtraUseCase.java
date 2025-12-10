package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedSensor;

public interface ISensorExtraUseCase {
    public ExpandedSensor getSensor(String id);
}
