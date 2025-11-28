package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * object
 */
@Component(service = IExtraUseCase.class)
public class DatastreamsExtraUseCase extends AbstractExtraUseCase<ExpandedDataStream, ProviderSnapshot> {

    @Reference
    DataUpdate dataUpdate;

    @Reference
    IAccessProviderUseCase providerUseCase;

    public ExtraUseCaseResponse<ProviderSnapshot> create(ExtraUseCaseRequest<ExpandedDataStream> request) {
        String id = DtoMapper.sanitizeId(request.model().id() != null ? request.model().id() : request.model().name());
        List<SensorThingsUpdate> listDtoModels = toDtos(request);

        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            return new ExtraUseCaseResponse<ProviderSnapshot>(false, "fail to create");

        }

        ProviderSnapshot snapshot = resourceUseCase.read(request.session(), id);
        if (snapshot != null) {
            return new ExtraUseCaseResponse<ProviderSnapshot>(id, snapshot);
        }
        return new ExtraUseCaseResponse<ProviderSnapshot>(false, "fail to get Snapshot");

    }

    public ExtraUseCaseResponse<ProviderSnapshot> delete(ExtraUseCaseRequest<ExpandedDataStream> request) {
        return new ExtraUseCaseResponse<ProviderSnapshot>(false, "fail to get providerSnapshot");

    }

    public ExtraUseCaseResponse<ProviderSnapshot> patch(ExtraUseCaseRequest<ExpandedDataStream> request) {
        return new ExtraUseCaseResponse<ProviderSnapshot>(false, "fail to get providerSnapshot");

    }

    @Override
    protected List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<ExpandedDataStream> request) {
        // read thing for each location and update it
        ExpandedDataStream datastream = request.model();
        if (datastream.thing() != null) {
            throw new UnsupportedOperationException("Not supported yet");
        }
        String id = (String) datastream.thing().id();
        ProviderSnapshot snapshot = getProviderSnapshot(request, datastream.thing());
        if (snapshot == null) {
            throw new UnsupportedOperationException(String.format("Thing %s not found"));
        }
        return List.of(DtoMapper.toDatastreamUpdate(id, datastream));

    }

    public ExtraUseCaseResponse<ProviderSnapshot> update(ExtraUseCaseRequest<ExpandedDataStream> request) {
        List<SensorThingsUpdate> listDtoModels = toDtos(request);

        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            return new ExtraUseCaseResponse<ProviderSnapshot>(false, "fail to create");

        }

        ProviderSnapshot snapshot = resourceUseCase.read(request.session(), request.id());
        if (snapshot != null) {
            return new ExtraUseCaseResponse<ProviderSnapshot>(request.id(), snapshot);
        }
        return new ExtraUseCaseResponse<ProviderSnapshot>(false, "fail to get Snapshot");

    }

}
