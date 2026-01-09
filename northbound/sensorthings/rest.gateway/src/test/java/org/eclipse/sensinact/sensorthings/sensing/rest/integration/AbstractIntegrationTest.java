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

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.IdSelf;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Self;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilDto;
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
    protected record AnyIdDTO(String selfLink, String id) implements IdSelf {
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

    protected void createDatastrem(String provider, String thingId) {
        createDatastrem(provider, thingId, 42);
    }

    private FeatureOfInterest getFeatureOfIKnterest(String foiRefId) {
        return new FeatureOfInterest(null, foiRefId, null, null, null, null, null);
    }

    protected void createDatastrem(String provider, String thingId, int value) {
        createDatastrem(provider, thingId, value);
    }

    protected void createDatastrem(String provider, String thingId, int value, Instant valueInstant) {
        createResource(provider, UtilDto.SERVICE_DATASTREAM, "thingId", thingId, valueInstant);
        createResource(provider, UtilDto.SERVICE_DATASTREAM, "id", provider, valueInstant);
        createResource(provider, UtilDto.SERVICE_DATASTREAM, "sensorId", "test1", valueInstant);
        createResource(provider, UtilDto.SERVICE_DATASTREAM, "observedPropertyId", "test2", valueInstant);
        createResource(provider, UtilDto.SERVICE_DATASTREAM, "lastObservation",
                getObservation("test", new RefId(provider), value, getFeatureOfIKnterest("test")), valueInstant);

    }

    public static ExpandedObservation getObservation(String name, RefId datastreamRefId, int result,
            FeatureOfInterest foi) {

        return new ExpandedObservation(null, "obs2", Instant.now(), Instant.now(), result, "test", null, null, null,
                null, null, datastreamRefId, foi);

    }

    protected void createLocation(String provider) {
        createResource(provider, UtilDto.SERVICE_THING, "id", provider, null);
    }

    protected void createThing(String provider, List<String> locationIds, List<String> datastreamIds) {
        createResource(provider, UtilDto.SERVICE_THING, "id", provider, null);
        createResource(provider, UtilDto.SERVICE_THING, "locationIds", locationIds, null);
        createResource(provider, UtilDto.SERVICE_THING, "datastreamIds", datastreamIds, null);
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
