/*********************************************************************
* Copyright (c) 2023 Kentyou.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Thomas Calmant (Kentyou) - initial implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.mqtt.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * SSL utility methods
 */
public class SSLUtils {

    /**
     * Use a constant to avoid typos
     */
    public static final String PKCS12 = "PKCS12";

    /**
     * Loading status holder
     */
    private static class CertKeys {
        /**
         * Loaded trust store
         */
        private KeyStore trustStore;

        /**
         * Certificate authority for the client certificate
         */
        private Certificate caCertificate;

        /**
         * Loaded key store
         */
        private KeyStore keyStore;

        /**
         * Private key / keystore password
         */
        private char[] keyPassword;
    }

    /**
     * Clears the content of the given array
     */
    private static void clearArray(final char[] array) {
        if (array != null) {
            Arrays.fill(array, (char) 0);
        }
    }

    /**
     * Loads the client key store
     *
     * @param config MQTT client configuration
     * @param keys   Loading status holder
     * @return The loaded key store
     * @throws KeyStoreException        Error creating/loading key store or no key
     *                                  store configuration found
     * @throws NoSuchAlgorithmException Error loading client certificate/key
     * @throws CertificateException     Error loading client certificate
     * @throws IOException              Error reading keys
     * @throws InvalidKeySpecException  Error loading keys
     */
    public static void loadKeyStore(final MqttClientConfiguration config, final CertKeys keys) throws KeyStoreException,
            NoSuchAlgorithmException, CertificateException, IOException, InvalidKeySpecException {
        // Get keystore type
        final String rawKeyStoreType = config.auth_keystore_type();
        final String keystoreType = rawKeyStoreType != null ? rawKeyStoreType.strip() : PKCS12;

        final KeyStore keyStore = KeyStore.getInstance(keystoreType);

        // Load the given keystore
        final String keystorePath = config.auth_keystore_path();
        final String clientCertPath = config.auth_clientcert_path();
        final String clientKeyPath = config.auth_clientcert_key();

        if (keystorePath != null && !keystorePath.isBlank()) {
            // Load the keystore directly
            final String keystorePassword = config._auth_keystore_password();
            keys.keyPassword = keystorePassword != null ? keystorePassword.toCharArray() : null;
            try (final FileInputStream inStream = new FileInputStream(keystorePath)) {
                keyStore.load(inStream, keys.keyPassword);
            }
        } else if (clientCertPath != null && !clientCertPath.isBlank() && clientKeyPath != null
                && !clientKeyPath.isBlank()) {
            // Setup the keystore
            final String clientKeyPassword = config._auth_clientcert_key_password();
            keys.keyPassword = clientKeyPassword != null ? clientKeyPassword.toCharArray() : null;
            keyStore.load(null, keys.keyPassword);

            // Add the CA if available
            if (keys.caCertificate != null) {
                keyStore.setCertificateEntry("ca", keys.caCertificate);
            }

            // Load the PEM client certificate and its private key
            final Certificate clientCert;
            try (final FileInputStream inStream = new FileInputStream(clientCertPath)) {
                clientCert = PEMUtils.loadCertificate(inStream);
            }

            // Load & store the private key
            try (final FileInputStream inStream = new FileInputStream(clientKeyPath)) {
                keyStore.setKeyEntry("client",
                        PEMUtils.loadPrivateKey(inStream, config.auth_clientcert_key_algorithm()), keys.keyPassword,
                        List.of(clientCert, keys.caCertificate).stream().filter(Objects::nonNull)
                                .toArray(Certificate[]::new));
            }
        } else {
            throw new KeyStoreException("No client authentication configuration given");
        }

        keys.keyStore = keyStore;
    }

    /**
     * Loads the trust store, if given
     *
     * @param config MQTT client configuration
     * @param keys   Loading status holder
     * @throws KeyStoreException        Error loading key store
     * @throws NoSuchAlgorithmException Error loading key store
     * @throws CertificateException     Error loading certificate
     * @throws IOException              Error reading key store or certificate
     */
    public static void loadTrustStore(final MqttClientConfiguration config, final CertKeys keys)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        // Get keystore type
        final String rawKeyStoreType = config.auth_truststore_type();
        final String keystoreType = rawKeyStoreType != null ? rawKeyStoreType.strip() : PKCS12;

        final KeyStore trustStore = KeyStore.getInstance(keystoreType);
        trustStore.load(null);

        // Load the given trust store
        final String truststorePath = config.auth_truststore_path();
        if (truststorePath != null && !truststorePath.isBlank()) {
            // Load the truststore directly
            char[] trustStorePassword = null;
            if (config._auth_truststore_password() != null) {
                trustStorePassword = config._auth_truststore_password().toCharArray();
            }

            // Load it
            try (final FileInputStream inStream = new FileInputStream(truststorePath)) {
                trustStore.load(inStream, trustStorePassword);
            } finally {
                clearArray(trustStorePassword);
            }
        }

        // Load the explicitly given CA certificate
        final String caCertPath = config.auth_ca_path();
        if (caCertPath != null && !caCertPath.isBlank()) {
            // Load the PEM client certificate and its private key
            final Certificate caCert;
            try (final FileInputStream inStream = new FileInputStream(caCertPath)) {
                caCert = PEMUtils.loadCertificate(inStream);
                keys.caCertificate = caCert;
                trustStore.setCertificateEntry("ca", caCert);
            }
        }

        keys.trustStore = trustStore;
    }

    /**
     * Prepares an {@link SSLSocketFactory} from the configuration
     *
     * @param config MQTT client configuration
     * @return An SSLSocketFactory
     * @throws KeyStoreException         Error setting up the key store
     * @throws IOException               Error reading certificate
     * @throws CertificateException      Error parsing certificate
     * @throws NoSuchAlgorithmException  Unsupported certificate
     * @throws KeyManagementException    Error setting up the SSL context
     * @throws UnrecoverableKeyException Error reading keys
     * @throws InvalidKeySpecException   Unreadable key
     */
    public static SSLSocketFactory setupSSLSocketFactory(final MqttClientConfiguration config)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException,
            KeyManagementException, UnrecoverableKeyException, InvalidKeySpecException {

        final CertKeys keys = new CertKeys();
        try {
            loadTrustStore(config, keys);
            loadKeyStore(config, keys);
            return setupSSLSocketFactory(keys);
        } finally {
            clearArray(keys.keyPassword);
        }
    }

    /**
     * Prepares an {@link SSLSocketFactory} from the configuration
     *
     * @param keys Loaded key stores and associated configuration
     * @return An SSLSocketFactory
     * @throws KeyStoreException         Error setting up the key store
     * @throws IOException               Error reading certificate
     * @throws CertificateException      Error parsing certificate
     * @throws NoSuchAlgorithmException  Unsupported certificate
     * @throws KeyManagementException    Error setting up the SSL context
     * @throws UnrecoverableKeyException Error reading keys
     */
    private static SSLSocketFactory setupSSLSocketFactory(final CertKeys keys) throws KeyStoreException, IOException,
            NoSuchAlgorithmException, CertificateException, KeyManagementException, UnrecoverableKeyException {

        // Prepare the key manager
        final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keys.keyStore, keys.keyPassword);

        // Prepare the trust store
        if (!keys.trustStore.aliases().hasMoreElements()) {
            // Trust store is empty, use the key store instead
            keys.trustStore = keys.keyStore;
        }

        final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keys.trustStore);

        final SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
        return sslContext.getSocketFactory();
    }
}
