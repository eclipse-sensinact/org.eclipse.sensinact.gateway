package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.util.List;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import jakarta.ws.rs.BadRequestException;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * object
 */
@Component(service = IExtraUseCase.class)
public class LocationsExtraUseCase extends AbstractExtraUseCase<ExpandedLocation, ProviderSnapshot> {
    @Reference
    IAccessProviderUseCase providerUseCase;

    @Reference
    DataUpdate dataUpdate;

    public ExtraUseCaseResponse<ProviderSnapshot> create(ExtraUseCaseRequest<ExpandedLocation> request) {
        List<SensorThingsUpdate> listDtoModels = toDtos(request);
        if (request.model().things() == null || request.model().things().size() == 0) {
            throw new BadRequestException("no linked things found");
        }
        String id = (String) request.model().things().stream().findFirst().get().id();
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();
            ProviderSnapshot resource = providerUseCase.read(request.session(), id);
            if (resource != null) {
                return new ExtraUseCaseResponse<ProviderSnapshot>(id, resource);
            }
            return new ExtraUseCaseResponse<ProviderSnapshot>(false, "fail to get providerProviderSnapshot");

        } catch (Exception e) {
            return new ExtraUseCaseResponse<ProviderSnapshot>(false, "fail to get providerProviderSnapshot");
        }

    }

    public ExtraUseCaseResponse<ProviderSnapshot> delete(ExtraUseCaseRequest<ExpandedLocation> request) {
        return new ExtraUseCaseResponse<ProviderSnapshot>(false, "fail to get providerProviderSnapshot");

    }

    public ExtraUseCaseResponse<ProviderSnapshot> patch(ExtraUseCaseRequest<ExpandedLocation> request) {
        return new ExtraUseCaseResponse<ProviderSnapshot>(false, "fail to get providerProviderSnapshot");

    }

    @Override
    protected List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<ExpandedLocation> request) {
        // read thing for each location and update it
        ExpandedLocation location = request.model();
        if (location.things() == null || location.things().size() == 0) {
            throw new UnsupportedOperationException("Not supported yet");
        } else {
            List<SensorThingsUpdate> listThingsUpdate = location.things().stream()
                    .map(thingId -> providerUseCase.read(request.session(), (String) thingId.id()))
                    .map((provider) -> DtoMapper.toExpandedThing(request, location, provider))
                    .flatMap((expandedThing) -> {
                        return DtoMapper.toUpdates(expandedThing);
                    }).toList();

            return listThingsUpdate;
        }
    }

    public ExtraUseCaseResponse<ProviderSnapshot> update(ExtraUseCaseRequest<ExpandedLocation> request) {
        List<SensorThingsUpdate> listDtoModels = toDtos(request);
        String id = (String) request.model().id();
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();
            ProviderSnapshot resource = providerUseCase.read(request.session(), id);
            if (resource != null) {
                return new ExtraUseCaseResponse<ProviderSnapshot>(id, resource);
            }
            return new ExtraUseCaseResponse<ProviderSnapshot>(false, "fail to get providerProviderSnapshot");

        } catch (Exception e) {
            return new ExtraUseCaseResponse<ProviderSnapshot>(false, "fail to get providerProviderSnapshot");
        }

    }

    @Override
    public String getId(ExpandedLocation dto) {
        return null;
    }

}
