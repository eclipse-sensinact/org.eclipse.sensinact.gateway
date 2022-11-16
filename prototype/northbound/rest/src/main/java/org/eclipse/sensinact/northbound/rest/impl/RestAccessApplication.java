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

import java.util.Map;
import java.util.Set;

import org.eclipse.sensinact.prototype.SensiNactSessionManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsApplicationBase;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import com.fasterxml.jackson.jakarta.rs.json.JacksonXmlBindJsonProvider;

import jakarta.ws.rs.core.Application;

@Component(service = Application.class)
@JakartarsName("sensinact-rest")
@JakartarsApplicationBase("/sensinact")
public class RestAccessApplication extends Application {
    @Reference
    SensiNactSessionManager sessionManager;

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(SensinactSessionProvider.class, ObjectMapperProvider.class, JacksonJsonProvider.class,
                JacksonXmlBindJsonProvider.class, RestNorthbound.class);
    }

    public SensiNactSessionManager getSessionManager() {
        return sessionManager;
    }

    @Override
    public Map<String, Object> getProperties() {
        return Map.of("session.manager", sessionManager);
    }
}
