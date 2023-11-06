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
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
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
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.sensinact.gateway.southbound.mqtt.api.IMqttMessage;
import org.eclipse.sensinact.gateway.southbound.mqtt.api.IMqttMessageListener;
import org.eclipse.sensinact.gateway.southbound.mqtt.impl.MqttClientConfiguration;
import org.eclipse.sensinact.gateway.southbound.mqtt.impl.MqttClientHandler;
import org.eclipse.sensinact.gateway.southbound.mqtt.impl.SSLUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    private static final String SERVER_CERT = "src/test/resources/server.p12";
    private static final String SERVER_CERT_PASS = "secret";

    private static final String CLIENT_CERT = "src/test/resources/client.p12";
    private static final String CLIENT_CERT_UNSIGNED = "src/test/resources/client_unsigned.p12";
    private static final String CLIENT_CERT_UNSIGNED_WITH_CA = "src/test/resources/client_unsigned_with_ca.p12";
    private static final String CLIENT_CERT_PASS = "titi21";

    private static final String TRUST_CERT = "src/test/resources/ca.p12";
    private static final String TRUST_CERT_PASS = "secret";

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
        try (FileOutputStream fos = new FileOutputStream(TRUST_CERT)) {
            caTrustStore.store(fos, TRUST_CERT_PASS.toCharArray());
        }

        // Prepare the server certificate & sign it with the CA
        makeSignedKeystore("localhost", "server", SERVER_CERT, SERVER_CERT_PASS, caKeyPair, caCert);

        // Prepare the server certificate & sign it with the CA
        makeSignedKeystore("Signed Client", "client", CLIENT_CERT, CLIENT_CERT_PASS, caKeyPair, caCert);

        // Prepare an unsigned client key pair
        makeUnsignedKeyStore("Self-Signed Client", "client", CLIENT_CERT_UNSIGNED, CLIENT_CERT_PASS);

        // Same thing, but with the CA certificate in the store
        final KeyStore clientUnsignedWithCAKeyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(CLIENT_CERT_UNSIGNED)) {
            clientUnsignedWithCAKeyStore.load(fis, CLIENT_CERT_PASS.toCharArray());
        }
        clientUnsignedWithCAKeyStore.setCertificateEntry(caAlias, caCert);
        try (FileOutputStream fos = new FileOutputStream(CLIENT_CERT_UNSIGNED_WITH_CA)) {
            clientUnsignedWithCAKeyStore.store(fos, CLIENT_CERT_PASS.toCharArray());
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
    static void makeSignedKeystore(final String commonName, final String alias, final String path,
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
     * Sets up the Moquette broker and a client
     */
    @BeforeEach
    void start() throws Exception {
        server = new Server();
        final IConfig config = new MemoryConfig(new Properties());
        config.setProperty(BrokerConstants.HOST_PROPERTY_NAME, "localhost");
        config.setProperty(BrokerConstants.PORT_PROPERTY_NAME, BrokerConstants.DISABLED_PORT_BIND);
        config.setProperty(BrokerConstants.SSL_PORT_PROPERTY_NAME, "2183");

        // Setup SSL
        config.setProperty(BrokerConstants.KEY_STORE_TYPE, "pkcs12");
        config.setProperty(BrokerConstants.JKS_PATH_PROPERTY_NAME, SERVER_CERT);
        config.setProperty(BrokerConstants.KEY_STORE_PASSWORD_PROPERTY_NAME, SERVER_CERT_PASS);
        config.setProperty(BrokerConstants.KEY_MANAGER_PASSWORD_PROPERTY_NAME, SERVER_CERT_PASS);

        // Client authentication
        config.setProperty(BrokerConstants.NEED_CLIENT_AUTH, "true");
        server.startServer(config);

        client = new MqttClient("ssl://localhost:2183", MqttClient.generateClientId());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        final SSLSocketFactory sslSocketFactory = SSLUtils.setupSSLSocketFactory("pkcs12",
                new FileInputStream(CLIENT_CERT), CLIENT_CERT_PASS.toCharArray(), new FileInputStream(TRUST_CERT),
                TRUST_CERT_PASS.toCharArray());
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

    MqttClientHandler setupHandler(final String handlerId, final String clientCert, final String clientCertPassword,
            final String... topics) throws Exception {
        MqttClientHandler handler = new MqttClientHandler();
        MqttClientConfiguration mock = Mockito.mock(MqttClientConfiguration.class);
        Mockito.when(mock.id()).thenReturn(handlerId);
        Mockito.when(mock.host()).thenReturn("localhost");
        Mockito.when(mock.port()).thenReturn(2183);
        Mockito.when(mock.auth_keystore_type()).thenReturn("pkcs12");
        Mockito.when(mock.auth_keystore_path()).thenReturn(clientCert);
        Mockito.when(mock._auth_keystore_password()).thenReturn(clientCertPassword);
        Mockito.when(mock.topics()).thenReturn(topics);
        handler.activate(mock);
        return handler;
    }

    final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Tests SSL client authentication
     */
    @Test
    void testMqttConnect() throws Exception {
        // Register a listener as a service
        final BlockingQueue<IMqttMessage> messages = new ArrayBlockingQueue<>(32);
        final IMqttMessageListener listener = (handler, topic, msg) -> {
            assertEquals(handler, msg.getHandlerId());
            messages.add(msg);
        };

        final String topic = "sensinact/mqtt/test";
        handlers.add(setupHandler("id-auth-ssl", CLIENT_CERT, CLIENT_CERT_PASS, topic));

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
    @Test
    void testMqttRejectUnsigned() throws Exception {
        for (String certFile : List.of(CLIENT_CERT_UNSIGNED, CLIENT_CERT_UNSIGNED_WITH_CA)) {
            final MqttException ex = assertThrows(MqttException.class,
                    () -> setupHandler("id-auth-ssl", certFile, CLIENT_CERT_PASS, "sensinact/mqtt/test"));
            assertInstanceOf(SSLHandshakeException.class, ex.getCause());
        }
    }
}
