/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.sensorthings.sensing.rest.impl;

import java.util.List;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.ODataId;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.ThingsAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.create.ThingsCreate;
import org.eclipse.sensinact.sensorthings.sensing.rest.delete.ThingsDelete;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.ThingsDelegateSensinact;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensorthings.ThingsDelegateSensorthings;
import org.eclipse.sensinact.sensorthings.sensing.rest.update.ThingsUpdate;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;;

public class ThingsAccessImpl extends AbstractAccess implements ThingsDelete, ThingsAccess, ThingsCreate, ThingsUpdate {
    private ThingsDelegateSensinact sensinactHandler;
    private ThingsDelegateSensorthings sensorthigHandler;

    public ThingsDelegateSensinact getSensinactHandler() {
        if (sensinactHandler == null)
            sensinactHandler = new ThingsDelegateSensinact(uriInfo, providers, application, requestContext);
        return sensinactHandler;

    }

    public ThingsDelegateSensorthings getSensorthingsHandler() {
        if (sensorthigHandler == null)
            sensorthigHandler = new ThingsDelegateSensorthings(uriInfo, providers, application, requestContext);
        return sensorthigHandler;

    }

    @Override
    public Response updateDatastream(ODataId id, ODataId id2, ExpandedDataStream datastream) {

        return getSensorthingsHandler().updateDatastream(id.value(), id2.value(), datastream);

    }

    @Override
    public Response patchDatastream(ODataId id, ODataId id2, ExpandedDataStream datastream) {

        return getSensorthingsHandler().patchDatastream(id.value(), id2.value(), datastream);

    }

    @Override
    public Response updateLocation(ODataId id, ODataId id2, ExpandedLocation location) {

        return getSensorthingsHandler().updateLocation(id.value(), id2.value(), location);

    }

    @Override
    public Response patchLocation(ODataId id, ODataId id2, ExpandedLocation location) {

        return getSensorthingsHandler().patchLocation(id.value(), id2.value(), location);

    }

    @Override
    public Response updateThing(ODataId id, ExpandedThing thing) {

        return getSensorthingsHandler().updateThing(id.value(), thing);

    }

    @Override
    public Response patchThing(ODataId id, ExpandedThing thing) {

        return getSensorthingsHandler().patchThing(id.value(), thing);

    }

    @Override
    public Response createDatastream(ODataId id, ExpandedDataStream datastream) {

        return getSensorthingsHandler().createDatastream(id.value(), datastream);

    }

    @Override
    public Response createLocation(ODataId id, ExpandedLocation location) {

        return getSensorthingsHandler().createLocation(id.value(), location);

    }

    @Override
    public Response updateLocationRef(ODataId id, RefId location) {

        return getSensorthingsHandler().updateLocationRef(id.value(), location);

    }

    @Override
    public Response updateDatastreamRef(ODataId id, RefId datastream) {

        return getSensorthingsHandler().updateDatastreamRef(id.value(), datastream);

    }

    @Override
    public Thing getThing(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        Thing result;
        if (!isSensorthingModel(provider)) {
            result = getSensinactHandler().getThing(id.value());
        } else {
            result = getSensorthingsHandler().getThing(id.value());

        }
        return result;
    }

    @Override
    public ResultList<Datastream> getThingDatastreams(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        ResultList<Datastream> result;
        if (!isSensorthingModel(provider)) {
            result = getSensinactHandler().getThingDatastreams(id.value());
        } else {
            result = getSensorthingsHandler().getThingDatastreams(id.value());

        }

        return result;
    }

    @Override
    public Datastream getThingDatastream(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        Datastream result;
        if (!isSensorthingModel(provider)) {
            result = getSensinactHandler().getThingDatastream(id.value(), id2.value());
        } else {
            result = getSensorthingsHandler().getThingDatastream(id.value(), id2.value());

        }

        return result;
    }

    private boolean isSensorthingModel(ProviderSnapshot provider) {
        return DtoMapperSimple.isSensorthingModel(provider);
    }

    @Override
    public ResultList<Observation> getThingDatastreamObservations(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        ResultList<Observation> result;
        if (!isSensorthingModel(provider)) {
            result = getSensinactHandler().getThingDatastreamObservations(id.value(), id2.value());
        } else {
            result = getSensorthingsHandler().getThingDatastreamObservations(id.value(), id2.value());

        }
        return result;
    }

    @Override
    public ObservedProperty getThingDatastreamObservedProperty(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        ObservedProperty result;
        if (!isSensorthingModel(provider)) {
            result = getSensinactHandler().getThingDatastreamObservedProperty(id.value(), id2.value());
        } else {
            result = getSensorthingsHandler().getThingDatastreamObservedProperty(id.value(), id2.value());

        }

        return result;
    }

    @Override
    public Sensor getThingDatastreamSensor(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        Sensor result;
        if (!isSensorthingModel(provider)) {
            result = getSensinactHandler().getThingDatastreamSensor(id.value(), id2.value());
        } else {
            result = getSensorthingsHandler().getThingDatastreamSensor(id.value(), id2.value());

        }
        return result;
    }

    @Override
    public Thing getThingDatastreamThing(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        Thing result;
        if (!isSensorthingModel(provider)) {
            result = getSensinactHandler().getThingDatastreamThing(id.value(), id2.value());
        } else {
            result = getSensorthingsHandler().getThingDatastreamThing(id.value(), id2.value());

        }
        return result;
    }

    @Override
    public ResultList<HistoricalLocation> getThingHistoricalLocations(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        ResultList<HistoricalLocation> result;
        if (!isSensorthingModel(provider)) {
            result = getSensinactHandler().getThingHistoricalLocations(id.value());
        } else {
            result = getSensorthingsHandler().getThingHistoricalLocations(id.value());

        }
        return result;
    }

    @Override
    public HistoricalLocation getThingHistoricalLocation(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        HistoricalLocation result;
        if (!isSensorthingModel(provider)) {
            result = getSensinactHandler().getThingHistoricalLocation(id.value(), id2.value());
        } else {
            result = getSensorthingsHandler().getThingHistoricalLocation(id.value(), id2.value());

        }

        return result;
    }

    @Override
    public Thing getThingHistoricalLocationsThing(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        Thing result;
        if (!isSensorthingModel(provider)) {
            result = getSensinactHandler().getThingHistoricalLocationsThing(id.value(), id2.value());
        } else {
            result = getSensorthingsHandler().getThingHistoricalLocationsThing(id.value(), id2.value());

        }
        return result;
    }

    @Override
    public ResultList<Location> getThingHistoricalLocationLocations(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        ResultList<Location> result;
        if (!isSensorthingModel(provider)) {
            result = getSensinactHandler().getThingHistoricalLocationLocations(id.value(), id2.value());
        } else {
            result = getSensorthingsHandler().getThingHistoricalLocationLocations(id.value(), id2.value());

        }

        return result;
    }

    @Override
    public ResultList<Location> getThingLocations(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        ResultList<Location> result;
        if (!isSensorthingModel(provider)) {
            result = getSensinactHandler().getThingLocations(id.value());
        } else {
            result = getSensorthingsHandler().getThingLocations(id.value());

        }
        return result;
    }

    @Override
    public Location getThingLocation(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        Location result;
        if (!isSensorthingModel(provider)) {
            result = getSensinactHandler().getThingLocation(id.value(), id2.value());
        } else {
            result = getSensorthingsHandler().getThingLocation(id.value(), id2.value());

        }
        return result;

    }

    @Override
    public ResultList<Thing> getThingLocationThings(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        ResultList<Thing> result;
        if (!isSensorthingModel(provider)) {
            result = getSensinactHandler().getThingLocationThings(id.value(), id2.value());
        } else {
            result = getSensorthingsHandler().getThingLocationThings(id.value(), id2.value());

        }

        return result;
    }

    @Override
    public ResultList<HistoricalLocation> getThingLocationHistoricalLocations(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        ResultList<HistoricalLocation> result;
        if (!isSensorthingModel(provider)) {
            result = getSensinactHandler().getThingLocationHistoricalLocations(id.value(), id2.value());
        } else {
            result = getSensorthingsHandler().getThingLocationHistoricalLocations(id.value(), id2.value());

        }
        return result;
    }

    @Override
    public Response deleteThing(ODataId id) {

        validateAndGetProvider(DtoMapperSimple.extractFirstIdSegment(id.value()));

        return getSensorthingsHandler().deleteThing(id.value());

    }

    @Override
    public Response deleteDatastreamRef(ODataId id, ODataId id2) {

        validateAndGetProvider(DtoMapperSimple.extractFirstIdSegment(id.value()));
        validateAndGetProvider(DtoMapperSimple.extractFirstIdSegment(id2.value()));

        return getSensorthingsHandler().deleteDatastreamRef(id.value(), id2.value());

    }

    @Override
    public Response deleteLocationRef(ODataId id, ODataId id2) {

        validateAndGetProvider(DtoMapperSimple.extractFirstIdSegment(id.value()));
        validateAndGetProvider(DtoMapperSimple.extractFirstIdSegment(id2.value()));

        return getSensorthingsHandler().deleteLocationRef(id.value(), id2.value());

    }

    @Override
    public Response deleteLocationsRef(ODataId id) {

        validateAndGetProvider(DtoMapperSimple.extractFirstIdSegment(id.value()));

        return getSensorthingsHandler().deleteLocationsRef(id.value());

    }

    @Override
    public ResultList<Datastream> getThingDatastreamThingDatastreams(ODataId id, ODataId id2) {
        Thing thing = getThingDatastreamThing(id, id2);
        return getThingDatastreams(new ODataId((String) thing.id()));
    }

    @Override
    public ResultList<HistoricalLocation> getThingDatastreamThingHistoricalLocation(ODataId id, ODataId id2) {
        Thing thing = getThingDatastreamThing(id, id2);
        return getThingHistoricalLocations(new ODataId((String) thing.id()));
    }

    @Override
    public ResultList<Location> getThingDatastreamThingLocations(ODataId id, ODataId id2) {
        Thing thing = getThingDatastreamThing(id, id2);
        return getThingLocations(new ODataId((String) thing.id()));
    }

    @Override
    public ResultList<Datastream> getThingDatastreamSensorDatastreams(ODataId id, ODataId id2) {
        Sensor sensor = getThingDatastreamSensor(id, id2);
        if (sensor != null) {
            return new ResultList<Datastream>(null, null, List.of(getThingDatastream(id, id2)));
        }
        throw new NotFoundException();
    }

    @Override
    public ResultList<Datastream> getThingDatastreamObservedPropertyDatastreams(ODataId id, ODataId id2) {
        ObservedProperty observedProperty = getThingDatastreamObservedProperty(id, id2);
        if (observedProperty != null) {
            return new ResultList<Datastream>(null, null, List.of(getThingDatastream(id, id2)));
        }
        throw new NotFoundException();
    }

    @Override
    public Observation getThingDatastreamObservation(ODataId id, ODataId id2, ODataId id3) {
        String providerDatastreamObservationId = DtoMapperSimple.extractFirstIdSegment(id3.value());
        String providerDatastreamId = DtoMapperSimple.extractFirstIdSegment(id2.value());
        if (!providerDatastreamId.equals(providerDatastreamObservationId)) {
            throw new NotFoundException();
        }
        ProviderSnapshot provider = validateAndGetProvider(providerDatastreamId);
        if (isSensorthingModel(provider)) {
            return getSensorthingsHandler().getThingDatastreamObservation(id.value(), id2.value(), id3.value());
        } else {
            return getSensinactHandler().getThingDatastreamObservation(id.value(), id2.value(), id3.value());

        }

    }

    @Override
    public Datastream getThingDatastreamObservationDatastream(ODataId id, ODataId id2, ODataId id3) {
        String providerDatastreamId = DtoMapperSimple.extractFirstIdSegment(id2.value());
        String providerDatastreamObservationId = DtoMapperSimple.extractFirstIdSegment(id3.value());
        if (!providerDatastreamId.equals(providerDatastreamObservationId)) {
            throw new NotFoundException();
        }
        ProviderSnapshot provider = validateAndGetProvider(providerDatastreamId);
        if (isSensorthingModel(provider)) {
            return getSensorthingsHandler().getThingDatastreamObservationDatastream(id.value(), id2.value(),
                    id3.value());
        } else {
            return getSensinactHandler().getThingDatastreamObservationDatastream(id.value(), id2.value(), id3.value());

        }
    }

    public FeatureOfInterest getThingDatastreamObservationFeatureOfInterest(ODataId id, ODataId id2, ODataId id3) {
        String providerDatastreamObservationId = DtoMapperSimple.extractFirstIdSegment(id3.value());
        String providerDatastreamId = DtoMapperSimple.extractFirstIdSegment(id2.value());
        if (!providerDatastreamId.equals(providerDatastreamObservationId)) {
            throw new NotFoundException();
        }
        ProviderSnapshot provider = validateAndGetProvider(providerDatastreamId);
        if (isSensorthingModel(provider)) {
            return getSensorthingsHandler().getThingDatastreamObservationFeatureOfInterest(id.value(), id2.value(),
                    id3.value());
        } else {
            return getSensinactHandler().getThingDatastreamObservationFeatureOfInterest(id.value(), id2.value(),
                    id3.value());

        }

    }

    @Override
    public ResultList<Datastream> getThingHistoricalLocationsThingDatastreams(ODataId id, ODataId id2) {
        Thing thing = getThingHistoricalLocationsThing(id, id2);
        return getThingDatastreams(new ODataId((String) thing.id()));
    }

    @Override
    public ResultList<HistoricalLocation> getThingHistoricalLocationsThingHistoricalLocations(ODataId id, ODataId id2) {
        Thing thing = getThingHistoricalLocationsThing(id, id2);
        return getThingHistoricalLocations(new ODataId((String) thing.id()));
    }

    @Override
    public ResultList<Location> getThingHistoricalLocationsThingLocations(ODataId id, ODataId id2) {
        String providerThingId = DtoMapperSimple.extractFirstIdSegment(id.value());
        String providerThingHistoricalLocationId = DtoMapperSimple.extractFirstIdSegment(id2.value());
        if (!providerThingHistoricalLocationId.equals(providerThingId)) {
            throw new NotFoundException();
        }
        return getThingHistoricalLocationLocations(id, id2);
    }

    @Override
    public ResultList<Location> getThingHistoricalLocationLocationHistoricalLocations(ODataId id, ODataId id2,
            ODataId id3) {
        String providerThingId = DtoMapperSimple.extractFirstIdSegment(id.value());
        String providerThingHistoricalLocationId = DtoMapperSimple.extractFirstIdSegment(id2.value());
        if (!providerThingHistoricalLocationId.equals(providerThingId)) {
            throw new NotFoundException();
        }
        List<? extends Location> locations = getThingHistoricalLocationLocations(id, id2).value().stream()
                .filter(l -> l.id().equals(id3.value())).toList();
        return new ResultList<Location>(null, null, locations);
    }

    @Override
    public ResultList<Thing> getThingHistoricalLocationLocationThings(ODataId id, ODataId id2, ODataId id3) {
        ResultList<Location> locations = getThingHistoricalLocationLocations(id, id2);
        if (!locations.value().stream().map(l -> l.id()).toList().contains(id3.value())) {
            throw new NotFoundException();
        }
        ProviderSnapshot provider = validateAndGetProvider(id.value());
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getThingHistoricalLocationLocationThings(id, id2, id3);
        } else {
            return getSensorthingsHandler().getThingHistoricalLocationLocationThings(id, id2, id3);

        }

    }

    @Override
    public Thing getThingLocationHistoricalLocationThing(ODataId id, ODataId id2, ODataId id3) {

        ResultList<HistoricalLocation> list = getThingLocationHistoricalLocations(id, id2);
        if (!list.value().stream().map(hl -> hl.id()).toList().contains(id3.value())) {
            throw new NotFoundException();
        }
        return getThing(id);
    }

    @Override
    public ResultList<Location> getThingLocationHistoricalLocationLocations(ODataId id, ODataId id2, ODataId id3) {
        String providerHl = DtoMapperSimple.extractFirstIdSegment(id3.value());
        String providerThing = DtoMapperSimple.extractFirstIdSegment(id.value());
        if (!providerHl.equals(providerThing)) {
            throw new NotFoundException();
        }
        Location location = getThingLocation(id, id2);
        if (location == null) {
            throw new NotFoundException();
        }
        return getThingLocations(id);

    }

    @Override
    public ResultList<Datastream> getThingLocationThingDatastreams(ODataId id, ODataId id2, ODataId id3) {
        ResultList<Location> locations = getThingLocations(id);
        if (!locations.value().stream().map(l -> l.id()).toList().contains(id2.value())) {
            throw new NotFoundException();
        }
        String providerId1 = DtoMapperSimple.extractFirstIdSegment(id.value());
        String providerId2 = DtoMapperSimple.extractFirstIdSegment(id3.value());
        if (!providerId1.equals(providerId2)) {
            throw new NotFoundException();
        }
        return getThingDatastreams(id);
    }

    @Override
    public ResultList<HistoricalLocation> getThingLocationThingHistoricalLocations(ODataId id, ODataId id2,
            ODataId id3) {
        ResultList<Location> locations = getThingLocations(id);
        if (!locations.value().stream().map(l -> l.id()).toList().contains(id2.value())) {
            throw new NotFoundException();
        }
        String providerId1 = DtoMapperSimple.extractFirstIdSegment(id.value());
        String providerId2 = DtoMapperSimple.extractFirstIdSegment(id3.value());
        if (!providerId1.equals(providerId2)) {
            throw new NotFoundException();
        }
        return getThingHistoricalLocations(id);
    }

    @Override
    public ResultList<Location> getThingLocationThingLocations(ODataId id, ODataId id2, ODataId id3) {
        ResultList<Location> locations = getThingLocations(id);
        if (!locations.value().stream().map(l -> l.id()).toList().contains(id2.value())) {
            throw new NotFoundException();
        }
        String providerId1 = DtoMapperSimple.extractFirstIdSegment(id.value());
        String providerId2 = DtoMapperSimple.extractFirstIdSegment(id3.value());
        if (!providerId1.equals(providerId2)) {
            throw new NotFoundException();
        }
        return getThingLocations(id);
    }

}
