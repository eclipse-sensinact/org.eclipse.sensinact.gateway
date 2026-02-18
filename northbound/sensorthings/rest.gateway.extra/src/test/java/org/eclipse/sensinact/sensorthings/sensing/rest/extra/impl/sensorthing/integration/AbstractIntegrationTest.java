/*********************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.sensorthing.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodySubscribers;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessServiceUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IDtoMemoryCache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.jakartars.runtime.JakartarsServiceRuntime;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.Property.TemplateArgument;
import org.osgi.test.common.annotation.Property.ValueSource;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;
import org.osgi.util.tracker.ServiceTracker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Providers;

@WithConfiguration(pid = "sensinact.sensorthings.northbound.rest", properties = {
        @Property(key = "test.class", source = ValueSource.TestClass),
        @Property(key = "sessionManager.target", value = "(test.class=%s)", templateArguments = @TemplateArgument(source = ValueSource.TestClass)) })
@WithConfiguration(pid = "sensinact.session.manager", properties = {
        @Property(key = "auth.policy", value = "ALLOW_ALL"),
        @Property(key = "test.class", source = ValueSource.TestClass) })
public abstract class AbstractIntegrationTest {

    static final HttpClient client = HttpClient.newHttpClient();
    protected static ObjectMapper mapper = null;

    public static ObjectMapper getMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper();
            getMapper().registerModule(new JavaTimeModule());
        }
        return mapper;

    }

    @Path("test")
    public static class TestTypeExfiltrator {
        public IDtoMemoryCache<FeatureOfInterest> foiCache;
        public IDtoMemoryCache<ObservedProperty> observedPropertyCache;
        public IDtoMemoryCache<Sensor> sensorCache;
        public IAccessServiceUseCase serviceUseCase;
        public SensiNactSession session;

        @SuppressWarnings("unchecked")
        @GET
        public void exfiltrate(@Context Providers providers) {
            ContextResolver<SensiNactSession> sessionResolver = providers.getContextResolver(SensiNactSession.class,
                    MediaType.WILDCARD_TYPE);
            session = sessionResolver != null ? sessionResolver.getContext(null) : null;
            ContextResolver<IAccessServiceUseCase> serviceUseCaseResolver = providers
                    .getContextResolver(IAccessServiceUseCase.class, MediaType.WILDCARD_TYPE);

            serviceUseCase = serviceUseCaseResolver != null
                    ? serviceUseCaseResolver.getContext(IAccessServiceUseCase.class)
                    : null;

            @SuppressWarnings("rawtypes")
            ContextResolver<IDtoMemoryCache> resolverCache = providers.getContextResolver(IDtoMemoryCache.class,
                    MediaType.WILDCARD_TYPE);
            sensorCache = resolverCache != null ? resolverCache.getContext(Sensor.class) : null;
            observedPropertyCache = resolverCache != null ? resolverCache.getContext(ObservedProperty.class) : null;
            foiCache = resolverCache != null ? resolverCache.getContext(FeatureOfInterest.class) : null;

        }
    }

    @BeforeEach
    void setupUseCases(@InjectBundleContext BundleContext ctx) throws IOException, InterruptedException {
        TestTypeExfiltrator exfiltrator = new TestTypeExfiltrator();
        ctx.registerService(TestTypeExfiltrator.class, exfiltrator, new Hashtable<>(Map.of("osgi.jakartars.resource",
                true, "osgi.jakartars.application.select", "(osgi.jakartars.name=sensorthings)")));

        boolean success = false;
        HttpResponse<String> result;
        for (int i = 0; i < 20; i++) {
            result = queryGet("http://localhost:8185/test");
            if (result.statusCode() == 204) {
                this.sensorCache = exfiltrator.sensorCache;
                this.observedPropertyCache = exfiltrator.observedPropertyCache;
                this.foiCache = exfiltrator.foiCache;
                this.serviceUseCase = exfiltrator.serviceUseCase;
                this.session = exfiltrator.session;
                if (this.serviceUseCase != null && this.foiCache != null && this.observedPropertyCache != null
                        && this.sensorCache != null && this.session != null) {
                    success = true;
                    break;
                }
            } else {
                Thread.sleep(250);
            }
        }
        assertTrue(success, "Unable to get the necessary providers");
    }

    protected JsonNode getJsonResponseFromGet(String url, int expectedStatus) throws IOException, InterruptedException {
        HttpResponse<String> response = queryGet(url);
        // Then
        assertEquals(expectedStatus, response.statusCode(), response.body());
        if (response.statusCode() < 400) {
            return getMapper().readTree(response.body());

        }
        return null;
    }

    protected JsonNode getJsonResponseFromDelete(String url, int expectedStatus)
            throws IOException, InterruptedException {
        HttpResponse<String> response = queryDelete(url);
        // Then
        assertEquals(expectedStatus, response.statusCode(), response.body());
        if (response.statusCode() < 400) {
            return getMapper().readTree(response.body());

        }
        return null;
    }

    protected JsonNode getJsonResponseFromPost(Object dto, String SubUrl, int expectedStatus)
            throws IOException, InterruptedException, JsonProcessingException, JsonMappingException {
        HttpResponse<String> response = queryPost(SubUrl, dto);
        // Then
        assertEquals(expectedStatus, response.statusCode(), response.body());
        if (response.statusCode() < 400) {
            return getMapper().readTree(response.body());

        }
        return null;
    }

    protected JsonNode getJsonResponseFromPut(Object dto, String SubUrl, int expectedStatus)
            throws IOException, InterruptedException, JsonProcessingException, JsonMappingException {
        HttpResponse<String> response = queryPut(SubUrl, dto);
        // Then
        assertEquals(expectedStatus, response.statusCode(), response.body());
        if (response.statusCode() < 400) {
            return getMapper().readTree(response.body());

        }
        return null;
    }

    protected JsonNode getJsonResponseFromPatch(Object dto, String SubUrl, int expectedStatus)
            throws IOException, InterruptedException, JsonProcessingException, JsonMappingException {
        HttpResponse<String> response = queryPatch(SubUrl, dto);
        // Then
        assertEquals(expectedStatus, response.statusCode(), response.body());
        if (response.statusCode() < 400) {
            return getMapper().readTree(response.body());

        }
        return null;
    }

    @InjectService
    protected GatewayThread thread;

    @InjectService
    protected JakartarsServiceRuntime jakartarsRuntime;
    public IAccessServiceUseCase serviceUseCase;

    public IDtoMemoryCache<FeatureOfInterest> foiCache;
    public IDtoMemoryCache<ObservedProperty> observedPropertyCache;
    public IDtoMemoryCache<Sensor> sensorCache;
    public SensiNactSession session;

    public HttpResponse<String> queryGet(final String path) throws IOException, InterruptedException {
        // Normalize URI
        final URI targetUri;
        targetUri = getTargetUri(path);

        final HttpRequest req = HttpRequest.newBuilder(targetUri).build();
        return client.send(req, (x) -> BodySubscribers.ofString(StandardCharsets.UTF_8));
    }

    public HttpResponse<String> queryDelete(final String path) throws IOException, InterruptedException {
        // Normalize URI
        final URI targetUri;
        targetUri = getTargetUri(path);

        final HttpRequest req = getHttpBuilder(targetUri).DELETE().build();
        return client.send(req, (x) -> BodySubscribers.ofString(StandardCharsets.UTF_8));
    }

    public HttpResponse<String> queryPost(final String path, Object dto) throws IOException, InterruptedException {
        // Normalize URI
        final URI targetUri;
        targetUri = getTargetUri(path);
        String body = getRequestBody(dto);

        final HttpRequest req = getHttpBuilder(targetUri).POST(HttpRequest.BodyPublishers.ofString(body)).build();
        return client.send(req, (x) -> BodySubscribers.ofString(StandardCharsets.UTF_8));
    }

    public HttpResponse<String> queryPatch(final String path, Object dto) throws IOException, InterruptedException {
        // Normalize URI
        final URI targetUri = getTargetUri(path);
        String body = getRequestBody(dto);

        final HttpRequest req = getHttpBuilder(targetUri)
                .method(HttpMethod.PATCH, HttpRequest.BodyPublishers.ofString(body)).build();
        return client.send(req, (x) -> BodySubscribers.ofString(StandardCharsets.UTF_8));
    }

    private String getRequestBody(Object dto) throws JsonProcessingException {

        String body = getMapper().writeValueAsString(dto);
        return body;
    }

    private URI getTargetUri(final String path) {
        final URI targetUri;
        if (path.contains("://")) {
            targetUri = URI.create(path);
        } else if (path.startsWith("/")) {
            targetUri = URI.create("http://localhost:8185/v1.1" + path);
        } else {
            targetUri = URI.create("http://localhost:8185/v1.1/" + path);
        }
        return targetUri;
    }

    public HttpResponse<String> queryPut(final String path, Object dto) throws IOException, InterruptedException {
        // Normalize URI
        final URI targetUri;
        targetUri = getTargetUri(path);
        String body = getRequestBody(dto);

        final HttpRequest req = getHttpBuilder(targetUri).PUT(HttpRequest.BodyPublishers.ofString(body)).build();
        return client.send(req, (x) -> BodySubscribers.ofString(StandardCharsets.UTF_8));
    }

    private Builder getHttpBuilder(final URI targetUri) {
        return HttpRequest.newBuilder(targetUri).uri(targetUri).header(CONTENT_TYPE, "application/json");
    }

    public static String getIdFromJson(JsonNode node) {
        return node.get("@iot.id").asText();
    }

    public static final String CONTENT_TYPE = "Content-Type";

    @BeforeEach
    void start(@InjectBundleContext BundleContext bc,
            @InjectConfiguration(withConfig = @WithConfiguration(pid = "sensinact.sensorthings.northbound.rest", location = "?")) Configuration sensorthingsConfig,
            TestInfo info) throws Exception {

        updateConfigurationHistory(sensorthingsConfig);

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

        assertInstanceOf(Application.class, app);

        // Wait for the servlet to be ready
        boolean ready = false;
        for (int i = 0; i < 10; i++) {
            HttpResponse<String> result = queryGet("/Datastreams");
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

    protected void updateConfigurationHistory(Configuration sensorthingsConfig) throws IOException {
        // nothin
    }

    @AfterEach
    void stop() {

        thread.execute(new AbstractSensinactCommand<Void>() {
            @Override
            protected Promise<Void> call(final SensinactDigitalTwin twin, final SensinactModelManager modelMgr,
                    final PromiseFactory promiseFactory) {
                twin.getProviders().forEach(SensinactProvider::delete);
                return null;
            }
        });
    }

}
