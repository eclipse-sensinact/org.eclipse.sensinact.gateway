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
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.LocationsAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.delete.LocationsDelete;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.LocationsDelegateSensinact;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensorthings.LocationsDelegateSensorthings;
import org.eclipse.sensinact.sensorthings.sensing.rest.update.LocationsUpdate;
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
    public Response updateLocation(String id, ExpandedLocation location) {
        return getSensorthingsHandler().updateLocation(id, location);
    }

    @Override
    public Response patchLocation(String id, ExpandedLocation location) {
        return getSensorthingsHandler().patchLocation(id, location);
    }

    @Override
    public Location getLocation(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getLocation(id);
        } else {
            return getSensorthingsHandler().getLocation(id);

        }
    }

    @Override
    public ResultList<HistoricalLocation> getLocationHistoricalLocations(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getLocationHistoricalLocations(id);
        } else {
            return getSensorthingsHandler().getLocationHistoricalLocations(id);

        }
    }

    private boolean isSensorthingModel(ProviderSnapshot provider) {
        return DtoMapperSimple.isSensorthingModel(provider);
    }

    @Override
    public HistoricalLocation getLocationHistoricalLocation(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getLocationHistoricalLocation(id, id2);
        } else {
            return getSensorthingsHandler().getLocationHistoricalLocation(id, id2);

        }
    }

    @Override
    public Thing getLocationHistoricalLocationsThing(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getLocationHistoricalLocationsThing(id, id2);
        } else {
            return getSensorthingsHandler().getLocationHistoricalLocationsThing(id, id2);

        }
    }

    @Override
    public ResultList<Location> getLocationHistoricalLocationLocations(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getLocationHistoricalLocationLocations(id, id2);
        } else {
            return getSensorthingsHandler().getLocationHistoricalLocationLocations(id, id2);

        }
    }

    @Override
    public ResultList<Thing> getLocationThings(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getLocationThings(id);
        } else {
            return getSensorthingsHandler().getLocationThings(id);

        }
    }

    @Override
    public Thing getLocationThing(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getLocationThing(id, id2);
        } else {
            return getSensorthingsHandler().getLocationThing(id, id2);

        }
    }

    @Override
    public ResultList<Datastream> getLocationThingDatastreams(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getLocationThingDatastreams(id, id2);
        } else {
            return getSensorthingsHandler().getLocationThingDatastreams(id, id2);

        }
    }

    @Override
    public ResultList<HistoricalLocation> getLocationThingHistoricalLocations(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getLocationThingHistoricalLocations(id, id2);
        } else {
            return getSensorthingsHandler().getLocationThingHistoricalLocations(id, id2);

        }
    }

    @Override
    public ResultList<Location> getLocationThingLocations(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getLocationThingLocations(id, id2);
        } else {
            return getSensorthingsHandler().getLocationThingLocations(id, id2);

        }
    }

    @Override
    public Response deleteLocation(String id) {
        return getSensorthingsHandler().deleteLocation(id);
    }

    @Override
    public Response deleteThingsRef(String id) {
        return getSensorthingsHandler().deleteThingsRef(id);
    }

    @Override
    public Response deleteThingRef(String id, String id2) {
        return getSensorthingsHandler().deleteThingRef(id, id2);
    }

}
