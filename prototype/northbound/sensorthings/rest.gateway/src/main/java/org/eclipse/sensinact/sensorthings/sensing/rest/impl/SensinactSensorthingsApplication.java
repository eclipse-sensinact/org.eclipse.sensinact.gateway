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

import java.util.Map;
import java.util.Set;

import org.eclipse.sensinact.northbound.filters.sensorthings.ISensorthingsFilterParser;
import org.eclipse.sensinact.prototype.SensiNactSessionManager;
import org.eclipse.sensinact.sensorthings.sensing.rest.SensorThingsFeature;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsApplicationBase;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.core.Application;

@Component(service = Application.class)
@JakartarsName("sensorthings")
@JakartarsApplicationBase("/")
public class SensinactSensorthingsApplication extends Application {

    @Reference
    SensiNactSessionManager sessionManager;

    @Reference
    ISensorthingsFilterParser filterParser;

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
                // Features/extensions
                SensorThingsFeature.class,
                SensinactSessionProvider.class,
                SensorthingsFilterProvider.class,
                // Root
                RootResourceAccessImpl.class,
                // Collections
                DatastreamsAccessImpl.class,
                FeaturesOfInterestAccessImpl.class,
                HistoricalLocationsAccessImpl.class,
                LocationsAccessImpl.class,
                ObservationsAccessImpl.class,
                ObservedPropertiesAccessImpl.class,
                SensorsAccessImpl.class,
                ThingsAccessImpl.class
            );
    }

    public SensiNactSessionManager getSessionManager() {
        return sessionManager;
    }

    @Override
    public Map<String, Object> getProperties() {
        return Map.of("session.manager", sessionManager, "filter.parser", filterParser);
    }
}
