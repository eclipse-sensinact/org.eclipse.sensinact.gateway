package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedSensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessResourceUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * object
 */
@Component(service = { IExtraUseCase.class, ISensorExtraUseCase.class })
public class SensorsExtraUseCase extends AbstractExtraUseCase<ExpandedSensor, ExpandedSensor>
        implements ISensorExtraUseCase {

    @Reference
    DataUpdate dataUpdate;

    @Reference
    IAccessResourceUseCase resourceUseCase;

    Map<String, ExpandedSensor> sensorById = new HashMap<String, ExpandedSensor>();

    public ExtraUseCaseResponse<ExpandedSensor> create(ExtraUseCaseRequest<ExpandedSensor> request) {
        ExpandedSensor sensor = request.model();
        String observedPropertyId = getId(sensor);
        ExpandedSensor createdSensor = new ExpandedSensor(null, observedPropertyId, sensor.name(), sensor.description(),
                sensor.encodingType(), sensor.metadata(), sensor.properties(), null);
        sensorById.put(observedPropertyId, createdSensor);

        return new ExtraUseCaseResponse<ExpandedSensor>(observedPropertyId, createdSensor);

    }

    public ExtraUseCaseResponse<ExpandedSensor> delete(ExtraUseCaseRequest<ExpandedSensor> request) {
        return new ExtraUseCaseResponse<ExpandedSensor>(false, "fail to get providerSnapshot");

    }

    public ExtraUseCaseResponse<ExpandedSensor> patch(ExtraUseCaseRequest<ExpandedSensor> request) {
        return new ExtraUseCaseResponse<ExpandedSensor>(false, "fail to get providerSnapshot");

    }

    @Override
    protected List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<ExpandedSensor> request) {
        return null;
    }

    public ExtraUseCaseResponse<ExpandedSensor> update(ExtraUseCaseRequest<ExpandedSensor> request) {
        return new ExtraUseCaseResponse<ExpandedSensor>(false, "fail to get providerSnapshot");

    }

    @Override
    public String getId(ExpandedSensor dto) {
        return DtoMapper.sanitizeId(dto.id() != null ? dto.id() : dto.name());
    }

    @Override
    public ExpandedSensor getInMemorySensor(String id) {
        return sensorById.get(id);
    }

    @Override
    public ExpandedSensor removeInMemorySensor(String id) {
        return sensorById.remove(id);
    }

}
