package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessResourceUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * object
 */
@Component(service = { IExtraUseCase.class, IObservedPropertyExtraUseCase.class })
public class ObservedPropertiesExtraUseCase
        extends AbstractExtraUseCase<ExpandedObservedProperty, ExpandedObservedProperty>
        implements IObservedPropertyExtraUseCase {

    @Reference
    DataUpdate dataUpdate;

    @Reference
    IAccessResourceUseCase resourceUseCase;

    Map<String, ExpandedObservedProperty> observedPropertyById = new ConcurrentHashMap<String, ExpandedObservedProperty>();

    public ExtraUseCaseResponse<ExpandedObservedProperty> create(
            ExtraUseCaseRequest<ExpandedObservedProperty> request) {
        String id = getId(request.model());
        ExpandedObservedProperty observedProperty = request.model();
        String observedPropertyId = getId(observedProperty);
        observedPropertyById.put(observedPropertyId, observedProperty);
        return new ExtraUseCaseResponse<ExpandedObservedProperty>(id, observedProperty);

    }

    @Override
    public String getId(ExpandedObservedProperty dto) {
        return DtoMapper.sanitizeId(dto.id() != null ? dto.id() : dto.name());
    }

    public ExtraUseCaseResponse<ExpandedObservedProperty> delete(
            ExtraUseCaseRequest<ExpandedObservedProperty> request) {
        return new ExtraUseCaseResponse<ExpandedObservedProperty>(false, "fail to get providerSnapshot");

    }

    public ExtraUseCaseResponse<ExpandedObservedProperty> patch(ExtraUseCaseRequest<ExpandedObservedProperty> request) {
        return new ExtraUseCaseResponse<ExpandedObservedProperty>(false, "fail to get providerSnapshot");

    }

    @Override
    protected List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<ExpandedObservedProperty> request) {
        // read thing for each location and update it
        return null;
    }

    public ExtraUseCaseResponse<ExpandedObservedProperty> update(
            ExtraUseCaseRequest<ExpandedObservedProperty> request) {
        return new ExtraUseCaseResponse<ExpandedObservedProperty>(false, "fail to get providerSnapshot");

    }

    @Override
    public ExpandedObservedProperty getObservedProperty(String id) {
        return observedPropertyById.remove(id);
    }

}
