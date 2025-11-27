package org.eclipse.sensinact.sensorthings.sensing.rest.extra.endpoint;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.Snapshot;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.IExtraDelegate;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.IExtraUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.IExtraUseCase.ExtraUseCaseRequest;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.IExtraUseCase.ExtraUseCaseResponse;
import org.eclipse.sensinact.sensorthings.sensing.rest.utils.IDtoMapper;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Component(service = IExtraDelegate.class)
public class ExtraDelegateImpl implements IExtraDelegate {

    @Reference
    IDtoMapper dtoMapper;

    private Map<String, IExtraUseCase<?>> useCases = new HashMap<String, IExtraUseCase<?>>();

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, bind = "bindExtraUseCase", unbind = "unbindExtraUseCase")
    public void bindExtraUseCase(IExtraUseCase<?> useCase) {
        useCases.put(useCase.getType().getName(), useCase);
    }

    @SuppressWarnings("unchecked")
    public <D extends Id> Response create(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, D dto,
            Class<D> clazz) {
        IExtraUseCase<D> useCase = (IExtraUseCase<D>) getExtraUseCase(dto.getClass());

        ExtraUseCaseRequest<D> request = new ExtraUseCaseRequest<D>(session, mapper, uriInfo, dto);
        ExtraUseCaseResponse<Snapshot> result = useCase.create(request);
        if (result.success()) {
            D createDto = toDto(session, mapper, uriInfo, result.snapshot(), clazz);
            URI createdUri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(createDto.id)).build();
            return Response.created(createdUri).entity(createDto).build();
        }
        return Response.status(500).build();
    }

    @SuppressWarnings("unchecked")
    public <D extends Id> Response delete(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String id,
            Class<D> clazz) {
        IExtraUseCase<D> useCase = (IExtraUseCase<D>) getExtraUseCase(clazz);
        ExtraUseCaseRequest<D> request = new ExtraUseCaseRequest<D>(session, mapper, uriInfo, id);
        ExtraUseCaseResponse<Snapshot> result = useCase.create(request);
        if (result.success()) {
            D createDto = toDto(session, mapper, uriInfo, result.snapshot(), clazz);
            URI createdUri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(createDto.id)).build();
            return Response.ok(createdUri).entity(createDto).build();
        }
        return Response.status(500).build();
    }

    protected IExtraUseCase<?> getExtraUseCase(Class<? extends Id> aType) {
        return useCases.get(aType.getName());
    }

    private FeatureOfInterest getFeatureOfInterest(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo,
            Snapshot snapshot) {
        if (snapshot instanceof ProviderSnapshot) {
            return dtoMapper.toFeatureOfInterest(session, null, mapper, uriInfo, null, null,
                    (ProviderSnapshot) snapshot);

        }
        throw new UnsupportedOperationException("Snapshot is not a ResourceSnaphot");
    }

    private HistoricalLocation getHistoricalLocation(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo,
            Snapshot snapshot) {
        if (snapshot instanceof ProviderSnapshot) {
            Optional<HistoricalLocation> historicalLocation = dtoMapper.toHistoricalLocation(session, null, mapper,
                    uriInfo, null, null, (ProviderSnapshot) snapshot);
            if (historicalLocation.get() == null) {
                throw new UnsupportedOperationException("can't find historicalLocation");
            }
            return historicalLocation.get();
        }
        throw new UnsupportedOperationException("Snapshot is not a ResourceSnaphot");
    }

    private Location getLocation(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, Snapshot snapshot) {
        if (snapshot instanceof ProviderSnapshot) {
            return dtoMapper.toLocation(session, null, mapper, uriInfo, null, null, (ProviderSnapshot) snapshot);
        }
        throw new UnsupportedOperationException("Snapshot is not a ResourceSnaphot");
    }

    private Observation getObservation(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo,
            Snapshot snapshot) {
        if (snapshot instanceof ResourceSnapshot) {
            Optional<Observation> observation = dtoMapper.toObservation(session, null, mapper, uriInfo, null, null,
                    (ResourceSnapshot) snapshot);
            if (observation.get() == null) {
                throw new NotFoundException("can't serialize observation");
            }
            return observation.get();
        }
        throw new UnsupportedOperationException("Snapshot is not a ResourceSnaphot");
    }

    private Sensor getSensor(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, Snapshot snapshot) {
        if (snapshot instanceof ResourceSnapshot) {
            return dtoMapper.toSensor(session, null, mapper, uriInfo, null, null, (ResourceSnapshot) snapshot);
        }
        throw new UnsupportedOperationException("Snapshot is not a ResourceSnaphot");
    }

    private <D extends Thing> D getThing(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo,
            Snapshot snapshot, Class<D> clazz) {
        if (snapshot instanceof ProviderSnapshot) {
            return dtoMapper.toThing(session, null, mapper, uriInfo, null, null, (ProviderSnapshot) snapshot, clazz);
        }
        throw new NotFoundException();
    }

    @SuppressWarnings("unchecked")
    protected <D extends Id> D toDto(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, Snapshot snapshot,
            Class<D> clazz) {
        if (Thing.class.isAssignableFrom(clazz)) {
            return (D) getThing(session, mapper, uriInfo, snapshot, (Class<? extends Thing>) clazz);
        } else if (Sensor.class.isAssignableFrom(clazz)) {
            return (D) getSensor(session, mapper, uriInfo, snapshot);
        } else if (Datastream.class.isAssignableFrom(clazz)) {
            return (D) getSensor(session, mapper, uriInfo, snapshot);
        } else if (Location.class.isAssignableFrom(clazz)) {
            return (D) getLocation(session, mapper, uriInfo, snapshot);
        } else if (Observation.class.isAssignableFrom(clazz)) {
            return (D) getObservation(session, mapper, uriInfo, snapshot);
        } else if (ObservedProperty.class.isAssignableFrom(clazz)) {
            return (D) getSensor(session, mapper, uriInfo, snapshot);
        } else if (HistoricalLocation.class.isAssignableFrom(clazz)) {
            return (D) getHistoricalLocation(session, mapper, uriInfo, snapshot);
        } else if (FeatureOfInterest.class.isAssignableFrom(clazz)) {
            return (D) getFeatureOfInterest(session, mapper, uriInfo, snapshot);

        }

        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void unbindExtraUseCase(IExtraUseCase<?> useCase) {
        useCases.remove(useCase.getType().getName());
    }

    @SuppressWarnings("unchecked")
    public <D extends Id> Response update(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String id,
            D dto, Class<D> clazz) {
        IExtraUseCase<D> useCase = (IExtraUseCase<D>) getExtraUseCase(dto.getClass());
        ExtraUseCaseRequest<D> request = new ExtraUseCaseRequest<D>(session, mapper, uriInfo, id, dto);
        ExtraUseCaseResponse<Snapshot> result = useCase.update(request);
        if (result.success()) {
            D createDto = toDto(session, mapper, uriInfo, result.snapshot(), clazz);
            URI createdUri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(createDto.id)).build();
            return Response.ok(createdUri).entity(createDto).build();
        }
        return Response.status(500).build();
    }

}
