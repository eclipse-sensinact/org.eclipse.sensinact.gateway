/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.ODataId;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.LocationsAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.delete.LocationsDelete;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.LocationsDelegateSensinact;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensorthings.LocationsDelegateSensorthings;
import org.eclipse.sensinact.sensorthings.sensing.rest.update.LocationsUpdate;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

public class LocationsAccessImpl extends AbstractAccess implements LocationsDelete, LocationsAccess, LocationsUpdate {
    private LocationsDelegateSensinact sensinactHandler;
    private LocationsDelegateSensorthings sensorthigHandler;

    public LocationsDelegateSensinact getSensinactHandler() {
        if (sensinactHandler == null)
            sensinactHandler = new LocationsDelegateSensinact(uriInfo, providers, application, requestContext);
        return sensinactHandler;

    }

    public LocationsDelegateSensorthings getSensorthingsHandler() {
        if (sensorthigHandler == null)
            sensorthigHandler = new LocationsDelegateSensorthings(uriInfo, providers, application, requestContext);
        return sensorthigHandler;

    }

    @Override
    public Response updateLocation(ODataId id, ExpandedLocation location) {

        return getSensorthingsHandler().updateLocation(id.value(), location);
    }

    @Override
    public Response patchLocation(ODataId id, ExpandedLocation location) {

        return getSensorthingsHandler().patchLocation(id.value(), location);
    }

    @Override
    public Location getLocation(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getLocation(id.value());
        } else {
            return getSensorthingsHandler().getLocation(id.value());

        }
    }

    @Override
    public ResultList<HistoricalLocation> getLocationHistoricalLocations(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getLocationHistoricalLocations(providerId);
        } else {
            return getSensorthingsHandler().getLocationHistoricalLocations(providerId);

        }
    }

    private boolean isSensorthingModel(ProviderSnapshot provider) {
        return DtoMapperSimple.isSensorthingModel(provider);
    }

    @Override
    public HistoricalLocation getLocationHistoricalLocation(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getLocationHistoricalLocation(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getLocationHistoricalLocation(id.value(), id2.value());

        }
    }

    @Override
    public Thing getLocationHistoricalLocationsThing(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getLocationHistoricalLocationsThing(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getLocationHistoricalLocationsThing(id.value(), id2.value());

        }
    }

    @Override
    public ResultList<Location> getLocationHistoricalLocationLocations(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getLocationHistoricalLocationLocations(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getLocationHistoricalLocationLocations(id.value(), id2.value());

        }
    }

    @Override
    public ResultList<Thing> getLocationThings(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getLocationThings(id.value());
        } else {
            return getSensorthingsHandler().getLocationThings(id.value());

        }
    }

    @Override
    public Thing getLocationThing(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getLocationThing(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getLocationThing(id.value(), id2.value());

        }
    }

    @Override
    public ResultList<Datastream> getLocationThingDatastreams(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getLocationThingDatastreams(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getLocationThingDatastreams(id.value(), id2.value());

        }
    }

    public Sensor getLocationThingDatastreamSensor(ODataId id, ODataId id2, ODataId id3) {
        if (!getLocationThingDatastreams(id, id2).value().stream().map(ds -> ds.id()).toList().contains(id3.value())) {
            throw new NotFoundException();
        }
        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getLocationThingDatastreamSensor(id3.value());
        } else {
            return getSensorthingsHandler().getLocationThingDatastreamSensor(id3.value());

        }
    }

    public ObservedProperty getLocationThingDatastreamObservedProperty(ODataId id, ODataId id2, ODataId id3) {
        if (!getLocationThingDatastreams(id, id2).value().stream().map(ds -> ds.id()).toList().contains(id3.value())) {
            throw new NotFoundException();
        }
        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getLocationThingDatastreamObservedProperty(id3.value());
        } else {
            return getSensorthingsHandler().getLocationThingDatastreamObservedProperty(id3.value());
        }
    }

    @Override
    public ResultList<HistoricalLocation> getLocationThingHistoricalLocations(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getLocationThingHistoricalLocations(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getLocationThingHistoricalLocations(id.value(), id2.value());

        }
    }

    @Override
    public ResultList<Location> getLocationThingLocations(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getLocationThingLocations(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getLocationThingLocations(id.value(), id2.value());

        }
    }

    @Override
    public Response deleteLocation(ODataId id) {

        return getSensorthingsHandler().deleteLocation(id.value());
    }

    @Override
    public Response deleteThingsRef(ODataId id) {

        return getSensorthingsHandler().deleteThingsRef(id.value());
    }

    @Override
    public Response deleteThingRef(ODataId id, ODataId id2) {

        return getSensorthingsHandler().deleteThingRef(id.value(), id2.value());
    }

    @Override
    public ResultList<Datastream> getLocationHistoricalLocationsThingDatastreams(ODataId id, ODataId id2) {
        String providerThingId = DtoMapperSimple.extractFirstIdSegment(id2.value());

        ODataId providerId = new ODataId(providerThingId);
        Thing thing = getLocationThing(id, providerId);
        if (thing == null) {
            throw new NotFoundException();
        }

        return getLocationThingDatastreams(id, providerId);

    }

    @Override
    public ResultList<HistoricalLocation> getLocationHistoricalLocationsThingHistoricalLocations(ODataId id,
            ODataId id2) {
        String providerThingId = DtoMapperSimple.extractFirstIdSegment(id2.value());
        ProviderSnapshot provider = validateAndGetProvider(providerThingId);
        Thing thing = getLocationThing(id, new ODataId(providerThingId));
        if (thing == null) {
            throw new NotFoundException();
        }
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getLocationHistoricalLocations(id.value());
        } else {
            return getSensorthingsHandler().getLocationHistoricalLocations(id.value());
        }
    }

    @Override
    public ResultList<Location> getLocationHistoricalLocationsThingLocations(ODataId id, ODataId id2) {
        ODataId providerThingId = new ODataId(DtoMapperSimple.extractFirstIdSegment(id2.value()));
        Thing thing = getLocationThing(id, providerThingId);
        ProviderSnapshot provider = validateAndGetProvider(providerThingId.value());
        if (isSensorthingModel(provider)) {
            return getSensorthingsHandler().getThingLocations((String) thing.id());
        } else {
            return getSensinactHandler().getThingLocations((String) thing.id());

        }
    }

    @Override
    public ResultList<HistoricalLocation> getLocationHistoricalLocationsLocationHistoricalLocations(ODataId id,
            ODataId id2, ODataId id3) {
        ResultList<Location> locations = getLocationThingLocations(id, id2);
        if (!locations.value().stream().map(l -> l.id()).toList().contains(id3.value())) {
            throw new NotFoundException();
        }
        return getLocationHistoricalLocations(id);
    }

    @Override
    public ResultList<Thing> getLocationHistoricalLocationsLocationThings(ODataId id, ODataId id2, ODataId id3) {
        ResultList<Location> locations = getLocationThingLocations(id, id2);

        if (!locations.value().stream().map(l -> l.id()).toList().contains(id3.value())) {
            throw new NotFoundException();
        }
        return getLocationThings(id);
    }

    @Override
    public Thing getLocationThingDatastreamThing(ODataId id, ODataId id2, ODataId id3) {
        if (!getLocationThingDatastreams(id, id2).value().stream().map(ds -> ds.id()).toList().contains(id3.value())) {
            throw new NotFoundException();
        }
        return getLocationThing(id, id2);

    }

    @Override
    public ResultList<Observation> getLocationThingDatastreamObservations(ODataId id, ODataId id2, ODataId id3) {
        if (!getLocationThingDatastreams(id, id2).value().stream().map(ds -> ds.id()).toList().contains(id3.value())) {
            throw new NotFoundException();
        }
        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getLocationThingDatastreamObservations(id3.value());
        } else {
            return getSensorthingsHandler().getLocationThingDatastreamObservations(id3.value());
        }
    }

    @Override
    public FeatureOfInterest getLocationThingDatastreamObservationFeatureOfInterest(ODataId id, ODataId id2,
            ODataId id3, ODataId id4) {
        if (!getLocationThingDatastreams(id, id2).value().stream().map(ds -> ds.id()).toList().contains(id3.value())) {
            throw new NotFoundException();
        }
        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getLocationThingDatastreamObservationFeatureOfInterest(id3.value());
        } else {
            return getSensorthingsHandler().getLocationThingDatastreamObservationFeatureOfInterest(id3.value());
        }
    }

    @Override
    public Thing getLocationThingHistoricalLocation(ODataId id, ODataId id2, ODataId id3) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        String providerThingId = DtoMapperSimple.extractFirstIdSegment(id2.value());
        String providerHlId = DtoMapperSimple.extractFirstIdSegment(id3.value());
        if (!providerHlId.equals(providerThingId)) {
            throw new NotFoundException();
        }
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (getLocationThings(id).value().stream().map(t -> t.id()).toList().contains(id2.value())) {
            throw new NotFoundException();
        }
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getLocationThingHistoricalLocation(id3.value());
        } else {
            return getSensorthingsHandler().getLocationThingHistoricalLocation(id3.value());
        }
    }

}
