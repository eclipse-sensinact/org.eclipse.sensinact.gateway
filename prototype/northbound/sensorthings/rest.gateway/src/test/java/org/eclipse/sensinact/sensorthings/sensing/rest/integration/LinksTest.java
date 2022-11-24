package org.eclipse.sensinact.sensorthings.sensing.rest.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodySubscribers;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.sensinact.prototype.PrototypePush;
import org.eclipse.sensinact.prototype.SensiNactSession;
import org.eclipse.sensinact.prototype.SensiNactSessionManager;
import org.eclipse.sensinact.prototype.notification.ResourceDataNotification;
import org.eclipse.sensinact.sensorthings.sensing.dto.RootResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests that all links related to a thing are valid
 */
public class LinksTest {
    private static final String USER = "user";

    private static final String PROVIDER = "linkTester";
    private static final String PROVIDER_TOPIC = PROVIDER + "/*";

    @InjectService
    SensiNactSessionManager sessionManager;

    @InjectService
    PrototypePush push;

    final ObjectMapper mapper = new ObjectMapper();

    BlockingQueue<ResourceDataNotification> queue;

    @BeforeEach
    void start() throws InterruptedException {
        queue = new ArrayBlockingQueue<>(32);
        SensiNactSession session = sessionManager.getDefaultSession(USER);
        session.addListener(List.of(PROVIDER_TOPIC), (t, e) -> queue.offer(e), null, null, null);
        assertNull(queue.poll(500, TimeUnit.MILLISECONDS));
    }

    @AfterEach
    void stop() {
        SensiNactSession session = sessionManager.getDefaultSession(USER);
        session.activeListeners().keySet().forEach(session::removeListener);
    }

    /**
     * Executes a GET request and returns its parsed content
     */
    public <T> T queryJson(final String path, final Class<T> resultType) throws IOException, InterruptedException {
        // Normalize URI
        final URI targetUri;
        if (path.startsWith("/")) {
            targetUri = URI.create("http://localhost:8185/v1.1" + path);
        } else {
            targetUri = URI.create("http://localhost:8185/v1.1/" + path);
        }

        final HttpClient client = HttpClient.newHttpClient();
        final HttpRequest req = HttpRequest.newBuilder(targetUri).build();
        final HttpResponse<InputStream> response = client.send(req, (x) -> BodySubscribers.ofInputStream());
        return mapper.createParser(response.body()).readValueAs(resultType);
    }

    @Test
    void testLinksFromRoot() throws IOException, InterruptedException {
        RootResponse json = queryJson("/", RootResponse.class);
        assertNotNull(json);
    }
}
