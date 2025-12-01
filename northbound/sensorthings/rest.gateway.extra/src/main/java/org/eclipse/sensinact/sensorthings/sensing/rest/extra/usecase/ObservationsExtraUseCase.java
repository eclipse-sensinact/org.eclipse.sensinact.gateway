package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
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
public class ObservationsExtraUseCase extends AbstractExtraUseCase<ExpandedObservation, ResourceSnapshot> {
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

    public ExtraUseCaseResponse<ResourceSnapshot> create(ExtraUseCaseRequest<ExpandedObservation> request) {
        String id = DtoMapper.sanitizeId(getId(request.model()));
        List<SensorThingsUpdate> listDtoModels = toDtos(request);

        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to create");

        }

        ResourceSnapshot provider = resourceUseCase.read(request.session(), id);
        if (provider != null) {
            return new ExtraUseCaseResponse<ResourceSnapshot>(id, provider);
        }
        return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get Snapshot");

    }

    public ExtraUseCaseResponse<ResourceSnapshot> delete(ExtraUseCaseRequest<ExpandedObservation> request) {
        return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get providerSnapshot");

    }

    public ExtraUseCaseResponse<ResourceSnapshot> patch(ExtraUseCaseRequest<ExpandedObservation> request) {
        return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get providerSnapshot");

    }

    @Override
    protected List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<ExpandedObservation> request) {
        // read thing for each location and update it
        ExpandedObservation observation = request.model();
        // parent can be datastream or featureOfInterest TODO
        String idDatastream = observation.datastream().id() != null ? (String) observation.datastream().id()
                : request.parentId();

        if (idDatastream == null) {
            throw new BadRequestException("can't find datastream parent ");
        }
        String providerId = DtoMapper.extractFirstIdSegment(idDatastream);

        return List.of(DtoMapper.toObservationUpdate(providerId, idDatastream, observation));
    }

    public ExtraUseCaseResponse<ResourceSnapshot> update(ExtraUseCaseRequest<ExpandedObservation> request) {
        return new ExtraUseCaseResponse<ResourceSnapshot>(false, "fail to get providerSnapshot");

    }

    @Override
    public String getId(ExpandedObservation aDto) {
        return null;
    }

}
