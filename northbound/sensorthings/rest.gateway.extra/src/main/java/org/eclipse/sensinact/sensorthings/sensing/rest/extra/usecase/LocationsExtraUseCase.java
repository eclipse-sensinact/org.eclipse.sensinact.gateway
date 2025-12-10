package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.update.LocationUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.update.ThingUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * object
 */
@Component(service = IExtraUseCase.class)
public class LocationsExtraUseCase extends AbstractExtraUseCase<ExpandedLocation, ServiceSnapshot> {
    @Reference
    IAccessProviderUseCase providerUseCase;

    @Reference
    DataUpdate dataUpdate;

    public ExtraUseCaseResponse<ServiceSnapshot> create(ExtraUseCaseRequest<ExpandedLocation> request) {
        List<SensorThingsUpdate> listDtoModels = toDtos(request);

        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();
            LocationUpdate locationUpdate = (LocationUpdate) listDtoModels.stream().filter(s -> {
                return s instanceof LocationUpdate;
            }).findFirst().get();

            ProviderSnapshot provider = providerUseCase.read(request.session(), locationUpdate.providerId());
            if (provider != null) {
                String locationId = request.model().id() == null ? request.model().name()
                        : (String) request.model().id();
                return new ExtraUseCaseResponse<ServiceSnapshot>(locationId, provider.getService("locations"));
            }
            return new ExtraUseCaseResponse<ServiceSnapshot>(false, "fail to get providerProviderSnapshot");

        } catch (Exception e) {
            return new ExtraUseCaseResponse<ServiceSnapshot>(false, e, "fail to get providerProviderSnapshot");
        }

    }

    public ExtraUseCaseResponse<ServiceSnapshot> delete(ExtraUseCaseRequest<ExpandedLocation> request) {
        return new ExtraUseCaseResponse<ServiceSnapshot>(false, "fail to get providerProviderSnapshot");

    }

    public ExtraUseCaseResponse<ServiceSnapshot> patch(ExtraUseCaseRequest<ExpandedLocation> request) {
        return new ExtraUseCaseResponse<ServiceSnapshot>(false, "fail to get providerProviderSnapshot");

    }

    @Override
    protected List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<ExpandedLocation> request) {
        // read thing for each location and update it
        ExpandedLocation location = request.model();

        List<SensorThingsUpdate> listUpdates = DtoMapper.toLocationUpdates(request.model());
        if (location.things() != null && location.things().size() >= 0 || request.parentId() != null) {
            List<String> listThingIds = new ArrayList<String>();

            if (location.things() != null && location.things().size() >= 0) {
                listThingIds.addAll(location.things().stream().map(refId -> (String) refId.id()).toList());
            }
            if (request.parentId() != null) {
                listThingIds.add(request.parentId());
            }

            listThingIds.stream().filter(providerId -> {
                ProviderSnapshot provider = providerUseCase.read(request.session(), providerId);
                ResourceSnapshot resource = provider.getResource("thing", "locationsIds");
                return resource != null && resource.getValue() != null;
            }).map(providerId -> {
                ProviderSnapshot provider = providerUseCase.read(request.session(), providerId);
                ResourceSnapshot resource = provider.getResource("thing", "locationsIds");

                @SuppressWarnings("unchecked")
                List<String> ids = (List<String>) resource.getValue().getValue();

                ids.add(DtoMapper.sanitizeId(location.id() != null ? location.id() : location.name()));

                return new ThingUpdate(providerId, null, null, providerId, null, ids);
            }).forEach(listUpdates::add);
        }
        return listUpdates;
    }

    public ExtraUseCaseResponse<ServiceSnapshot> update(ExtraUseCaseRequest<ExpandedLocation> request) {
        List<SensorThingsUpdate> listDtoModels = toDtos(request);

        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();
            LocationUpdate locationUpdate = (LocationUpdate) listDtoModels.get(0);

            ProviderSnapshot provider = providerUseCase.read(request.session(), locationUpdate.providerId());
            if (provider != null) {
                String locationId = (String) request.model().id();
                return new ExtraUseCaseResponse<ServiceSnapshot>(locationId, provider.getService(locationId));
            }
            return new ExtraUseCaseResponse<ServiceSnapshot>(false, "fail to get providerProviderSnapshot");

        } catch (Exception e) {
            return new ExtraUseCaseResponse<ServiceSnapshot>(false, "fail to get providerProviderSnapshot");
        }

    }

    @Override
    public String getId(ExpandedLocation dto) {
        return null;
    }

}
