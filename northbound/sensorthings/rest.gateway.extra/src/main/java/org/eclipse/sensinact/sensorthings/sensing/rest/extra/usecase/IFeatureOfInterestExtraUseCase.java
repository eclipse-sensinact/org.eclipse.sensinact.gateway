package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;

public interface IFeatureOfInterestExtraUseCase {
    public FeatureOfInterest getInMemoryFeatureOfInterest(String id);

    public FeatureOfInterest removeInMemoryFeatureOfInterest(String id);

}
