package org.eclipse.sensinact.sensorthings.sensing.rest.utils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.UriInfo;

public interface IDtoMapper {

    public String extractFirstIdSegment(String id);

    ResultList<Datastream> getDataStreams(SensiNactSession userSession, Application application, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ProviderSnapshot providerSnapshot);

    public ResultList<Observation> getLiveObservations(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ProviderSnapshot provider);

    public ResultList<Observation> getObservationList(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ContainerRequestContext requestContext,
            ResourceSnapshot resourceSnapshot, ICriterion filter);

    public ResultList<Observation> getObservationList(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ResourceSnapshot resourceSnapshot,
            ICriterion filter, int localResultLimit);

    public Instant getTimestampFromId(String id);

    public Datastream toDatastream(SensiNactSession userSession, Application application, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ResourceSnapshot resource, ICriterion filter);

    public <M extends Datastream> M toDatastream(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ResourceSnapshot resource,
            ICriterion filter, Class<M> clazz);

    public FeatureOfInterest toFeatureOfInterest(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ProviderSnapshot provider);

    public <M extends FeatureOfInterest> M toFeatureOfInterest(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ProviderSnapshot provider, Class<M> clazz);

    public Optional<HistoricalLocation> toHistoricalLocation(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ProviderSnapshot provider);

    public Optional<HistoricalLocation> toHistoricalLocation(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ProviderSnapshot provider, Optional<TimedValue<?>> t);

    public List<HistoricalLocation> toHistoricalLocationList(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ProviderSnapshot provider, List<TimedValue<?>> historicalLocations);

    public Location toLocation(SensiNactSession userSession, Application application, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ProviderSnapshot provider);

    public Optional<Observation> toObservation(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ResourceSnapshot resource);

    public Optional<Observation> toObservation(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ResourceSnapshot resource, Optional<TimedValue<?>> t);

    public List<Observation> toObservationList(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ResourceSnapshot resourceSnapshot, List<TimedValue<?>> observations);

    public ObservedProperty toObservedProperty(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ResourceSnapshot resource);

    public Sensor toSensor(SensiNactSession userSession, Application application, ObjectMapper mapper, UriInfo uriInfo,
            ExpansionSettings expansions, ICriterion filter, ResourceSnapshot resource);

    public Thing toThing(SensiNactSession userSession, Application application, ObjectMapper mapper, UriInfo uriInfo,
            ExpansionSettings expansions, ICriterion filter, ProviderSnapshot provider);

    public <T extends Thing> T toThing(SensiNactSession userSession, Application application, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ProviderSnapshot provider,
            Class<T> clazz);

    /**
     * Ensure the given ID contains a single segment
     */
    public void validatedProviderId(String id);
}
