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

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
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
import org.eclipse.sensinact.sensorthings.sensing.rest.access.ThingsAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.create.ThingsCreate;
import org.eclipse.sensinact.sensorthings.sensing.rest.delete.ThingsDelete;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.ThingsDelegateSensinact;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensorthings.ThingsDelegateSensorthings;
import org.eclipse.sensinact.sensorthings.sensing.rest.update.ThingsUpdate;

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
    public Response updateDatastream(String id, String id2, ExpandedDataStream datastream) {
        return getSensorthingsHandler().updateDatastream(id, id2, datastream);

    }

    @Override
    public Response patchDatastream(String id, String id2, ExpandedDataStream datastream) {
        return getSensorthingsHandler().patchDatastream(id, id2, datastream);

    }

    @Override
    public Response updateLocation(String id, String id2, ExpandedLocation location) {
        return getSensorthingsHandler().updateLocation(id, id2, location);

    }

    @Override
    public Response patchLocation(String id, String id2, ExpandedLocation location) {
        return getSensorthingsHandler().patchLocation(id, id2, location);

    }

    @Override
    public Response updateThing(String id, ExpandedThing thing) {
        return getSensorthingsHandler().updateThing(id, thing);

    }

    @Override
    public Response patchThing(String id, ExpandedThing thing) {
        return getSensorthingsHandler().patchThing(id, thing);

    }

    @Override
    public Response createDatastream(String id, ExpandedDataStream datastream) {
        return getSensorthingsHandler().createDatastream(id, datastream);

    }

    @Override
    public Response createLocation(String id, ExpandedLocation location) {
        return getSensorthingsHandler().createLocation(id, location);

    }

    @Override
    public Response updateLocationRef(String id, RefId location) {
        return getSensorthingsHandler().updateLocationRef(id, location);

    }

    @Override
    public Response updateDatastreamRef(String id, RefId datastream) {
        return getSensorthingsHandler().updateDatastreamRef(id, datastream);

    }

    @Override
    public Thing getThing(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getThing(id);
        } else {
            return getSensorthingsHandler().getThing(id);

        }
    }

    @Override
    public ResultList<Datastream> getThingDatastreams(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getThingDatastreams(id);
        } else {
            return getSensorthingsHandler().getThingDatastreams(id);

        }
    }

    @Override
    public Datastream getThingDatastream(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getThingDatastream(id, id2);
        } else {
            return getSensorthingsHandler().getThingDatastream(id, id2);

        }
    }

    private boolean isSensorthingModel(ProviderSnapshot provider) {
        return DtoMapperSimple.isSensorthingModel(provider);
    }

    @Override
    public ResultList<Observation> getThingDatastreamObservations(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getThingDatastreamObservations(id, id2);
        } else {
            return getSensorthingsHandler().getThingDatastreamObservations(id, id2);

        }
    }

    @Override
    public ObservedProperty getThingDatastreamObservedProperty(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getThingDatastreamObservedProperty(id, id2);
        } else {
            return getSensorthingsHandler().getThingDatastreamObservedProperty(id, id2);

        }
    }

    @Override
    public Sensor getThingDatastreamSensor(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getThingDatastreamSensor(id, id2);
        } else {
            return getSensorthingsHandler().getThingDatastreamSensor(id, id2);

        }
    }

    @Override
    public Thing getThingDatastreamThing(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getThingDatastreamThing(id, id2);
        } else {
            return getSensorthingsHandler().getThingDatastreamThing(id, id2);

        }
    }

    @Override
    public ResultList<HistoricalLocation> getThingHistoricalLocations(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getThingHistoricalLocations(id);
        } else {
            return getSensorthingsHandler().getThingHistoricalLocations(id);

        }
    }

    @Override
    public HistoricalLocation getThingHistoricalLocation(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getThingHistoricalLocation(id, id2);
        } else {
            return getSensorthingsHandler().getThingHistoricalLocation(id, id2);

        }
    }

    @Override
    public Thing getThingHistoricalLocationsThing(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getThingHistoricalLocationsThing(id, id2);
        } else {
            return getSensorthingsHandler().getThingHistoricalLocationsThing(id, id2);

        }
    }

    @Override
    public ResultList<Location> getThingHistoricalLocationLocations(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getThingHistoricalLocationLocations(id, id2);
        } else {
            return getSensorthingsHandler().getThingHistoricalLocationLocations(id, id2);

        }
    }

    @Override
    public ResultList<Location> getThingLocations(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getThingLocations(id);
        } else {
            return getSensorthingsHandler().getThingLocations(id);

        }
    }

    @Override
    public Location getThingLocation(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getThingLocation(id, id2);
        } else {
            return getSensorthingsHandler().getThingLocation(id, id2);

        }
    }

    @Override
    public ResultList<Thing> getThingLocationThings(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getThingLocationThings(id, id2);
        } else {
            return getSensorthingsHandler().getThingLocationThings(id, id2);

        }
    }

    @Override
    public ResultList<HistoricalLocation> getThingLocationHistoricalLocations(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getThingLocationHistoricalLocations(id, id2);
        } else {
            return getSensorthingsHandler().getThingLocationHistoricalLocations(id, id2);

        }
    }

    @Override
    public Response deleteThing(String id) {
        return getSensorthingsHandler().deleteThing(id);

    }

    @Override
    public Response deleteDatastreamRef(String id, String id2) {
        return getSensorthingsHandler().deleteDatastreamRef(id, id2);

    }

    @Override
    public Response deleteLocationRef(String id, String id2) {
        return getSensorthingsHandler().deleteLocationRef(id, id2);

    }

    @Override
    public Response deleteLocationsRef(String id) {
        return getSensorthingsHandler().deleteLocationsRef(id);

    }

}
