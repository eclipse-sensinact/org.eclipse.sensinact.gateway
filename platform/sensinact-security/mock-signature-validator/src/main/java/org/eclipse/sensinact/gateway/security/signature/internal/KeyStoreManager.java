/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.security.signature.internal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

/**
 * Implementation of the KeyStoreManager service, that
 * defines methods for accessing a keystore
 *
 * @author pierre parrend
 */
public class KeyStoreManager {
    private static final String KEYSTORE_TYPE = "jks";
    private final KeyStore keystore;

    /**
     * Constructor
     *
     * @param keystoreFile
     * @param keyStorePassword
     * @throws KeyStoreManagerException
     */
    public KeyStoreManager(String keystoreFile, String keyStorePassword) throws KeyStoreManagerException {
        //System.out.println("KEYSTORE : " + keystoreFile + " / PASSWD : " + keyStorePassword);
        try {
            this.keystore = KeyStore.getInstance(KEYSTORE_TYPE);
            final InputStream iStream = (InputStream) new FileInputStream(keystoreFile);
            keystore.load(iStream, keyStorePassword.toCharArray());
        } catch (Exception e) {
            throw new KeyStoreManagerException(e);
        }
    }

    /**
     * A method for retrieving a public Key Certificate
     * stored in the KeyStore
     *
     * @param subject
     * @return Certificate
     */
    public Certificate getCertificate(String subject) throws CertificateException, NoSuchAlgorithmException, FileNotFoundException, KeyStoreException, IOException {

        Certificate cert = null;
        cert = keystore.getCertificate(subject);
        return cert;
    }

    /**
     * @param subject
     * @param keyPasswd
     * @return PrivateKey
     */
    public PrivateKey getPrivateKey(final String subject, final String keyPasswd) throws CertificateException, NoSuchAlgorithmException, FileNotFoundException, KeyStoreException, UnrecoverableKeyException, IOException {

        PrivateKey priv = (PrivateKey) keystore.getKey(subject, keyPasswd.toCharArray());
        return priv;
    }

    public boolean isValid(final Certificate cert) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateNotYetValidException, CertificateExpiredException, CertificateException {
        boolean valid = false;
        final String alias = keystore.getCertificateAlias(cert);
        if (alias != null) {
            valid = this.isTemporallyOK(cert);
        }
        return valid;
    }

    public boolean isTemporallyOK(final Certificate cert) throws CertificateExpiredException, CertificateNotYetValidException {
        boolean valid = false;
        final X509Certificate x509 = (X509Certificate) cert;
        //check temporal validity
        try {
            x509.checkValidity();
            //if no Exception is thrown
            valid = true;
        } catch (CertificateExpiredException e) {
            e.printStackTrace();
        } catch (CertificateNotYetValidException e) {
            e.printStackTrace();
        }
        return valid;
    }
}
