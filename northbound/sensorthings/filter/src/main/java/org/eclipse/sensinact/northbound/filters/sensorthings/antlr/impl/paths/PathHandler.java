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
package org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.ResourceValueFilterInputHolder;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.UnsupportedRuleException;
import org.eclipse.sensinact.northbound.session.SensiNactSession;

public class PathHandler {

    private final String path;

    public PathHandler(final String path) {
        this.path = path;
    }

    public Object handle(final ResourceValueFilterInputHolder holder) {
        final ProviderSnapshot provider = holder.getProvider();
        final SensiNactSession session = holder.getSession();

        switch (holder.getContext()) {
        case THINGS:
            return new ThingPathHandler(provider, session).handle(path);
        case FEATURES_OF_INTEREST:
            return new FeatureOfInterestPathHandler(provider, session).handle(path);
        case HISTORICAL_LOCATIONS:
            return new HistoricalLocationPathHandler(provider, session).handle(path);
        case LOCATIONS:
            return new LocationPathHandler(provider, session).handle(path);

        case OBSERVATIONS:
            return new ObservationPathHandler(provider, session).handle(path);
        case DATASTREAMS:
            return new DatastreamPathHandler(provider, session).handle(path);
        case OBSERVED_PROPERTIES:
            return new ObservedPropertyPathHandler(provider, session).handle(path);
        case SENSORS:
            return new SensorPathHandler(provider, session).handle(path);

        default:
            throw new UnsupportedRuleException("Path of " + holder.getContext() + " is not yet supported");
        }
    }
}
