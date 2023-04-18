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
package org.eclipse.sensinact.sensorthings.sensing.rest.integration;

import java.time.Instant;

import org.eclipse.sensinact.prototype.PrototypePush;
import org.eclipse.sensinact.prototype.SensiNactSession;
import org.eclipse.sensinact.prototype.SensiNactSessionManager;
import org.eclipse.sensinact.prototype.generic.dto.GenericDto;
import org.eclipse.sensinact.prototype.security.UserInfo;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Self;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.osgi.test.common.annotation.InjectService;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Common setup for the tests
 */
public class AbstractIntegrationTest {

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class AnyIdDTO extends Id {
    }

    protected static final TypeReference<ResultList<AnyIdDTO>> RESULT_ANY = new TypeReference<>() {
    };

    protected static final TypeReference<ResultList<Self>> RESULT_SELF = new TypeReference<>() {
    };

    private static final UserInfo USER = UserInfo.ANONYMOUS;

    @InjectService
    protected PrototypePush push;

    @InjectService
    protected SensiNactSessionManager sessionManager;
    protected SensiNactSession session;

    protected final TestUtils utils = new TestUtils();

    @BeforeEach
    void start() throws InterruptedException {
        session = sessionManager.getDefaultSession(USER);
    }

    @AfterEach
    void stop() {
        session.expire();
        session = null;
    }

    protected void createResource(String provider, String service, String resource, Object value) {
        createResource(provider, service, resource, value, null);
    }

    protected void createResource(String provider, String service, String resource, Object value, Instant instant) {
        GenericDto dto = new GenericDto();
        dto.provider = provider;
        dto.service = service;
        dto.resource = resource;
        dto.type = value.getClass();
        dto.value = value;
        dto.timestamp = instant;
        try {
            push.pushUpdate(dto).getValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
