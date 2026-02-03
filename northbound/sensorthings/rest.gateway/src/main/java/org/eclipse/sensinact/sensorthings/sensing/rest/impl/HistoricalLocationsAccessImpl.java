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

import java.util.ArrayList;
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
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.ODataId;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.HistoricalLocationsAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.delete.HistoricalLocationsDelete;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.HistoricalLocationsDelegateSensinact;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensorthings.HistoricalLocationsDelegateSensorthings;

import jakarta.ws.rs.core.Response;;

public class HistoricalLocationsAccessImpl extends AbstractAccess
        implements HistoricalLocationsAccess, HistoricalLocationsDelete {
    private HistoricalLocationsDelegateSensinact sensinactHandler;
    private HistoricalLocationsDelegateSensorthings sensorthigHandler;
    private static List<String> deletedHistoricalLocation = new ArrayList<String>(); // workaround

    public HistoricalLocationsDelegateSensinact getSensinactHandler() {
        if (sensinactHandler == null)
            sensinactHandler = new HistoricalLocationsDelegateSensinact(uriInfo, providers, application,
                    requestContext);
        return sensinactHandler;

    }

    public HistoricalLocationsDelegateSensorthings getSensorthingsHandler() {
        if (sensorthigHandler == null)
            sensorthigHandler = new HistoricalLocationsDelegateSensorthings(uriInfo, providers, application,
                    requestContext);
        return sensorthigHandler;

    }

    @Override
    public HistoricalLocation getHistoricalLocation(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getHistoricalLocation(id.value());
        } else {
            return getSensorthingsHandler().getHistoricalLocation(id.value());

        }
    }

    @Override
    public ResultList<Location> getHistoricalLocationLocations(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getHistoricalLocationLocations(id.value());
        } else {
            return getSensorthingsHandler().getHistoricalLocationLocations(id.value());

        }
    }

    @Override
    public Location getHistoricalLocationLocation(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getHistoricalLocationLocation(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getHistoricalLocationLocation(id.value(), id2.value());

        }
    }

    @Override
    public ResultList<Thing> getHistoricalLocationLocationThings(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getHistoricalLocationLocationThings(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getHistoricalLocationLocationThings(id.value(), id2.value());

        }
    }

    @Override
    public ResultList<HistoricalLocation> getHistoricalLocationLocationHistoricalLocations(ODataId id, ODataId id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getHistoricalLocationLocationHistoricalLocations(id.value(), id2.value());
        } else {
            return getSensorthingsHandler().getHistoricalLocationLocationHistoricalLocations(id.value(), id2.value());

        }
    }

    @Override
    public Thing getHistoricalLocationThing(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getHistoricalLocationThing(id.value());
        } else {
            return getSensorthingsHandler().getHistoricalLocationThing(id.value());

        }
    }

    @Override
    public ResultList<Datastream> getHistoricalLocationThingDatastreams(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getHistoricalLocationThingDatastreams(id.value());
        } else {
            return getSensorthingsHandler().getHistoricalLocationThingDatastreams(id.value());

        }
    }

    @Override
    public ResultList<HistoricalLocation> getHistoricalLocationThingHistoricalLocations(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getHistoricalLocationThingHistoricalLocations(id.value());
        } else {
            return getSensorthingsHandler().getHistoricalLocationThingHistoricalLocations(id.value());

        }
    }

    @Override
    public ResultList<Location> getHistoricalLocationThingLocations(ODataId id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getHistoricalLocationThingLocations(id.value());
        } else {
            return getSensorthingsHandler().getHistoricalLocationThingLocations(id.value());

        }
    }

    private boolean isSensorthingModel(ProviderSnapshot provider) {
        return DtoMapperSimple.isSensorthingModel(provider);
    }

    @Override
    public Sensor getHistoricalLocationThingDatastreamSensor(ODataId id, ODataId id2, ODataId id3) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getHistoricalLocationThingDatastreamSensor(id3.value());
        } else {
            return getSensorthingsHandler().getHistoricalLocationThingDatastreamSensor(id3.value());

        }
    }

    @Override
    public ResultList<Observation> getHistoricalLocationThingDatastreamObservations(ODataId id, ODataId id2,
            ODataId id3) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getHistoricalLocationThingDatastreamObservations(id3.value());
        } else {
            return getSensorthingsHandler().getHistoricalLocationThingDatastreamObservations(id3.value());

        }
    }

    @Override
    public FeatureOfInterest getHistoricalLocationThingDatastreamObservationFeatureOfInterest(ODataId id, ODataId id2,
            ODataId id3, ODataId id4) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getHistoricalLocationThingDatastreamObservationFeatureOfInterest(id3.value());
        } else {
            return getSensorthingsHandler()
                    .getHistoricalLocationThingDatastreamObservationFeatureOfInterest(id3.value());

        }
    }

    @Override
    public ObservedProperty getHistoricalLocationThingDatastreamObservedProperty(ODataId id, ODataId id2, ODataId id3) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getHistoricalLocationThingDatastreamObservedProperty(id3.value());
        } else {
            return getSensorthingsHandler().getHistoricalLocationThingDatastreamObservedProperty(id3.value());

        }
    }

    @Override
    public Response deleteHistoricalLocation(ODataId id) {
        getSensorthingsHandler().deleteHistoricalLocation(id.value());
        return Response.ok().build();
    }

}
