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

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Self;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.SensinactSensorthingsApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.Property.TemplateArgument;
import org.osgi.test.common.annotation.Property.ValueSource;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;
import org.osgi.util.tracker.ServiceTracker;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;

import jakarta.ws.rs.core.Application;

/**
 * Common setup for the tests
 */
@WithConfiguration(pid = "sensinact.sensorthings.northbound.rest", properties = {
        @Property(key = "test.class", source = ValueSource.TestClass),
        @Property(key = "sessionManager.target", value = "(test.class=%s)", templateArguments = @TemplateArgument(source = ValueSource.TestClass)) })
@WithConfiguration(pid = "sensinact.session.manager", properties = {
        @Property(key = "auth.policy", value = "ALLOW_ALL"),
        @Property(key = "test.class", source = ValueSource.TestClass) })
public class AbstractIntegrationTest {

    @JsonIgnoreProperties(ignoreUnknown = true)
    protected record AnyIdDTO(String selfLink, String id) implements Id, Self {
    }

    protected void createDatastream(String providerId) {
        createDatastream(providerId, UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    protected void createDatastream(String providerId, String providerThingId, String sensorId,
            String observedPropertyId) {
        createDatastream(providerId, providerThingId, sensorId, observedPropertyId, UUID.randomUUID().toString(), 42);
    }

    protected void createDatastream(String providerId, String providerThingId, String sensorId,
            String observedPropertyId, String idObservation, Object obsValue) {
        createDatastream(providerId, providerThingId, sensorId, observedPropertyId, idObservation, null, obsValue);
    }

    protected void createDatastream(String providerId, String providerThingId, String sensorId,
            String observedPropertyId, String idObservation) {
        createDatastream(providerId, providerThingId, sensorId, observedPropertyId, idObservation, null, 42);
    }

    protected void createDatastream(String providerId, String providerThingId, String sensorId,
            String observedPropertyId, String idObservation, String idFoi, Object obsValue) {
        ExpandedObservation obs = null;
        if (idObservation != null) {
            obs = new ExpandedObservation(null, idObservation, Instant.now(), Instant.MIN, obsValue, null, null, null,
                    null, null, null, null,
                    idFoi != null ? new FeatureOfInterest(null, idFoi, "test", null, null, null, null) : null);
            createResource(providerId, "datastream", "lastObservation", obs);

        }
        createResource(providerId, "datastream", "id", providerId);
        createResource(providerId, "datastream", "sensorId", sensorId);
        createResource(providerId, "datastream", "observedPropertyId", observedPropertyId);
        createResource(providerId, "datastream", "observedPropertyName", "test");
        createResource(providerId, "datastream", "sensorName", "test");
        createResource(providerId, "datastream", "thingId", providerThingId);
        createResource(providerId, "datastream", "name", "test");
        createResource(providerId, "datastream", "unitSymbol", "test");
        createResource(providerId, "datastream", "unitName", "test");
        createResource(providerId, "datastream", "unitDefinition", "test");

    }

    protected void createLocation(String providerId) {
        createResource(providerId, "location", "id", providerId);
    }

    protected void createLocation(String providerId, GeoJsonObject location) {
        createResource(providerId, "location", "id", providerId);
        createResource(providerId, "location", "location", location);

    }

    protected void createThing(String providerId, Object datastreamListId) {
        createResource(providerId, "thing", "id", providerId);
        createResource(providerId, "thing", "locationIds", List.of());
        createResource(providerId, "thing", "datastreamIds", datastreamListId);
        createResource(providerId, "thing", "name", "test");

    }

    protected void createThing(String providerId, Object datastreamListId, Object locationIds) {
        createResource(providerId, "thing", "id", providerId);
        createResource(providerId, "thing", "locationIds", locationIds);
        createResource(providerId, "thing", "datastreamIds", datastreamListId);
        createResource(providerId, "thing", "name", "test");

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    protected record SelfDTO(String selfLink) implements Self {
    }

    protected static final TypeReference<ResultList<AnyIdDTO>> RESULT_ANY = new TypeReference<>() {
    };

    protected static final TypeReference<ResultList<SelfDTO>> RESULT_SELF = new TypeReference<>() {
    };

    private static final UserInfo USER = UserInfo.ANONYMOUS;

    @InjectService
    protected DataUpdate push;

    @InjectService
    protected GatewayThread thread;

    protected SensiNactSessionManager sessionManager;
    protected SensiNactSession session;

    protected final TestUtils utils = new TestUtils();

    @BeforeEach
    void start(@InjectBundleContext BundleContext bc, TestInfo info) throws Exception {

        Class<?> test = info.getTestClass().get();
        while (test.isMemberClass()) {
            test = test.getEnclosingClass();
        }

        ServiceTracker<Application, Application> tracker = new ServiceTracker<Application, Application>(bc,
                bc.createFilter("(&(objectClass=jakarta.ws.rs.core.Application)(test.class=" + test.getName() + "))"),
                null);

        tracker.open();

        Application app = tracker.waitForService(5000);
        assertNotNull(app);
        assertInstanceOf(SensinactSensorthingsApplication.class, app);

        sessionManager = ((SensinactSensorthingsApplication) app).getSessionManager();

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
            Thread.sleep(200);
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
        dto.type = value instanceof List ? Object.class : value.getClass();
        dto.value = value;
        dto.timestamp = instant;
        try {
            push.pushUpdate(dto).getValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
