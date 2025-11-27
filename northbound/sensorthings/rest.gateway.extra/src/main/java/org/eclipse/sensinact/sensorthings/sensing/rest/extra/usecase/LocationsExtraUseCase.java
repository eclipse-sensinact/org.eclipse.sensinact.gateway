package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.util.ArrayList;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.Snapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.Helpers;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.utils.IDtoMapper;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * object
 */
@Component(service = IExtraUseCase.class)
public class LocationsExtraUseCase extends AbstractExtraUseCase<ExpandedLocation> {

    @Reference
    DataUpdate dataUpdate;

    @Reference
    IDtoMapper dtoMapper;

    @Reference
    IAccessProviderUseCase providerUseCase;

    @Reference
    IAccessProviderUseCase resourceUseCase;

    public ExtraUseCaseResponse<Snapshot> create(ExtraUseCaseRequest<ExpandedLocation> request) {
        Stream<SensorThingsUpdate> listDtoModels = toDtos(request);
        String id = (String) request.model().id;
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();
            Snapshot resource = resourceUseCase.read(request.session(), id);
            if (resource != null) {
                return new ExtraUseCaseResponse<Snapshot>(id, resource);
            }
            return new ExtraUseCaseResponse<Snapshot>(false, "fail to get providerSnapshot");

        } catch (Exception e) {
            return new ExtraUseCaseResponse<Snapshot>(false, "fail to get providerSnapshot");
        }

    }

    public ExtraUseCaseResponse<Snapshot> delete(ExtraUseCaseRequest<ExpandedLocation> request) {
        return new ExtraUseCaseResponse<Snapshot>(false, "fail to get providerSnapshot");

    }

    private ProviderSnapshot getProviderSnapshot(ExtraUseCaseRequest<ExpandedLocation> request, Id thingId) {
        ProviderSnapshot provider = providerUseCase.read(request.session(), (String) thingId.id);
        if (provider == null) {
            throw new IllegalStateException("Provider not found for thing ID: " + thingId.id);
        }
        return provider;
    }

    public ExtraUseCaseResponse<Snapshot> patch(ExtraUseCaseRequest<ExpandedLocation> request) {
        return new ExtraUseCaseResponse<Snapshot>(false, "fail to get providerSnapshot");

    }

    @Override
    protected List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<ExpandedLocation> request) {
        // read thing for each location and update it
        ExpandedLocation location = request.model();

        Stream<SensorThingsUpdate> listThingsUpdate = location.things.stream()
                .map(thingId -> getProviderSnapshot(request, thingId))
                .map((provider) -> toExpandedThing(request, location, provider)).flatMap((expandedThing) -> {
                    return Helpers.toUpdates(expandedThing);
                });

        return listThingsUpdate;
    }

    private ExpandedThing toExpandedThing(ExtraUseCaseRequest<ExpandedLocation> request, ExpandedLocation location,
            ProviderSnapshot provider) {
        ExpandedThing expandedThing = dtoMapper.toThing(request.session(), null, request.mapper(), request.uriInfo(),
                null, null, provider, ExpandedThing.class);
        expandedThing.locations = new ArrayList<>();
        expandedThing.locations.add(location);
        return expandedThing;
    }

    public ExtraUseCaseResponse<Snapshot> update(ExtraUseCaseRequest<ExpandedLocation> request) {
        return new ExtraUseCaseResponse<Snapshot>(false, "fail to get providerSnapshot");

    }

}
