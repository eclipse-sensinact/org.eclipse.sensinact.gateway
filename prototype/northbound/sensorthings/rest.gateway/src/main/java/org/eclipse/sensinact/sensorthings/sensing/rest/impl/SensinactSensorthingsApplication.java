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

import java.util.Map;
import java.util.Set;

import org.eclipse.sensinact.northbound.filters.sensorthings.ISensorthingsFilterParser;
import org.eclipse.sensinact.prototype.SensiNactSessionManager;
import org.eclipse.sensinact.sensorthings.sensing.rest.SensorThingsFeature;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsApplicationBase;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.core.Application;

@Component(service = Application.class, configurationPid = "sensinact.sensorthings.northbound.rest")
@JakartarsName("sensorthings")
@JakartarsApplicationBase("/")
public class SensinactSensorthingsApplication extends Application {

    public static final String NOT_SET = "<<NOT_SET>>";
    public static @interface Config {
        String history_provider() default NOT_SET;
        int history_results_max() default 3000;
    }

    @Reference
    SensiNactSessionManager sessionManager;

    @Reference
    ISensorthingsFilterParser filterParser;

    @Activate
    Config config;

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
        return NOT_SET.equals(config.history_provider())
                ? Map.of("session.manager", sessionManager, "filter.parser", filterParser,
                        "sensinact.history.result.limit", config.history_results_max())
                : Map.of("session.manager", sessionManager, "filter.parser", filterParser, "sensinact.history.provider",
                        config.history_provider(), "sensinact.history.result.limit", config.history_results_max());
    }
}
