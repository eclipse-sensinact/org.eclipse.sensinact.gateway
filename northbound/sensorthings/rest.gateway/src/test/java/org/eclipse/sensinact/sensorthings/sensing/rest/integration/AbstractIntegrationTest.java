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

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.Instant;

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.security.UserInfo;
import org.eclipse.sensinact.core.session.SensiNactSession;
import org.eclipse.sensinact.core.session.SensiNactSessionManager;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Self;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

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
    protected DataUpdate push;

    @InjectService
    protected GatewayThread thread;

    @InjectService
    protected SensiNactSessionManager sessionManager;
    protected SensiNactSession session;

    protected final TestUtils utils = new TestUtils();

    @BeforeEach
    void start() throws IOException, InterruptedException {
        session = sessionManager.getDefaultSession(USER);

        // Wait for the servlet to be ready
        boolean ready = false;
        for (int i = 0; i < 10; i++) {
            HttpResponse<String> result = utils.query("/Datastreams");
            if (result.statusCode() < 400) {
                ready = true;
                break;
            }

            // Not ready yet
            System.out.println("Waiting for the SensorThings servlet to come up...");
            Thread.sleep(1000);
        }

        if (!ready) {
            fail("SensorThings servlet didn't come up");
        }
    }

    @AfterEach
    void stop() {
        session.expire();

        thread.execute(new AbstractSensinactCommand<Void>() {
            @Override
            protected Promise<Void> call(final SensinactDigitalTwin twin, final SensinactModelManager modelMgr,
                    final PromiseFactory promiseFactory) {
                twin.getProviders().forEach(SensinactProvider::delete);
                return null;
            }
        });
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
