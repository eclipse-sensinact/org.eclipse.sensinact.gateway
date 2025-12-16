package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedSensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessResourceUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * object
 */
@Component(service = { IExtraUseCase.class, IFeatureOfInterestExtraUseCase.class })
public class FeatureOfInterestExtraUseCase extends AbstractExtraUseCase<FeatureOfInterest, FeatureOfInterest>
        implements IFeatureOfInterestExtraUseCase {

    @Reference
    DataUpdate dataUpdate;

    @Reference
    IAccessResourceUseCase resourceUseCase;

    Map<String, FeatureOfInterest> featureOfInterestById = new ConcurrentHashMap<String, FeatureOfInterest>();

    public ExtraUseCaseResponse<FeatureOfInterest> create(ExtraUseCaseRequest<FeatureOfInterest> request) {
        String id = getId(request.model());
        FeatureOfInterest featureOfInterest = request.model();
        String featureOfInterestId = getId(featureOfInterest);
        featureOfInterestById.put(featureOfInterestId, featureOfInterest);
        return new ExtraUseCaseResponse<FeatureOfInterest>(id, featureOfInterest);

    }

    @Override
    public String getId(FeatureOfInterest dto) {
        return DtoToModelMapper.sanitizeId(dto.id() != null ? dto.id() : dto.name());
    }

    public ExtraUseCaseResponse<FeatureOfInterest> delete(ExtraUseCaseRequest<FeatureOfInterest> request) {
        return new ExtraUseCaseResponse<FeatureOfInterest>(false, "fail to get providerSnapshot");

    }

    public ExtraUseCaseResponse<FeatureOfInterest> patch(ExtraUseCaseRequest<FeatureOfInterest> request) {
        return new ExtraUseCaseResponse<FeatureOfInterest>(false, "fail to get providerSnapshot");

    }

    @Override
    protected List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<FeatureOfInterest> request) {
        // read thing for each location and update it
        return null;
    }

    public ExtraUseCaseResponse<FeatureOfInterest> update(ExtraUseCaseRequest<FeatureOfInterest> request) {
        return new ExtraUseCaseResponse<FeatureOfInterest>(false, "fail to get providerSnapshot");

    }

    public FeatureOfInterest removeInMemoryFeatureOfInterest(String id) {
        return featureOfInterestById.remove(id);

    }

    @Override
    public FeatureOfInterest getInMemoryFeatureOfInterest(String id) {
        return featureOfInterestById.get(id);
    }
}
