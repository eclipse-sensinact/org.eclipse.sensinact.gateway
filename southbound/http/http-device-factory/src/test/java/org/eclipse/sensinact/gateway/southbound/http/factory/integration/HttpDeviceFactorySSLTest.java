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
package org.eclipse.sensinact.gateway.southbound.http.factory.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.test.common.annotation.InjectService;

/**
 * Tests the HTTP device factory with authentication
 */
public class HttpDeviceFactorySSLTest {

    QueuedThreadPool threadPool;
    Server server;
    RequestHandler handler;
    int httpPort;

    static String trustJksPath, serverJksPath, clientJksPath;
    static Path tmpDir;

    @InjectService
    SensiNactSessionManager sessionManager;
    SensiNactSession session;
    BlockingQueue<ResourceDataNotification> queue;

    @InjectService
    ConfigurationAdmin configAdmin;

    @BeforeAll
    static void setup() throws Exception {
        // Temporary directory
        tmpDir = Files.createTempDirectory("sensinact-http");

        final KeyToolUtils kt = new KeyToolUtils();

        final String caJksPath = tmpDir.resolve("ca.jks").toFile().getAbsolutePath();
        trustJksPath = tmpDir.resolve("trust.jks").toFile().getAbsolutePath();
        serverJksPath = tmpDir.resolve("server.jks").toFile().getAbsolutePath();
        clientJksPath = tmpDir.resolve("client.jks").toFile().getAbsolutePath();
        final File caPemFile = tmpDir.resolve("ca.pem").toFile();
        final File serverCrsFile = tmpDir.resolve("server.crs").toFile();
        final File serverPemFile = tmpDir.resolve("server.pem").toFile();
        final Path combinedServerPemPath = tmpDir.resolve("ca_server.pem");
        final File clientCrsFile = tmpDir.resolve("client.crs").toFile();
        final File clientPemFile = tmpDir.resolve("client.pem").toFile();
        final Path combinedClientPemPath = tmpDir.resolve("ca_client.pem");

        // From: https://gist.github.com/jankronquist/6412839
        // Generate CA and trust store
        kt.runTool("-genkeypair", "-keyalg", "RSA", "-keysize", "2048", "-validity", "5", "-alias", "ca", "-dname",
                "CN=CA,O=sensinact,OU=test", "-keystore", caJksPath, "-storepass", "password", "-ext",
                "KeyUsage=digitalSignature,keyCertSign", "-ext", "BasicConstraints=ca:true,PathLen:3");
        kt.runTool(null, Redirect.to(caPemFile), "-exportcert", "-rfc", "-alias", "ca", "-keystore", caJksPath,
                "-storepass", "password");
        kt.runTool(Redirect.from(caPemFile), null, "-importcert", "-alias", "ca", "-noprompt", "-keystore",
                trustJksPath, "-storepass", "password");

        // Generate server certificate
        kt.runTool("-genkeypair", "-keyalg", "RSA", "-keysize", "2048", "-validity", "5", "-alias", "server", "-dname",
                "CN=localhost,O=sensinact,OU=test", "-keystore", serverJksPath, "-storepass", "password", "-ext",
                "SubjectAlternativeName:c=DNS:localhost,IP:127.0.0.1");
        kt.runTool(null, Redirect.to(serverCrsFile), "-certreq", "-alias", "server", "-storepass", "password",
                "-keystore", serverJksPath);
        kt.runTool(Redirect.from(serverCrsFile), Redirect.to(serverPemFile), "-gencert", "-alias", "ca", "-rfc",
                "-keystore", caJksPath, "-storepass", "password");
        kt.runTool(Redirect.from(caPemFile), null, "-importcert", "-alias", "ca", "-noprompt", "-keystore",
                serverJksPath, "-storepass", "password");
        try (OutputStream out = Files.newOutputStream(combinedServerPemPath, StandardOpenOption.CREATE)) {
            Files.copy(Paths.get(caPemFile.getPath()), out);
            Files.copy(Paths.get(serverPemFile.getPath()), out);
        }
        kt.runTool(Redirect.from(combinedServerPemPath.toFile()), null, "-importcert", "-alias", "server", "-keystore",
                serverJksPath, "-storepass", "password");

        // Generate client certificate
        kt.runTool("-genkeypair", "-keyalg", "RSA", "-keysize", "2048", "-validity", "5", "-alias", "client", "-dname",
                "CN=client,O=sensinact", "-keystore", clientJksPath, "-storepass", "password");
        kt.runTool(null, Redirect.to(clientCrsFile), "-certreq", "-alias", "client", "-keystore", clientJksPath,
                "-storepass", "password");
        kt.runTool(Redirect.from(clientCrsFile), Redirect.to(clientPemFile), "-gencert", "-alias", "ca", "-rfc",
                "-keystore", caJksPath, "-storepass", "password");
        kt.runTool(Redirect.from(caPemFile), null, "-importcert", "-alias", "ca", "-noprompt", "-keystore",
                clientJksPath, "-storepass", "password");
        try (OutputStream out = Files.newOutputStream(combinedClientPemPath, StandardOpenOption.CREATE)) {
            Files.copy(Paths.get(caPemFile.getPath()), out);
            Files.copy(Paths.get(clientPemFile.getPath()), out);
        }
        kt.runTool(Redirect.from(combinedClientPemPath.toFile()), null, "-importcert", "-alias", "client", "-keystore",
                clientJksPath, "-storepass", "password");
    }

    @AfterAll
    static void teardown() throws Exception {
        Files.walk(tmpDir).map(Path::toFile).forEach(File::delete);
        Files.delete(tmpDir);
    }

    @BeforeEach
    void start() throws InterruptedException {
        session = sessionManager.getDefaultSession(UserInfo.ANONYMOUS);
        queue = new ArrayBlockingQueue<>(32);
    }

    @AfterEach
    void stop() throws Exception {
        session.activeListeners().keySet().forEach(session::removeListener);
        session = null;
        stopServer();
    }

    void startServer(boolean withClientAuth) throws Exception {
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.addCustomizer(new SecureRequestCustomizer());
        HttpConnectionFactory http11 = new HttpConnectionFactory(httpConfig);
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(serverJksPath);
        sslContextFactory.setKeyStorePassword("password");

        if (withClientAuth) {
            sslContextFactory.setTrustStorePath(trustJksPath);
            sslContextFactory.setTrustStorePassword("password");
            sslContextFactory.setNeedClientAuth(true);
        }
        SslConnectionFactory tls = new SslConnectionFactory(sslContextFactory, http11.getProtocol());

        threadPool = new QueuedThreadPool();
        threadPool.setName("server");

        server = new Server(threadPool);
        ServerConnector conn = new ServerConnector(server, tls, http11);
        conn.setPort(0);
        server.addConnector(conn);

        handler = new RequestHandler();
        server.setHandler(handler);
        server.start();
        httpPort = conn.getLocalPort();
    }

    void stopServer() throws Exception {
        if (server != null) {
            server.stop();
            server = null;
        }

        if (threadPool != null) {
            threadPool.stop();
            threadPool = null;
        }

        if (handler != null) {
            handler.clear();
        }
    }

    String readFile(final String filename) throws IOException {
        try (InputStream inStream = getClass().getClassLoader().getResourceAsStream("/" + filename)) {
            return new String(inStream.readAllBytes());
        }
    }

    /**
     * Opens the given file from resources
     */
    String readData(final String filename, final String toReplace, final String replaceBy) throws IOException {
        final String content = readFile(filename);
        if (toReplace != null) {
            return content.replace(toReplace, replaceBy);
        } else {
            return content;
        }
    }

    /**
     * HTTP device factory should fail as the server certificate doesn't come from a
     * trusted certificate
     */
    @Test
    void testRejectUntrusted() throws Exception {
        startServer(false);

        // Excepted providers
        final String provider1 = "ssl-reject-provider1";

        // Register listener
        session.addListener(List.of(provider1 + "/*"), (t, e) -> queue.offer(e), null, null, null);

        // Providers shouldn't exist yet
        assertNull(session.describeProvider(provider1));

        final String inputFileName = "csv-header-typed";
        final String mappingConfig = new String(readFile(inputFileName + "-mapping.json"));
        handler.setData("/data", readFile(inputFileName + ".csv").replace("typed-provider", "ssl-reject-provider"));

        Configuration config = configAdmin.createFactoryConfiguration("sensinact.http.device.factory", "?");
        try {
            config.update(new Hashtable<>(Map.of("tasks.oneshot",
                    "[{\"url\": \"https://localhost:" + httpPort + "/data\", \"mapping\": " + mappingConfig + "}]")));
            // Wait for the provider to appear
            assertNull(queue.poll(1, TimeUnit.SECONDS));

            // No calls should have been made
            assertEquals(0, handler.nbVisitedPaths());
        } finally {
            config.delete();
        }
    }

    /**
     * HTTP device factory should fail as the server certificate doesn't come from a
     * trusted certificate
     */
    @Test
    void testAllowUntrusted() throws Exception {
        startServer(false);

        // Excepted providers
        final String provider1 = "ssl-allowed-provider1";

        // Register listener
        session.addListener(List.of(provider1 + "/*"), (t, e) -> queue.offer(e), null, null, null);

        // Providers shouldn't exist yet
        assertNull(session.describeProvider(provider1));

        final String inputFileName = "csv-header-typed";
        final String mappingConfig = new String(readFile(inputFileName + "-mapping.json"));
        handler.setData("/data", readData(inputFileName + ".csv", "typed-provider", "ssl-allowed-provider"));

        Configuration config = configAdmin.createFactoryConfiguration("sensinact.http.device.factory", "?");
        try {
            config.update(new Hashtable<>(Map.of("tasks.oneshot", "[{\"url\": \"https://localhost:" + httpPort
                    + "/data\", \"ssl.ignoreErrors\": true, \"mapping\": " + mappingConfig + "}]")));
            // Wait for the provider to appear
            assertNotNull(queue.poll(1, TimeUnit.SECONDS));

            // Ensure we got a value value
            assertEquals(42, session.getResourceValue(provider1, "data", "value", Integer.class));

            // Only 1 call should have been made
            assertEquals(1, handler.nbVisitedPaths());
            assertEquals(1, handler.nbVisits("/data"));
        } finally {
            config.delete();
        }
    }

    /**
     * HTTP device factory should fail as the server certificate doesn't come from a
     * trusted certificate
     */
    @Test
    void testTrusted() throws Exception {
        startServer(false);

        // Excepted providers
        final String provider1 = "ssl-trusted-provider1";

        // Register listener
        session.addListener(List.of(provider1 + "/*"), (t, e) -> queue.offer(e), null, null, null);

        // Providers shouldn't exist yet
        assertNull(session.describeProvider(provider1));

        final String inputFileName = "csv-header-typed";
        final String mappingConfig = new String(readFile(inputFileName + "-mapping.json"));
        handler.setData("/data", readData(inputFileName + ".csv", "typed-provider", "ssl-trusted-provider"));

        Configuration config = configAdmin.createFactoryConfiguration("sensinact.http.device.factory", "?");
        try {
            config.update(new Hashtable<>(Map.of("tasks.oneshot",
                    "[{\"url\": \"https://localhost:" + httpPort
                            + "/data\", \"ssl.ignoreErrors\": false, \"ssl.truststore\": \""
                            + Paths.get(trustJksPath).toUri().toASCIIString() + "\""
                            + ", \"ssl.truststore.password\": \"password\", \"mapping\": " + mappingConfig + "}]")));
            // Wait for the provider to appear
            assertNotNull(queue.poll(1, TimeUnit.SECONDS));

            // Ensure we got a value value
            assertEquals(42, session.getResourceValue(provider1, "data", "value", Integer.class));

            // Only 1 call should have been made
            assertEquals(1, handler.nbVisitedPaths());
            assertEquals(1, handler.nbVisits("/data"));
        } finally {
            config.delete();
        }
    }

    /**
     * Tests client authentication failure
     */
    @Test
    void testClientAuthFailure() throws Exception {
        // Start the server in client authentication mode
        startServer(true);

        // Excepted providers
        final String provider1 = "ssl-client-auth-fail-provider1";

        // Register listener
        session.addListener(List.of(provider1 + "/*"), (t, e) -> queue.offer(e), null, null, null);

        // Providers shouldn't exist yet
        assertNull(session.describeProvider(provider1));

        final String inputFileName = "csv-header-typed";
        final String mappingConfig = new String(readFile(inputFileName + "-mapping.json"));
        handler.setData("/data", readData(inputFileName + ".csv", "typed-provider", "ssl-client-auth-fail-provider"));

        Configuration config = configAdmin.createFactoryConfiguration("sensinact.http.device.factory", "?");
        try {
            config.update(new Hashtable<>(Map.of("tasks.oneshot",
                    "[{\"url\": \"https://localhost:" + httpPort
                            + "/data\", \"ssl.ignoreErrors\": false, \"ssl.truststore\": \""
                            + Paths.get(trustJksPath).toUri().toASCIIString() + "\""
                            + ", \"ssl.truststore.password\": \"password\", \"mapping\": " + mappingConfig + "}]")));
            // Wait for the provider to appear
            assertNull(queue.poll(1, TimeUnit.SECONDS));

            // No call should have been accepted
            assertEquals(0, handler.nbVisitedPaths());
        } finally {
            config.delete();
        }
    }

    /**
     * Tests client authentication failure
     */
    @Test
    void testClientAuthValid() throws Exception {
        // Start the server in client authentication mode
        startServer(true);

        // Excepted providers
        final String provider1 = "ssl-client-auth-valid-provider1";

        // Register listener
        session.addListener(List.of(provider1 + "/*"), (t, e) -> queue.offer(e), null, null, null);

        // Providers shouldn't exist yet
        assertNull(session.describeProvider(provider1));

        final String inputFileName = "csv-header-typed";
        final String mappingConfig = new String(readFile(inputFileName + "-mapping.json"));
        handler.setData("/data", readData(inputFileName + ".csv", "typed-provider", "ssl-client-auth-valid-provider"));

        Configuration config = configAdmin.createFactoryConfiguration("sensinact.http.device.factory", "?");
        try {
            config.update(new Hashtable<>(Map.of("tasks.oneshot",
                    "[{\"url\": \"https://localhost:" + httpPort
                            + "/data\", \"ssl.ignoreErrors\": false, \"ssl.truststore\": \""
                            + Paths.get(trustJksPath).toUri().toASCIIString() + "\""
                            + ", \"ssl.truststore.password\": \"password\", \"ssl.keystore\": \""
                            + Paths.get(clientJksPath).toUri().toASCIIString()
                            + "\", \"ssl.keystore.password\": \"password\", \"mapping\": " + mappingConfig + "}]")));
            // Wait for the provider to appear
            assertNotNull(queue.poll(1, TimeUnit.SECONDS));

            // Ensure we got a value value
            assertEquals(42, session.getResourceValue(provider1, "data", "value", Integer.class));

            // Only 1 call should have been made
            assertEquals(1, handler.nbVisitedPaths());
            assertEquals(1, handler.nbVisits("/data"));
        } finally {
            config.delete();
        }
    }
}
