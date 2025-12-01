package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessResourceUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import jakarta.ws.rs.BadRequestException;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * object
 */
@Component(service = IExtraUseCase.class)
public class ObservedPropertiesExtraUseCase extends AbstractExtraUseCase<ExpandedObservedProperty, ResourceSnapshot> {

    @Reference
    IAccessProviderUseCase providerUseCase;

    @Reference
    DataUpdate dataUpdate;

    @Override
    protected IAccessProviderUseCase getProviderUseCase() {
        return providerUseCase;
    }

    @Reference
    IAccessResourceUseCase resourceUseCase;

    public ExtraUseCaseResponse<ResourceSnapshot> create(ExtraUseCaseRequest<ExpandedObservedProperty> request) {
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

    public ExtraUseCaseResponse<ResourceSnapshot> delete(ExtraUseCaseRequest<ExpandedObservedProperty> request) {
        return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get providerSnapshot");

    }

    public ExtraUseCaseResponse<ResourceSnapshot> patch(ExtraUseCaseRequest<ExpandedObservedProperty> request) {
        return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get providerSnapshot");

    }

    @Override
    protected List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<ExpandedObservedProperty> request) {
        // read thing for each location and update it
        ExpandedObservedProperty observedProperty = request.model();
        String idDatastream = observedProperty.datastream() != null ? (String) observedProperty.datastream().id()
                : null;
        String idSensor = observedProperty.sensor() != null ? (String) observedProperty.sensor().id() : null;
        String idThing = observedProperty.thing() != null ? (String) observedProperty.thing().id() : null;

        if (idDatastream == null || idThing == null || idSensor == null) {
            throw new BadRequestException("can't find parent ids");
        }

        return List.of(DtoMapper.toObservedPropertyUpdate(idThing, idDatastream, idSensor, observedProperty));
    }

    public ExtraUseCaseResponse<ResourceSnapshot> update(ExtraUseCaseRequest<ExpandedObservedProperty> request) {
        return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get providerSnapshot");

    }

    @Override
    public String getId(ExpandedObservedProperty dto) {
        // TODO Auto-generated method stub
        return DtoMapper.sanitizeId(dto.id() != null ? dto.id() : dto.datastream().id() + "~" + dto.name());
    }

}
