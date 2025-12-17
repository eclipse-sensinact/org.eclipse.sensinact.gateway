package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodySubscribers;
import java.nio.charset.StandardCharsets;

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.IFeatureOfInterestExtraUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.IObservedPropertyExtraUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.ISensorExtraUseCase;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.ws.rs.core.Application;

@WithConfiguration(pid = "sensinact.sensorthings.northbound.rest", properties = {
        @Property(key = "test.class", source = ValueSource.TestClass),
        @Property(key = "sessionManager.target", value = "(test.class=%s)", templateArguments = @TemplateArgument(source = ValueSource.TestClass)) })
@WithConfiguration(pid = "sensinact.session.manager", properties = {
        @Property(key = "auth.policy", value = "ALLOW_ALL"),
        @Property(key = "test.class", source = ValueSource.TestClass) })
public class AbstractIntegrationTest {

    private static final UserInfo USER = UserInfo.ANONYMOUS;
    static final HttpClient client = HttpClient.newHttpClient();
    protected static final ObjectMapper mapper = new ObjectMapper();

    protected JsonNode getJsonResponseFromGet(String url) throws IOException, InterruptedException {
        HttpResponse<String> response = queryGet(url);
        return mapper.readTree(response.body());
    }

    protected JsonNode getJsonResponseFromPost(Id dto, String SubUrl, int expectedStatus)
            throws IOException, InterruptedException, JsonProcessingException, JsonMappingException {
        HttpResponse<String> response = queryPost(SubUrl, dto);
        // Then
        assertEquals(response.statusCode(), expectedStatus);
        if (response.statusCode() < 400) {
            return mapper.readTree(response.body());

        }
        return null;
    }

    @InjectService
    protected GatewayThread thread;

    @InjectService
    protected ISensorExtraUseCase sensorUseCase;

    @InjectService
    protected IObservedPropertyExtraUseCase observedPropertyUseCase;

    @InjectService
    protected IFeatureOfInterestExtraUseCase foiUseCase;

    public HttpResponse<String> queryGet(final String path) throws IOException, InterruptedException {
        // Normalize URI
        final URI targetUri;
        if (path.contains("://")) {
            targetUri = URI.create(path);
        } else if (path.startsWith("/")) {
            targetUri = URI.create("http://localhost:8185/v1.1" + path);
        } else {
            targetUri = URI.create("http://localhost:8185/v1.1/" + path);
        }

        final HttpRequest req = HttpRequest.newBuilder(targetUri).build();
        return client.send(req, (x) -> BodySubscribers.ofString(StandardCharsets.UTF_8));
    }

    public HttpResponse<String> queryPost(final String path, Id dto) throws IOException, InterruptedException {
        // Normalize URI
        final URI targetUri;
        if (path.contains("://")) {
            targetUri = URI.create(path);
        } else if (path.startsWith("/")) {
            targetUri = URI.create("http://localhost:8185/v1.1" + path);
        } else {
            targetUri = URI.create("http://localhost:8185/v1.1/" + path);
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        String body = mapper.writeValueAsString(dto);

        final HttpRequest req = HttpRequest.newBuilder(targetUri).uri(targetUri)
                .header(CONTENT_TYPE, "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
        return client.send(req, (x) -> BodySubscribers.ofString(StandardCharsets.UTF_8));
    }

    public HttpResponse<String> queryPut(final String path, String id, Id dto)
            throws IOException, InterruptedException {
        // Normalize URI
        final URI targetUri;
        if (path.contains("://")) {
            targetUri = URI.create(path + "/" + id);
        } else if (path.startsWith("/")) {
            targetUri = URI.create("http://localhost:8185/v1.1" + path);
        } else {
            targetUri = URI.create("http://localhost:8185/v1.1/" + path);
        }
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(dto);

        final HttpRequest req = HttpRequest.newBuilder(targetUri).uri(targetUri)
                .header(CONTENT_TYPE, "application/json").PUT(HttpRequest.BodyPublishers.ofString(body)).build();
        return client.send(req, (x) -> BodySubscribers.ofString(StandardCharsets.UTF_8));
    }

    public static String getIdFromJson(JsonNode node) {
        return node.get("@iot.id").asText();
    }

    public static final String CONTENT_TYPE = "Content-Type";

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
