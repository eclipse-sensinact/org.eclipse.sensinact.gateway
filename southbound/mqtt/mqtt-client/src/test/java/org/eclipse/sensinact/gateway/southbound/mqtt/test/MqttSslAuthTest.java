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
package org.eclipse.sensinact.gateway.southbound.mqtt.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocketFactory;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.sensinact.gateway.southbound.mqtt.api.IMqttMessage;
import org.eclipse.sensinact.gateway.southbound.mqtt.api.IMqttMessageListener;
import org.eclipse.sensinact.gateway.southbound.mqtt.impl.MqttClientConfiguration;
import org.eclipse.sensinact.gateway.southbound.mqtt.impl.MqttClientHandler;
import org.eclipse.sensinact.gateway.southbound.mqtt.impl.SSLUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.moquette.BrokerConstants;
import io.moquette.broker.Server;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.MemoryConfig;

/**
 * Tests of the MQTT southbound
 */
public class MqttSslAuthTest {

    private static final String SERVER_CERT_P12 = "src/test/resources/server.p12";
    private static final String SERVER_CERT_PASS = "secret";

    private static final String CLIENT_CERT_P12 = "src/test/resources/client.p12";
    private static final String CLIENT_CERT_PEM = "src/test/resources/client.pem";
    private static final String CLIENT_CERT_KEY_PEM = "src/test/resources/client.key";
    private static final String CLIENT_CERT_UNSIGNED_P12 = "src/test/resources/client_unsigned.p12";
    private static final String CLIENT_CERT_UNSIGNED_WITH_CA_P12 = "src/test/resources/client_unsigned_with_ca.p12";
    private static final String CLIENT_CERT_PASS = "titi21";

    private static final String TRUST_CERT_P12 = "src/test/resources/ca.p12";
    private static final String TRUST_CERT_PEM = "src/test/resources/ca.pem";
    private static final String TRUST_CERT_PASS = "secret";

    /**
     * Holds the generated key pair and the associated certificate
     */
    private static class CertificateKeyPair {
        private KeyPair keypair;
        private Certificate certificate;
    }

    /**
     * Client to publish messages
     */
    private MqttClient client;

    /**
     * Active handlers
     */
    private final List<MqttClientHandler> handlers = new ArrayList<>();

    /**
     * MQTT broker
     */
    private Server server;

    /**
     * Generates the certificates used in the tests
     */
    @BeforeAll
    static void generateCertificates() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        final String caAlias = "ca";

        // Generate a CA key pair
        final KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGen.initialize(2048);
        final KeyPair caKeyPair = keyPairGen.generateKeyPair();

        // Create CA certificate (self signed)
        final X509Certificate caCert = generateSelfSignedCertificate(makeName("Certificate Authority", "ca"), caKeyPair,
                true);
        // Write down trust store
        final KeyStore caTrustStore = KeyStore.getInstance("PKCS12");
        caTrustStore.load(null);
        caTrustStore.setCertificateEntry("ca", caCert);
        try (FileOutputStream fos = new FileOutputStream(TRUST_CERT_P12)) {
            caTrustStore.store(fos, TRUST_CERT_PASS.toCharArray());
        }
        writePemCertificate(caCert, TRUST_CERT_PEM);

        // Prepare the server certificate & sign it with the CA
        makeSignedKeystore("localhost", "server", SERVER_CERT_P12, SERVER_CERT_PASS, caKeyPair, caCert);

        // Prepare the server certificate & sign it with the CA
        final CertificateKeyPair clientCert = makeSignedKeystore("Signed Client", "client", CLIENT_CERT_P12,
                CLIENT_CERT_PASS, caKeyPair, caCert);
        writePemCertificate(clientCert.certificate, CLIENT_CERT_PEM);
        writePemPrivateKey(clientCert.keypair.getPrivate(), CLIENT_CERT_KEY_PEM);

        // Prepare an unsigned client key pair
        makeUnsignedKeyStore("Self-Signed Client", "client", CLIENT_CERT_UNSIGNED_P12, CLIENT_CERT_PASS);

        // Same thing, but with the CA certificate in the store
        final KeyStore clientUnsignedWithCAKeyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(CLIENT_CERT_UNSIGNED_P12)) {
            clientUnsignedWithCAKeyStore.load(fis, CLIENT_CERT_PASS.toCharArray());
        }
        clientUnsignedWithCAKeyStore.setCertificateEntry(caAlias, caCert);
        try (FileOutputStream fos = new FileOutputStream(CLIENT_CERT_UNSIGNED_WITH_CA_P12)) {
            clientUnsignedWithCAKeyStore.store(fos, CLIENT_CERT_PASS.toCharArray());
        }
    }

    /**
     * Removes certificate files
     */
    @AfterAll
    static void cleanupCertificates() {
        for (String file : Arrays.asList(SERVER_CERT_P12, CLIENT_CERT_P12, CLIENT_CERT_KEY_PEM, CLIENT_CERT_PEM,
                CLIENT_CERT_UNSIGNED_P12, CLIENT_CERT_UNSIGNED_WITH_CA_P12, TRUST_CERT_P12, TRUST_CERT_PEM)) {
            try {
                Files.deleteIfExists(Paths.get(file));
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    /**
     * Generates the name of a certificate
     */
    static X500Name makeName(final String commonName, final String alias) {
        return new X500Name(String.format("C=FR, O=Test, OU=%s, CN=%s", alias, commonName));
    }

    /**
     * Creates a certificate, signs it with the CA and adds both to the keystore
     */
    static CertificateKeyPair makeSignedKeystore(final String commonName, final String alias, final String path,
            final String password, final KeyPair caKeyPair, final X509Certificate caCert) throws Exception {
        // Prepare the certificate
        final KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGen.initialize(2048);
        final KeyPair keyPair = keyPairGen.generateKeyPair();

        // Sign it with the CA
        final PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(
                makeName(commonName, alias), keyPair.getPublic());
        final JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder("SHA256withRSA");
        final ContentSigner signer = csBuilder.build(keyPair.getPrivate());
        final PKCS10CertificationRequest csr = p10Builder.build(signer);
        final X509Certificate newCert = signCSR(csr, caKeyPair, caCert);

        // Setup the key store
        final KeyStore serverKeyStore = KeyStore.getInstance("PKCS12");
        serverKeyStore.load(null, password.toCharArray());
        serverKeyStore.setCertificateEntry("ca", caCert);
        serverKeyStore.setKeyEntry(alias, keyPair.getPrivate(), password.toCharArray(),
                new X509Certificate[] { newCert, caCert });

        // Write it down
        try (FileOutputStream fos = new FileOutputStream(path)) {
            serverKeyStore.store(fos, password.toCharArray());
        }

        final CertificateKeyPair ckp = new CertificateKeyPair();
        ckp.certificate = newCert;
        ckp.keypair = keyPair;
        return ckp;
    }

    /**
     * Creates a self-signed certificate and stores it in a keystore
     */
    static void makeUnsignedKeyStore(final String commonName, final String alias, final String path,
            final String password) throws Exception {
        // Prepare the keys
        final KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGen.initialize(2048);
        final KeyPair unsignedKeyPair = keyPairGen.generateKeyPair();
        final KeyStore unsignedKeyStore = KeyStore.getInstance("PKCS12");
        final X509Certificate selfSignedCert = generateSelfSignedCertificate(makeName(commonName, alias),
                unsignedKeyPair, false);
        unsignedKeyStore.load(null);
        unsignedKeyStore.setKeyEntry("client_unsigned", unsignedKeyPair.getPrivate(), CLIENT_CERT_PASS.toCharArray(),
                new X509Certificate[] { selfSignedCert });

        // Write it down
        try (FileOutputStream fos = new FileOutputStream(path)) {
            unsignedKeyStore.store(fos, password.toCharArray());
        }
    }

    /**
     * Generates a self-signed certificate, which can be an authority
     */
    static X509Certificate generateSelfSignedCertificate(X500Name subject, KeyPair certPair, boolean isAuthority)
            throws Exception {
        long serial = System.currentTimeMillis();
        Date notBefore = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        Date notAfter = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
        SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(certPair.getPublic().getEncoded());

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(subject, BigInteger.valueOf(serial),
                notBefore, notAfter, subject, publicKeyInfo);

        certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(isAuthority));

        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption").build(certPair.getPrivate());

        X509CertificateHolder certHolder = certBuilder.build(signer);
        X509Certificate cert = new JcaX509CertificateConverter().getCertificate(certHolder);

        return cert;
    }

    /**
     * Sign a CSR with the given CA
     */
    static X509Certificate signCSR(PKCS10CertificationRequest csr, KeyPair caKeyPair, X509Certificate caCert)
            throws Exception {
        AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA256withRSA");
        AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);

        AsymmetricKeyParameter caPrivateKeyParameters = PrivateKeyFactory
                .createKey(caKeyPair.getPrivate().getEncoded());
        SubjectPublicKeyInfo csrPublicKeyInfo = csr.getSubjectPublicKeyInfo();

        X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(
                new X509CertificateHolder(caCert.getEncoded()).getSubject(),
                BigInteger.valueOf(System.currentTimeMillis()),
                new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000),
                new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000), csr.getSubject(), csrPublicKeyInfo);

        ContentSigner contentSigner = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(caPrivateKeyParameters);
        X509CertificateHolder certificateHolder = certificateBuilder.build(contentSigner);
        X509Certificate signedCertificate = new JcaX509CertificateConverter().getCertificate(certificateHolder);

        return signedCertificate;
    }

    /**
     * Write a certificate in a PEM file
     *
     * @param certificate Certificate to write
     * @param filename    Output file name
     * @throws IOException                  Error writing file
     * @throws FileNotFoundException        Error opening file for writing
     * @throws CertificateEncodingException Certificate encoding error
     */
    private static void writePemCertificate(Certificate certificate, String filename)
            throws FileNotFoundException, IOException, CertificateEncodingException {
        try (final PemWriter pemWriter = new PemWriter(new OutputStreamWriter(new FileOutputStream(filename)))) {
            pemWriter.writeObject(new PemObject("CERTIFICATE", certificate.getEncoded()));
        }
    }

    /**
     * Write a private key in a PEM file
     *
     * @param key      Private key
     * @param filename Output file name
     * @param password Private key password
     * @throws IOException           Error writing file
     * @throws FileNotFoundException Error opening file for writing
     */
    private static void writePemPrivateKey(final PrivateKey key, final String filename)
            throws FileNotFoundException, IOException {
        String algorithm = key.getAlgorithm();
        if (algorithm == null) {
            algorithm = "";
        }
        try (final PemWriter pemWriter = new PemWriter(new OutputStreamWriter(new FileOutputStream(filename)))) {
            pemWriter.writeObject(new PemObject(algorithm + " PRIVATE KEY", key.getEncoded()));
        }
    }

    /**
     * Sets up the Moquette broker and a client
     */
    @BeforeEach
    void start() throws Exception {
        server = new Server();
        final IConfig config = new MemoryConfig(new Properties());
        config.setProperty(IConfig.HOST_PROPERTY_NAME, "localhost");
        config.setProperty(IConfig.PORT_PROPERTY_NAME, BrokerConstants.DISABLED_PORT_BIND);
        config.setProperty(IConfig.SSL_PORT_PROPERTY_NAME, "2183");

        // WebSocket
        config.setProperty(IConfig.WSS_PORT_PROPERTY_NAME, "2184");
        config.setProperty(IConfig.WEB_SOCKET_PATH_PROPERTY_NAME, "/ws");

        // Setup SSL
        config.setProperty(IConfig.KEY_STORE_TYPE, SSLUtils.PKCS12);
        config.setProperty(IConfig.JKS_PATH_PROPERTY_NAME, SERVER_CERT_P12);
        config.setProperty(IConfig.KEY_STORE_PASSWORD_PROPERTY_NAME, SERVER_CERT_PASS);
        config.setProperty(IConfig.KEY_MANAGER_PASSWORD_PROPERTY_NAME, SERVER_CERT_PASS);

        // Client authentication
        config.setProperty(BrokerConstants.NEED_CLIENT_AUTH, "true");
        server.startServer(config);

        client = new MqttClient("ssl://localhost:2183", MqttClient.generateClientId());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        final SSLSocketFactory sslSocketFactory = SSLUtils.setupSSLSocketFactory(
                makeSslKeystoreConfig(CLIENT_CERT_P12, CLIENT_CERT_PASS, TRUST_CERT_P12, TRUST_CERT_PASS));
        options.setSocketFactory(sslSocketFactory);
        client.connect(options);
    }

    @AfterEach
    void stop() throws Exception {
        try {
            if (client.isConnected()) {
                client.disconnect();
            }
            client.close();

            for (MqttClientHandler handler : handlers) {
                handler.deactivate();
            }
        } finally {
            server.stopServer();
        }
    }

    MqttClientConfiguration makeSslKeystoreConfig(final String keystorePath, final String keystorePassword,
            final String trustStorePath, final String trustStorePassword) {
        MqttClientConfiguration mock = Mockito.mock(MqttClientConfiguration.class);
        Mockito.when(mock.auth_keystore_type()).thenReturn(SSLUtils.PKCS12);
        Mockito.when(mock.auth_keystore_path()).thenReturn(keystorePath);
        Mockito.when(mock._auth_keystore_password()).thenReturn(keystorePassword);
        Mockito.when(mock.auth_truststore_path()).thenReturn(trustStorePath);
        Mockito.when(mock._auth_truststore_password()).thenReturn(trustStorePassword);
        return mock;
    }

    MqttClientConfiguration makeSslPemConfig(final String certPath, final String certKey, final String certKeyPassword,
            final String caPath) {
        MqttClientConfiguration mock = Mockito.mock(MqttClientConfiguration.class);
        Mockito.when(mock.auth_clientcert_path()).thenReturn(certPath);
        Mockito.when(mock.auth_clientcert_key()).thenReturn(certKey);
        Mockito.when(mock._auth_clientcert_key_password()).thenReturn(certKeyPassword);
        Mockito.when(mock.auth_clientcert_ca_path()).thenReturn(caPath);
        return mock;
    }

    MqttClientHandler setupHandler(final MqttClientConfiguration baseSslConfig, final String handlerId,
            final boolean useWebsocket, final String... topics) throws Exception {
        MqttClientHandler handler = new MqttClientHandler();
        Mockito.when(baseSslConfig.id()).thenReturn(handlerId);
        Mockito.when(baseSslConfig.host()).thenReturn("localhost");
        Mockito.when(baseSslConfig.topics()).thenReturn(topics);

        if (useWebsocket) {
            Mockito.when(baseSslConfig.protocol()).thenReturn("ws");
            Mockito.when(baseSslConfig.port()).thenReturn(2184);
            Mockito.when(baseSslConfig.path()).thenReturn("/ws");
        } else {
            Mockito.when(baseSslConfig.port()).thenReturn(2183);
        }

        handler.activate(baseSslConfig);
        return handler;
    }

    final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Tests SSL client authentication
     */
    @ParameterizedTest(name = "websocket={0}")
    @ValueSource(booleans = { false, true })
    void testMqttConnect(final boolean useWebsocket) throws Exception {
        // Register a listener as a service
        final BlockingQueue<IMqttMessage> messages = new ArrayBlockingQueue<>(32);
        final IMqttMessageListener listener = (handler, topic, msg) -> {
            assertEquals(handler, msg.getHandlerId());
            messages.add(msg);
        };

        final String topic = "sensinact/mqtt/test";
        final MqttClientConfiguration config = makeSslKeystoreConfig(CLIENT_CERT_P12, CLIENT_CERT_PASS, TRUST_CERT_P12,
                TRUST_CERT_PASS);
        handlers.add(setupHandler(config, "id-auth-ssl", useWebsocket, topic));

        for (MqttClientHandler handler : handlers) {
            handler.addListener(listener,
                    Map.of(IMqttMessageListener.MQTT_TOPICS_FILTERS, new String[] { "sensinact/mqtt/test" }));
        }

        String content = "HandlerID";

        // Send a message
        client.publish(topic, content.getBytes(StandardCharsets.UTF_8), 1, false);

        // Wait a bit (we should get only 1 message)
        final IMqttMessage msg = messages.poll(1, TimeUnit.SECONDS);
        assertNotNull(msg);
        assertEquals(0, messages.size());
        assertEquals("id-auth-ssl", msg.getHandlerId());
        assertEquals(topic, msg.getTopic());
        assertEquals(content, new String(msg.getPayload(), StandardCharsets.UTF_8));
    }

    /**
     * Ensure we are rejected correctly
     */
    @ParameterizedTest(name = "websocket={0}")
    @ValueSource(booleans = { false, true })
    void testMqttRejectUnsigned(final boolean useWebsocket) throws Exception {
        for (String certFile : List.of(CLIENT_CERT_UNSIGNED_P12, CLIENT_CERT_UNSIGNED_WITH_CA_P12)) {
            final MqttClientConfiguration config = makeSslKeystoreConfig(certFile, CLIENT_CERT_PASS, TRUST_CERT_P12,
                    TRUST_CERT_PASS);
            final MqttException ex = assertThrows(MqttException.class,
                    () -> setupHandler(config, "id-auth-ssl", useWebsocket, "sensinact/mqtt/test"));
            assertInstanceOf(SSLHandshakeException.class, ex.getCause());
        }
    }

    /**
     * Ensure we can load PEM files
     */
    @ParameterizedTest(name = "websocket={0}")
    @ValueSource(booleans = { false, true })
    void testMqttConnectPem(final boolean useWebsocket) throws Exception {
        // Register a listener as a service
        final BlockingQueue<IMqttMessage> messages = new ArrayBlockingQueue<>(32);
        final IMqttMessageListener listener = (handler, topic, msg) -> {
            assertEquals(handler, msg.getHandlerId());
            messages.add(msg);
        };

        final String topic = "sensinact/mqtt/test";
        final MqttClientConfiguration config = makeSslPemConfig(CLIENT_CERT_PEM, CLIENT_CERT_KEY_PEM, CLIENT_CERT_PASS,
                TRUST_CERT_PEM);
        handlers.add(setupHandler(config, "id-auth-ssl", useWebsocket, topic));

        for (MqttClientHandler handler : handlers) {
            handler.addListener(listener,
                    Map.of(IMqttMessageListener.MQTT_TOPICS_FILTERS, new String[] { "sensinact/mqtt/test" }));
        }

        String content = "HandlerID";

        // Send a message
        client.publish(topic, content.getBytes(StandardCharsets.UTF_8), 1, false);

        // Wait a bit (we should get only 1 message)
        final IMqttMessage msg = messages.poll(1, TimeUnit.SECONDS);
        assertNotNull(msg);
        assertEquals(0, messages.size());
        assertEquals("id-auth-ssl", msg.getHandlerId());
        assertEquals(topic, msg.getTopic());
        assertEquals(content, new String(msg.getPayload(), StandardCharsets.UTF_8));
    }
}
