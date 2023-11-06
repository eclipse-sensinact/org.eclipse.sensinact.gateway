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
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class SSLUtils {

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
     */
    public static SSLSocketFactory setupSSLSocketFactory(final MqttClientConfiguration config)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException,
            KeyManagementException, UnrecoverableKeyException {

        // Get the password
        char[] password = null;
        if (config._auth_keystore_password() != null) {
            password = config._auth_keystore_password().toCharArray();
        }

        char[] caPassword = null;
        if (config._auth_truststore_password() != null) {
            caPassword = config._auth_truststore_password().toCharArray();
        }

        FileInputStream storeFile = null;
        FileInputStream caFile = null;
        try {
            storeFile = new FileInputStream(config.auth_keystore_path());

            final String trustStorePath = config.auth_truststore_path();
            if (trustStorePath != null && !trustStorePath.isBlank()) {
                caFile = new FileInputStream(trustStorePath);
            } else {
                caFile = null;
            }

            return setupSSLSocketFactory(config.auth_keystore_type(), storeFile, password, caFile, caPassword);
        } finally {
            if (storeFile != null) {
                storeFile.close();
            }

            if (caFile != null) {
                caFile.close();
            }
        }
    }

    /**
     * Prepares an {@link SSLSocketFactory} from the configuration
     *
     * @param keyStoreType   Type of key store (PKCS12, JKS, ...)
     * @param keyStoreStream Key store input stream
     * @param password       Key store password
     * @return An SSLSocketFactory
     * @throws KeyStoreException         Error setting up the key store
     * @throws IOException               Error reading certificate
     * @throws CertificateException      Error parsing certificate
     * @throws NoSuchAlgorithmException  Unsupported certificate
     * @throws KeyManagementException    Error setting up the SSL context
     * @throws UnrecoverableKeyException Error reading keys
     */
    public static SSLSocketFactory setupSSLSocketFactory(final String keyStoreType, final InputStream keyStoreStream,
            final char[] password, final InputStream trustStoreStream, final char[] trustStorePassword)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException,
            KeyManagementException, UnrecoverableKeyException {

        // Load the key store
        final KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(keyStoreStream, password);

        final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, password);

        // Load the trust store
        final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        if (trustStoreStream != null) {
            // Use a distinct trust store
            final KeyStore trustStore = KeyStore.getInstance(keyStoreType);
            trustStore.load(trustStoreStream, trustStorePassword);
            tmf.init(trustStore);
        } else {
            // Use the key store as trust store
            tmf.init(keyStore);
        }

        final SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
        return sslContext.getSocketFactory();
    }

}
