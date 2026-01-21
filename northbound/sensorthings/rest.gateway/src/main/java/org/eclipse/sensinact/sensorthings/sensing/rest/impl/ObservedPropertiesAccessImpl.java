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

import jakarta.ws.rs.core.Response;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.ObservedPropertiesAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.delete.ObservedPropertiesDelete;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.ObservedPropertiesDelegateSensinact;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensorthings.ObservedPropertiesDelegateSensorthings;
import org.eclipse.sensinact.sensorthings.sensing.rest.update.ObservedPropertiesUpdate;

public class ObservedPropertiesAccessImpl extends AbstractAccess
        implements ObservedPropertiesDelete, ObservedPropertiesAccess, ObservedPropertiesUpdate {
    private ObservedPropertiesDelegateSensinact sensinactHandler;
    private ObservedPropertiesDelegateSensorthings sensorthigHandler;

    public ObservedPropertiesDelegateSensinact getSensinactHandler() {
        if (sensinactHandler == null)
            sensinactHandler = new ObservedPropertiesDelegateSensinact(uriInfo, providers, application, requestContext);
        return sensinactHandler;

    }

    public ObservedPropertiesDelegateSensorthings getSensorthingsHandler() {
        if (sensorthigHandler == null)
            sensorthigHandler = new ObservedPropertiesDelegateSensorthings(uriInfo, providers, application,
                    requestContext);
        return sensorthigHandler;

    }

    @Override
    public Response updateObservedProperties(String id, ObservedProperty observedProperty) {
        return getSensorthingsHandler().updateObservedProperties(id, observedProperty);

    }

    @Override
    public Response patchObservedProperties(String id, ObservedProperty observedProperty) {
        return getSensorthingsHandler().patchObservedProperties(id, observedProperty);

    }

    @Override
    public ObservedProperty getObservedProperty(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservedProperty(id);
        } else {
            return getSensorthingsHandler().getObservedProperty(id);

        }
    }

    @Override
    public ResultList<Datastream> getObservedPropertyDatastreams(String id) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservedPropertyDatastreams(id);
        } else {
            return getSensorthingsHandler().getObservedPropertyDatastreams(id);

        }
    }

    @Override
    public Datastream getObservedPropertyDatastream(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservedPropertyDatastream(id, id2);
        } else {
            return getSensorthingsHandler().getObservedPropertyDatastream(id, id2);

        }
    }

    @Override
    public ResultList<Observation> getObservedPropertyDatastreamObservations(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservedPropertyDatastreamObservations(id, id2);
        } else {
            return getSensorthingsHandler().getObservedPropertyDatastreamObservations(id, id2);

        }
    }

    @Override
    public ObservedProperty getObservedPropertyDatastreamObservedProperty(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservedPropertyDatastreamObservedProperty(id, id2);
        } else {
            return getSensorthingsHandler().getObservedPropertyDatastreamObservedProperty(id, id2);

        }
    }

    @Override
    public Sensor getObservedPropertyDatastreamSensor(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservedPropertyDatastreamSensor(id, id2);
        } else {
            return getSensorthingsHandler().getObservedPropertyDatastreamSensor(id, id2);

        }
    }

    @Override
    public Thing getObservedPropertyDatastreamThing(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
       if (!isSensorthingModel(provider)) {
            return getSensinactHandler().getObservedPropertyDatastreamThing(id, id2);
        } else {
            return getSensorthingsHandler().getObservedPropertyDatastreamThing(id, id2);

        }
    }

    private boolean isSensorthingModel(ProviderSnapshot provider) {
        return DtoMapperSimple.isSensorthingModel(provider);
    }

    @Override
    public Response deleteObservedProperty(String id) {
        return getSensorthingsHandler().deleteObservedProperty(id);

    }

}
