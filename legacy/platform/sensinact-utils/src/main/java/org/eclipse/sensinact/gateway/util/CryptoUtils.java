/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.util;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Cryptographic helper methods
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class CryptoUtils {
    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static final SecureRandom SECURE_RANDOM;
    public static final MessageDigest SHA1;
    public static final MessageDigest SHA256;
    public static final MessageDigest MD5;

    static {
        SecureRandom random = null;
        MessageDigest sha1Digest = null;
        MessageDigest sha256Digest = null;
        MessageDigest md5Digest = null;
        try {
            //Initialize SecureRandom
            //This is a lengthy operation, to be done only upon
            //initialization of the application
            random = SecureRandom.getInstance("SHA1PRNG");
            sha1Digest = MessageDigest.getInstance("SHA-1");
            sha256Digest = MessageDigest.getInstance("SHA-256");
            md5Digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace(System.err);
        }
        SECURE_RANDOM = random;
        SHA1 = sha1Digest;
        SHA256 = sha256Digest;
        MD5 = md5Digest;

        random = null;
        sha1Digest = null;
        sha256Digest = null;
        md5Digest = null;
    }

    public static final String cryptWithMD5(String pass) throws InvalidKeyException {
        if (MD5 == null) {
            throw new InvalidKeyException("Algorithm MD5 not implemented");
        }
        try {
            //clone the MessageDigest to avoid multithreading congestion
            MessageDigest mydigest = (MessageDigest) MD5.clone();
            mydigest.reset();
            byte[] digested = mydigest.digest(pass.getBytes());
            return String.format("%032X", new BigInteger(1, digested)).toLowerCase();
        } catch (CloneNotSupportedException e) {
            throw new InvalidKeyException(e);
        }
    }

    /**
     * @throws InvalidKeyException if an error occurred while generating
     *                             a token bytes array
     */
    public static final String createToken() throws InvalidKeyException {
        if (SECURE_RANDOM == null || SHA1 == null) {
            throw new InvalidKeyException();
        }
        String token = null;
        String randomNum = new Integer(SECURE_RANDOM.nextInt()).toString();

        byte[] aInput = SHA1.digest(randomNum.getBytes());

        StringBuilder result = new StringBuilder();
        for (int idx = 0; idx < aInput.length; ++idx) {
            byte b = aInput[idx];
            result.append(DIGITS[(b & 0xf0) >> 4]);
            result.append(DIGITS[b & 0x0f]);
        }
        token = result.toString();
        return token;
    }

    /**
     * @throws InvalidKeyException if an error occurred while generating
     *                             a public key
     */
    public static final String createToken(String identifier) throws InvalidKeyException {
        if (SECURE_RANDOM == null || SHA1 == null) {
            throw new InvalidKeyException();
        }
        String token = null;
        byte[] aInput = SHA1.digest(identifier.getBytes());

        long current = System.currentTimeMillis();
        byte[] currentBytes = new byte[8];
        for (int i = 7; i >= 0; i--) {
            currentBytes[i] = (byte) (current & 0xFF);
            current >>= 8;
        }
        int index = 0;
        int length = aInput.length;
        int pos = 0;

        StringBuilder result = new StringBuilder();

        for (; index < length; index++) {
            if (pos == currentBytes.length) {
                pos = 0;
            }
            byte b = (byte) (aInput[index] + currentBytes[pos]);
            result.append(DIGITS[(b & 0xf0) >> 4]);
            result.append(DIGITS[(b & 0x0f)]);
            pos++;
        }
        token = result.toString();
        return token;
    }

    public static MessageDigest getDigest(String algo) {
        // "SHA1-Digest" should not be necessary. Extensive tests should be
        // available to allow for its removal
        if ("SHA1-Digest".equals(algo) || "SHA-1".equals(algo) || "SHA1".equals(algo)) {
            return CryptoUtils.SHA1;

        } else if ("SHA-256".equals(algo) || "SHA-256-Digest".equals(algo) || "SHA256".equals(algo)) {
            return CryptoUtils.SHA256;

        } else if ("MD5".equals(algo) || "MD5-Digest".equals(algo) || "MD-5".equals(algo)) {
            return CryptoUtils.MD5;
        }
        return null;
    }

    /**
     * @throws InvalidKeyException if an error occurred while generating
     *                             a public key
     */
    public static final void main(String[] args) throws InvalidKeyException {
        String token = createToken(args[0]);
        System.out.println(token);
    }
}
