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

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Utility class to smooth the handling of trusted certificates
 */
public class ChainedTrustManagers implements X509TrustManager {

    /**
     * Configured trust managers (ordered)
     */
    private List<X509TrustManager> trustManagers = new ArrayList<X509TrustManager>();

    /**
     * Allow expired certificates
     */
    private final boolean allowExpired;

    /**
     * Sets up a chain of trust managers. Certificates are checked the standard Java
     * way
     *
     * @param trustManagers Trust managers to chain
     */
    public ChainedTrustManagers(final TrustManager... trustManagers) {
        this(false, trustManagers);
    }

    /**
     * Sets up a chain of trust managers. Expired certificates can be considered
     * valid
     *
     * @param allowExpired  If true, expired certificates are considered valid
     * @param trustManagers Trust managers to chain
     */
    public ChainedTrustManagers(final boolean allowExpired, final TrustManager... trustManagers) {
        this.allowExpired = allowExpired;
        for (final TrustManager subManager : trustManagers) {
            if (subManager instanceof X509TrustManager) {
                this.trustManagers.add((X509TrustManager) subManager);
            }
        }
    }

    /**
     * Sets up a chain of trust managers. Certificates are checked the standard Java
     * way.
     *
     * @param trustManagers Trust managers to chain
     */
    public ChainedTrustManagers(final TrustManager[]... trustManagers) {
        this(false, trustManagers);
    }

    /**
     * Sets up a chain of trust managers. Expired certificates can be considered
     * valid
     *
     * @param allowExpired  If true, expired certificates are considered valid
     * @param trustManagers Trust managers to chain
     */
    public ChainedTrustManagers(final boolean allowExpired, final TrustManager[]... trustManagers) {
        this.allowExpired = allowExpired;
        for (final TrustManager[] subManagers : trustManagers) {
            for (final TrustManager subManager : subManagers) {
                if (subManager instanceof X509TrustManager) {
                    this.trustManagers.add((X509TrustManager) subManager);
                }
            }
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (allowExpired) {
            chain = disableExpirationCheck(chain);
        }

        CertificateException lastEx = null;
        for (X509TrustManager trustManager : trustManagers) {
            try {
                trustManager.checkClientTrusted(chain, authType);
                // Passed!
                return;
            } catch (CertificateException e) {
                lastEx = e;
            }
        }

        if (lastEx != null) {
            throw lastEx;
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (allowExpired) {
            chain = disableExpirationCheck(chain);
        }

        CertificateException lastEx = null;
        for (X509TrustManager trustManager : trustManagers) {
            try {
                trustManager.checkServerTrusted(chain, authType);
                // Passed!
                return;
            } catch (CertificateException e) {
                lastEx = e;
            }
        }

        if (lastEx != null) {
            throw lastEx;
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        final List<X509Certificate> issuers = new ArrayList<X509Certificate>();
        for (X509TrustManager trustManager : trustManagers) {
            issuers.addAll(Arrays.asList(trustManager.getAcceptedIssuers()));
        }
        return issuers.toArray(new X509Certificate[0]);
    }

    /**
     * Disables the expiration check on the given certificates
     *
     * @param chain The certificates for which to disable the expiration check
     * @return A chain of new certificates with no expiration check
     */
    public X509Certificate[] disableExpirationCheck(final X509Certificate[] chain) {
        final X509Certificate[] newChain = new X509Certificate[chain.length];
        for (int i = 0; i < chain.length; i++) {
            newChain[i] = disableExpirationCheck(chain[i]);
        }
        return newChain;
    }

    /**
     * Creates a proxy of the given certificate, disabling the expiration checks.
     *
     * @param certificate A certificate
     * @return A proxy of the certificate with no expiration check
     */
    private X509Certificate disableExpirationCheck(final X509Certificate certificate) {

        return new X509Certificate() {
            @Override
            public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {
                // Ignore
            }

            @Override
            public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
                // Ignore
            }

            @Override
            public boolean hasUnsupportedCriticalExtension() {
                return certificate.hasUnsupportedCriticalExtension();
            }

            @Override
            public Set<String> getNonCriticalExtensionOIDs() {
                return certificate.getNonCriticalExtensionOIDs();
            }

            @Override
            public byte[] getExtensionValue(String oid) {
                return certificate.getExtensionValue(oid);
            }

            @Override
            public Set<String> getCriticalExtensionOIDs() {
                return certificate.getCriticalExtensionOIDs();
            }

            @Override
            public void verify(PublicKey key, String sigProvider) throws CertificateException, NoSuchAlgorithmException,
                    InvalidKeyException, NoSuchProviderException, SignatureException {
                certificate.verify(key, sigProvider);
            }

            @Override
            public void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException,
                    InvalidKeyException, NoSuchProviderException, SignatureException {
                certificate.verify(key);
            }

            @Override
            public String toString() {
                return certificate.toString();
            }

            @Override
            public PublicKey getPublicKey() {
                return certificate.getPublicKey();
            }

            @Override
            public byte[] getEncoded() throws CertificateEncodingException {
                return certificate.getEncoded();
            }

            @Override
            public int getVersion() {
                return certificate.getVersion();
            }

            @Override
            public byte[] getTBSCertificate() throws CertificateEncodingException {
                return certificate.getTBSCertificate();
            }

            @Override
            public boolean[] getSubjectUniqueID() {
                return certificate.getSubjectUniqueID();
            }

            @Override
            public Principal getSubjectDN() {
                return certificate.getSubjectDN();
            }

            @Override
            public byte[] getSignature() {
                return certificate.getSignature();
            }

            @Override
            public byte[] getSigAlgParams() {
                return certificate.getSigAlgParams();
            }

            @Override
            public String getSigAlgOID() {
                return certificate.getSigAlgOID();
            }

            @Override
            public String getSigAlgName() {
                return certificate.getSigAlgName();
            }

            @Override
            public BigInteger getSerialNumber() {
                return certificate.getSerialNumber();
            }

            @Override
            public Date getNotBefore() {
                return certificate.getNotBefore();
            }

            @Override
            public Date getNotAfter() {
                return certificate.getNotAfter();
            }

            @Override
            public boolean[] getKeyUsage() {
                return certificate.getKeyUsage();
            }

            @Override
            public boolean[] getIssuerUniqueID() {
                return certificate.getIssuerUniqueID();
            }

            @Override
            public Principal getIssuerDN() {
                return certificate.getIssuerDN();
            }

            @Override
            public int getBasicConstraints() {
                return certificate.getBasicConstraints();
            }
        };
    }
}
