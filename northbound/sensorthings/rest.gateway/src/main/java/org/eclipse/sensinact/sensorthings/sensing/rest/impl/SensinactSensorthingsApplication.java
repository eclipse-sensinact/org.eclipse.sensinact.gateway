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

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.sensinact.northbound.filters.sensorthings.ISensorthingsFilterParser;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.IDtoMemoryCache;
import org.eclipse.sensinact.sensorthings.sensing.rest.SensorThingsFeature;
import org.eclipse.sensinact.sensorthings.sensing.rest.usecase.impl.AccessProviderUseCaseProvider;
import org.eclipse.sensinact.sensorthings.sensing.rest.usecase.impl.AccessResourceUseCaseProvider;
import org.eclipse.sensinact.sensorthings.sensing.rest.usecase.impl.AccessServiceUseCaseProvider;
import org.eclipse.sensinact.sensorthings.sensing.rest.usecase.impl.DtoMemoryCacheProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsApplicationBase;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.core.Application;

@Component(service = { Application.class }, configurationPid = "sensinact.sensorthings.northbound.rest")
@JakartarsName("sensorthings")
@JakartarsApplicationBase("/")
public class SensinactSensorthingsApplication extends Application {

    public static @interface Config {
        String history_provider()

        default NOT_SET;

        int history_results_max()

        default 3000;

        boolean history_in_memory() default false;
    }

    public static final String NOT_SET = "<<NOT_SET>>";

    @Reference(target = "(cache.type=expanded-observation)")
    IDtoMemoryCache<ExpandedObservation> cacheObs;

    @Reference(target = "(cache.type=historical-location)")
    IDtoMemoryCache<Instant> cacheHl;

    @Reference
    SensiNactSessionManager sessionManager;

    @Reference
    ISensorthingsFilterParser filterParser;

    @Activate
    Config config;

    @Override
    public Set<Class<?>> getClasses() {

        Set<Class<?>> listResource = new HashSet<Class<?>>(Set.of(
                // Features/extensions
                SensorThingsFeature.class, ThrowableMapperProvider.class, SensinactSessionProvider.class,
                SensorthingsFilterProvider.class, AccessProviderUseCaseProvider.class,
                AccessResourceUseCaseProvider.class, AccessServiceUseCaseProvider.class, LoggingFilter.class,
                DtoMemoryCacheProvider.class, DtoMapperProvider.class,
                // Root
                RootResourceAccessImpl.class,
                // Collections
                DatastreamsAccessImpl.class, FeaturesOfInterestAccessImpl.class, HistoricalLocationsAccessImpl.class,
                LocationsAccessImpl.class, ObservationsAccessImpl.class, ObservedPropertiesAccessImpl.class,
                SensorsAccessImpl.class, ThingsAccessImpl.class));

        return listResource;
    }

    @Override
    public Map<String, Object> getProperties() {
        boolean defaultHistoryInMemory = config != null ? config.history_in_memory() : false;
        int defaultHistoryMaxResult = config != null ? config.history_results_max() : 0;

        boolean historyInMem = defaultHistoryInMemory;

        int resultMax = defaultHistoryMaxResult;

        String provider = config.history_provider();

        Map<String, Object> props = new HashMap<>();
        props.put("session.manager", sessionManager);
        props.put("filter.parser", filterParser);
        props.put("sensinact.history.in.memory", historyInMem);
        props.put("sensinact.history.result.limit", resultMax);
        props.put("cache.historical.location", cacheHl);
        props.put("cache.expanded.observation", cacheObs);
        if (!NOT_SET.equals(provider)) {
            props.put("sensinact.history.provider", provider);
        }
        return props;
    }

    public SensiNactSessionManager getSessionManager() {
        return sessionManager;
    }
}
