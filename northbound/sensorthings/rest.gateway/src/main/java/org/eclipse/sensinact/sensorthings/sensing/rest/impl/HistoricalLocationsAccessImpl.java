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
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.HistoricalLocationsAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.delete.HistoricalLocationsDelete;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.HistoricalLocationsDelegateSensinact;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensorthings.HistoricalLocationsDelegateSensorthings;

import jakarta.ws.rs.core.Response;;

public class HistoricalLocationsAccessImpl extends AbstractAccess
        implements HistoricalLocationsAccess, HistoricalLocationsDelete {
    private HistoricalLocationsDelegateSensinact sensinactHandler;
    private HistoricalLocationsDelegateSensorthings sensorthigHandler;

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
    public Response deleteHistoricalLocation(String id) {
        // TODO Auto-generated method stub
        return getSensorthingsHandler().deleteHistoricalLocation(id);
    }

    @Override
    public HistoricalLocation getHistoricalLocation(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getHistoricalLocation(id);
        } else {
            return getSensorthingsHandler().getHistoricalLocation(id);

        }
    }

    @Override
    public ResultList<Location> getHistoricalLocationLocations(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getHistoricalLocationLocations(id);
        } else {
            return getSensorthingsHandler().getHistoricalLocationLocations(id);

        }
    }

    @Override
    public Location getHistoricalLocationLocation(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getHistoricalLocationLocation(id, id2);
        } else {
            return getSensorthingsHandler().getHistoricalLocationLocation(id, id2);

        }
    }

    @Override
    public ResultList<Thing> getHistoricalLocationLocationThings(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getHistoricalLocationLocationThings(id, id2);
        } else {
            return getSensorthingsHandler().getHistoricalLocationLocationThings(id, id2);

        }
    }

    @Override
    public ResultList<HistoricalLocation> getHistoricalLocationLocationHistoricalLocations(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getHistoricalLocationLocationHistoricalLocations(id, id2);
        } else {
            return getSensorthingsHandler().getHistoricalLocationLocationHistoricalLocations(id, id2);

        }
    }

    @Override
    public Thing getHistoricalLocationThing(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getHistoricalLocationThing(id);
        } else {
            return getSensorthingsHandler().getHistoricalLocationThing(id);

        }
    }

    @Override
    public ResultList<Datastream> getHistoricalLocationThingDatastreams(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getHistoricalLocationThingDatastreams(id);
        } else {
            return getSensorthingsHandler().getHistoricalLocationThingDatastreams(id);

        }
    }

    @Override
    public ResultList<HistoricalLocation> getHistoricalLocationThingHistoricalLocations(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getHistoricalLocationThingHistoricalLocations(id);
        } else {
            return getSensorthingsHandler().getHistoricalLocationThingHistoricalLocations(id);

        }
    }

    @Override
    public ResultList<Location> getHistoricalLocationThingLocations(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getHistoricalLocationThingLocations(id);
        } else {
            return getSensorthingsHandler().getHistoricalLocationThingLocations(id);

        }
    }

    private boolean isSensorthingModel(ProviderSnapshot provider) {
        return DtoMapperSimple.isSensorthingModel(provider);
    }

}
