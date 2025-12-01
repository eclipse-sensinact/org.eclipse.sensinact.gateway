package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessResourceUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * object
 */
@Component(service = IExtraUseCase.class)
public class DatastreamsExtraUseCase extends AbstractExtraUseCase<ExpandedDataStream, ResourceSnapshot> {
    @Reference
    IAccessProviderUseCase providerUseCase;

    @Reference
    IAccessResourceUseCase resourceUseCase;

    @Reference
    DataUpdate dataUpdate;

    @Override
    protected IAccessProviderUseCase getProviderUseCase() {
        return providerUseCase;
    }

    public ExtraUseCaseResponse<ResourceSnapshot> create(ExtraUseCaseRequest<ExpandedDataStream> request) {
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

    public ExtraUseCaseResponse<ResourceSnapshot> delete(ExtraUseCaseRequest<ExpandedDataStream> request) {
        return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get providerSnapshot");

    }

    public ExtraUseCaseResponse<ResourceSnapshot> patch(ExtraUseCaseRequest<ExpandedDataStream> request) {
        return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get providerSnapshot");

    }

    @Override
    protected List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<ExpandedDataStream> request) {
        // read thing for each location and update it
        ExpandedDataStream datastream = request.model();

        if (datastream.thing() == null) {
            throw new UnsupportedOperationException(String.format("Thing id not found in Datastream Payload"));
        }
        String id = (String) datastream.thing().id();
        ProviderSnapshot snapshot = getProviderSnapshot(request, id);
        if (snapshot == null) {
            throw new UnsupportedOperationException(String.format("Thing %s not found", id));
        }
        return List.of(DtoMapper.toDatastreamUpdate(id, datastream));

    }

    public ExtraUseCaseResponse<ResourceSnapshot> update(ExtraUseCaseRequest<ExpandedDataStream> request) {
        List<SensorThingsUpdate> listDtoModels = toDtos(request);

        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to create");

        }

        ResourceSnapshot snapshot = resourceUseCase.read(request.session(), request.id());
        if (snapshot != null) {
            return new ExtraUseCaseResponse<ResourceSnapshot>(request.id(), snapshot);
        }
        return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get Snapshot");

    }

    @Override
    public String getId(ExpandedDataStream dto) {
        return DtoMapper.sanitizeId(dto.id() != null ? dto.id() : dto.thing().id() + "~" + dto.name());
    }

}
