package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessServiceUseCase;
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
    IAccessServiceUseCase serviceUseCase;

    Map<String, ExpandedObservedProperty> observedPropertyById = new HashMap<String, ExpandedObservedProperty>();

    public ExtraUseCaseResponse<ExpandedObservedProperty> create(
            ExtraUseCaseRequest<ExpandedObservedProperty> request) {
        ExpandedObservedProperty observedProperty = request.model();
        String observedPropertyId = getId(observedProperty);
        ExpandedObservedProperty createExpandedProperty = new ExpandedObservedProperty(null, observedPropertyId,
                observedProperty.name(), observedProperty.description(), observedProperty.definition(),
                observedProperty.properties(), null);
        observedPropertyById.put(observedPropertyId, createExpandedProperty);

        return new ExtraUseCaseResponse<ExpandedObservedProperty>(observedPropertyId, createExpandedProperty);

    }

    @Override
    public String getId(ExpandedObservedProperty dto) {
        return DtoToModelMapper.sanitizeId(dto.id() != null ? dto.id() : dto.name());
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
        return null;
    }

    public ExtraUseCaseResponse<ExpandedObservedProperty> update(
            ExtraUseCaseRequest<ExpandedObservedProperty> request) {
        return new ExtraUseCaseResponse<ExpandedObservedProperty>(false, "fail to get providerSnapshot");

    }

    @Override
    public ExpandedObservedProperty getInMemoryObservedProperty(String id) {
        return observedPropertyById.get(id);
    }

    @Override
    public ExpandedObservedProperty removeInMemoryObservedProperty(String id) {
        return observedPropertyById.remove(id);
    }

}
