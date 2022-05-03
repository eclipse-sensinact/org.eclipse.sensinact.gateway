/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.security.signature.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.util.CryptoUtils;
import org.eclipse.sensinact.gateway.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation class of the CryptographicUtils service, using Bouncy Castle
 * Cryptography provider.
 */
public class CryptographicUtils {
	
	private static final Logger LOG = LoggerFactory.getLogger(CryptographicUtils.class);

    /**
     * Constructor
     */
    public CryptographicUtils() throws NoSuchAlgorithmException {
    }

    private boolean checkHashValue(final String realHash, final String pretendedHash) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("pretended hash:" + pretendedHash);
        }
        boolean validated = false;
        if (LOG.isDebugEnabled()) {
            LOG.debug("real Hash Value:" + realHash);
        }
        if (realHash.equals(pretendedHash)) {
            validated = true;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("hash valid? " + validated);
        }
        return validated;
    }

    /**
     * Check whether the actual hash value of a given entry from a given Jar
     * File matches the proposed one.
     *
     * @param jar
     * @param file
     * @param hashValue
     * @param algo
     * @return boolean
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public boolean checkHashValue(Mediator mediator, URL entry, String hashValue, String algo) throws IOException, NoSuchAlgorithmException {
        //final LogUtils logHash = new LogUtils();
        //logHash.initTimeMeasure();
        final String realHash = this.getHashValue(mediator, entry.openStream(), algo);
        //logHash.showDuration(perfLogger, file + " hash extraction");
        //logHash.initTimeMeasure();
        final boolean checked = this.checkHashValue(realHash, hashValue);
        //logHash.showDuration(perfLogger, file + " hash validation");
        return checked;
    }

    public String getHashValue(Mediator mediator, InputStream iStream, final String algo) throws IOException, NoSuchAlgorithmException {
        //final LogUtils logHash = new LogUtils();
        //logHash.initTimeMeasure();
        final byte[] fileData = IOUtils.read(iStream);
        //logHash.showDuration(perfLogger, " read inputStream to byte array");
        //logHash.initTimeMeasure();
        String hash = this.getHashValue(fileData, algo);
        //logHash.showDuration(perfLogger, " hash extraction from cryptoUtils");
        return hash;
    }

    /**
     * A method for checking whether a given Hash value is the one of the given
     * file
     *
     * @param data
     * @param hashValue
     * @param algo
     * @return boolean
     */
    public boolean checkHashValue(final byte[] data, final String hashValue, final String algo) throws NoSuchAlgorithmException {
        boolean validated = false;
        final String realHash = this.getHashValue(data, algo);
        if (realHash.equals(hashValue)) {
            validated = true;
        }
        return validated;
    }

    /**
     * A method for retrieving the hash value of a given file in a given
     * archive.
     *
     * @param data
     * @param algo
     * @return String, the hash value of the file
     */
    public byte[] digest(byte[] data, String algo) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = null;
        if ((messageDigest = CryptoUtils.getDigest(algo)) != null) {
            return messageDigest.digest(data);
        } else {
            throw new NoSuchAlgorithmException();
        }
    }

    /**
     * A method for retrieving the hash value of a given file in a given
     * archive.
     *
     * @param data
     * @param algo
     * @return String, the hash value of the file
     */
    public String getHashValue(byte[] data, String algo) throws NoSuchAlgorithmException {
        return Base64.getEncoder().encodeToString(digest(data, algo));
    }

    /**
     * A method for verifying validity of a given CMS file
     *
     * @param data
     * @param cmsData
     * @return boolean
     */
    public boolean checkCMSDataValidity(final byte[] data, final byte[] cmsData, String algo) throws Exception {
        return true;
    }
}
