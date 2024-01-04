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

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * Utility class to setup key and trust stores from PEM files
 */
public class PEMUtils {

    /**
     * Default key algorithm to use if not explicitly given in file
     */
    private static final String DEFAULT_KEY_ALG = "RSA";

    /**
     * Loads an X.509 certificate from the given stream
     *
     * @param inSteam Input stream
     * @return The loaded certificate
     * @throws CertificateException Error loading certificate
     */
    public static Certificate loadCertificate(final InputStream inSteam) throws CertificateException {
        return loadCertificate(inSteam, "X.509");
    }

    /**
     * Loads a certificate from the given stream
     *
     * @param inSteam Input stream
     * @param type    Type of certificate
     * @return The loaded certificate
     * @throws CertificateException Error loading certificate
     */
    public static Certificate loadCertificate(final InputStream inSteam, final String type)
            throws CertificateException {
        final CertificateFactory factory = CertificateFactory.getInstance(type);
        return factory.generateCertificate(inSteam);
    }

    /**
     * Loads a private key trying to determine its type
     *
     * @param inStream PEM file content stream
     * @return The loaded private key
     * @throws NoSuchAlgorithmException Unsupported key type
     * @throws InvalidKeySpecException  Invalid key
     * @throws IOException              Error reading stream
     */
    public static PrivateKey loadPrivateKey(final InputStream inStream)
            throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        return loadPrivateKey(new String(inStream.readAllBytes()));
    }

    /**
     * Loads a private key from the content of a PEM file (those starting with
     * <code>----- BEGIN...</code>), trying to determine its type
     *
     * @param pemContent Content of the PEM file
     * @return The loaded private key
     * @throws NoSuchAlgorithmException Unsupported key type
     * @throws InvalidKeySpecException  Invalid key
     */
    public static PrivateKey loadPrivateKey(final String pemContent)
            throws InvalidKeySpecException, NoSuchAlgorithmException {
        if (pemContent == null || pemContent.isBlank()) {
            throw new InvalidKeySpecException("No key given");
        }

        // Look for the first line
        final String beginMarker = "-----BEGIN ";
        final int beginIdx = pemContent.indexOf(beginMarker);
        if (beginIdx == -1) {
            throw new InvalidKeySpecException("Key had no BEGIN marker");
        }

        final String beginEOLMarker = " PRIVATE KEY-----";
        int endOfLineIdx = pemContent.indexOf(beginEOLMarker, beginIdx);
        if (endOfLineIdx == -1) {
            throw new InvalidKeySpecException("Key had an invalid BEGIN marker");
        }

        final int algorithmIdx = beginIdx + beginMarker.length();
        final String algorithm;
        if (algorithmIdx >= endOfLineIdx) {
            // No algorithm given, consider RSA
            algorithm = DEFAULT_KEY_ALG;
        } else {
            final String tmpAlgorithm = pemContent.substring(algorithmIdx, endOfLineIdx).strip();
            if (tmpAlgorithm.isEmpty()) {
                algorithm = DEFAULT_KEY_ALG;
            } else {
                algorithm = tmpAlgorithm;
            }
        }

        // Compute the start index
        final int keyStartIdx = endOfLineIdx + beginEOLMarker.length();

        // Look for the end
        final int keyEndIdx = pemContent.indexOf("-----END ", keyStartIdx);
        if (keyEndIdx == -1) {
            throw new InvalidKeySpecException("Key have no END marker");
        }

        // Extract the Base64 key and remove the spaces/new lines
        final String b64key = pemContent.substring(keyStartIdx, keyEndIdx).strip().replaceAll("[\r\n ]", "");
        final byte[] rawKey = Base64.getDecoder().decode(b64key);
        return loadPrivateKey(rawKey, algorithm);
    }

    /**
     * Loads a private key with the given algorithm (those starting with
     * <code>----- BEGIN...</code>). Tries to detect the algorithm if none given
     *
     * @param inStream  PEM file content stream
     * @param algorithm Explicit key algorithm
     * @return The loaded private key
     * @throws NoSuchAlgorithmException Unsupported key type
     * @throws InvalidKeySpecException  Invalid key
     * @throws IOException              Error reading stream
     */
    public static PrivateKey loadPrivateKey(final InputStream inStream, final String algorithm)
            throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        return loadPrivateKey(new String(inStream.readAllBytes()), algorithm);
    }

    /**
     * Loads a private key with the given algorithm (those starting with
     * <code>----- BEGIN...</code>). Tries to detect the algorithm if none given
     *
     * @param pemContent Content of the PEM file
     * @param algorithm  Explicit key algorithm
     * @return The loaded private key
     * @throws NoSuchAlgorithmException Unsupported key type
     * @throws InvalidKeySpecException  Invalid key
     */
    public static PrivateKey loadPrivateKey(final String pemContent, final String algorithm)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (pemContent == null || pemContent.isBlank()) {
            throw new InvalidKeySpecException("No key given");
        }

        if (algorithm == null || algorithm.isBlank()) {
            return loadPrivateKey(pemContent);
        }

        // Extract the Base64 key and remove the spaces/new lines
        final String b64key = pemContent.replaceAll("-----[\\-]+-----", "").replaceAll("[\r\n ]", "");
        final byte[] rawKey = Base64.getDecoder().decode(b64key);
        return loadPrivateKey(rawKey, algorithm);
    }

    /**
     * Loads a private key from the given PKCS8 encoded key (decoded bytes)
     *
     * @param pkcs8key  The raw key in PKCS8 format
     * @param algorithm Key algorithm
     * @return The loaded key
     * @throws NoSuchAlgorithmException Unsupported key type
     * @throws InvalidKeySpecException  Invalid key
     */
    public static PrivateKey loadPrivateKey(final byte[] pkcs8key, final String algorithm)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        final PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(pkcs8key);
        final KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        return keyFactory.generatePrivate(ks);
    }
}
