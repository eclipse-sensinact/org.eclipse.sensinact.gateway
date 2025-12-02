package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedSensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessResourceUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import jakarta.ws.rs.BadRequestException;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * object
 */
@Component(service = IExtraUseCase.class)
public class SensorsExtraUseCase extends AbstractExtraUseCase<ExpandedSensor, ResourceSnapshot> {

    @Reference
    DataUpdate dataUpdate;

    @Reference
    IAccessResourceUseCase resourceUseCase;

    public ExtraUseCaseResponse<ResourceSnapshot> create(ExtraUseCaseRequest<ExpandedSensor> request) {
        String id = getId(request.model());
        List<SensorThingsUpdate> listDtoModels = toDtos(request);

        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to create");

        }

        ResourceSnapshot snapshot = resourceUseCase.read(request.session(), id);
        if (snapshot != null) {
            return new ExtraUseCaseResponse<ResourceSnapshot>(id, snapshot);
        }
        return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get Snapshot");

    }

    public ExtraUseCaseResponse<ResourceSnapshot> delete(ExtraUseCaseRequest<ExpandedSensor> request) {
        return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get providerSnapshot");

    }

    public ExtraUseCaseResponse<ResourceSnapshot> patch(ExtraUseCaseRequest<ExpandedSensor> request) {
        return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get providerSnapshot");

    }

    @Override
    protected List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<ExpandedSensor> request) {
        ExpandedSensor sensor = request.model();
        String idDatastream = sensor.datastream() != null ? (String) sensor.datastream().id() : null;
        String providerId = DtoMapper.extractFirstIdSegment(idDatastream);
        if (idDatastream == null) {
            throw new BadRequestException("can't find parent ids");
        }

        return List.of(DtoMapper.toSensorUpdate(providerId, idDatastream, sensor));
    }

    public ExtraUseCaseResponse<ResourceSnapshot> update(ExtraUseCaseRequest<ExpandedSensor> request) {
        return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get providerSnapshot");

    }

    @Override
    public String getId(ExpandedSensor dto) {
        // TODO Auto-generated method stub
        return DtoMapper.sanitizeId(dto.id() != null ? dto.id() : dto.datastream().id() + "~" + dto.name());
    }

}
