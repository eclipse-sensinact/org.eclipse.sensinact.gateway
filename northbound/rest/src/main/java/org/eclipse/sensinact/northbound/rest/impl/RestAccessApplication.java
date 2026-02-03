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
package org.eclipse.sensinact.northbound.rest.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.sensinact.northbound.query.api.IQueryHandler;
import org.eclipse.sensinact.northbound.security.api.Authenticator;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsApplicationBase;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import com.fasterxml.jackson.jakarta.rs.json.JacksonXmlBindJsonProvider;

import jakarta.ws.rs.core.Application;

@Component(service = Application.class, configurationPid = "sensinact.northbound.rest")
@JakartarsName("sensinact-rest")
@JakartarsApplicationBase("/sensinact")
public class RestAccessApplication extends Application {

    @interface Config {
        boolean allow_anonymous() default false;
    }

    @Reference
    SensiNactSessionManager sessionManager;

    @Reference
    IQueryHandler queryHandler;

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    final List<Authenticator> authenticators = new CopyOnWriteArrayList<>();

    @Activate
    Config config;

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
            StatusCodeFilter.class,
            SensinactSessionProvider.class,
            SensinactSessionManagerProvider.class,
            QueryHandlerProvider.class,
            ObjectMapperProvider.class,
            JacksonJsonProvider.class,
            JacksonXmlBindJsonProvider.class,
            RestNorthbound.class,
            AuthenticationFilter.class,
            RestRuntime.class);
    }

    public SensiNactSessionManager getSessionManager() {
        return sessionManager;
    }

    @Override
    public Map<String, Object> getProperties() {
        return Map.of("session.manager", sessionManager, "query.handler", queryHandler, "authentication.providers",
                authenticators, "raw.anonymous.access", config.allow_anonymous());
    }
}
