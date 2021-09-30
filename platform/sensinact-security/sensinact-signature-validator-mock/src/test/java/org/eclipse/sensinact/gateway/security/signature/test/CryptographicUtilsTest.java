/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.security.signature.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.assertj.core.api.Assertions;
import org.eclipse.sensinact.gateway.security.signature.internal.CryptographicUtils;
import org.eclipse.sensinact.gateway.util.crypto.Base64;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class CryptographicUtilsTest {
    static CryptographicUtils cutils = null;
    String fileName4hash = "src/test/resources/textFile.txt.bis";
    String trueHashValue = "c00148f586db109ffaca3724102e69e2e7996bf0";
    String falseHashValue = "swzWklBmNJVD4/8+hpCT6b3L7WY=";
    String defaultAlgo = "SHA-1";
    String fileName4CMS = "src/test/resources/textFile.txt";
    String alias = "selfsigned";
    String passwd = "sensiNact_team";
    String keyStoreType = "jks";
    String defaultKeystoreFile = "../cert/keystore.jks";
    String signatureFileName = "src/test/resources/JUNITTES.SF";
    String signatureBlockName = "src/test/resources/JUNITTES.DSA";
    KeyStore ks = null;

    public CryptographicUtilsTest() throws NoSuchAlgorithmException {
        cutils = new CryptographicUtils(null);
    }

    KeyStore getKeyStore() throws KeyStoreException, FileNotFoundException, NoSuchAlgorithmException, IOException, CertificateException {
        KeyStore ks = KeyStore.getInstance(keyStoreType);
        ks.load(new FileInputStream(defaultKeystoreFile), passwd.toCharArray());
        return ks;
    }

    /**
     * @param fileName
     * @return byte[]
     * @throws IOException
     */
    byte[] getData(String fileName) throws IOException {
      try (  FileInputStream fis = new FileInputStream(fileName);){
        byte[] data = new byte[fis.available()];
        fis.read(data);
        return data;
      }
    }

    /**
     * @param data
     * @param algo
     * @throws NoSuchAlgorithmException
     */
    String getTrueHashValue(byte[] data, String algo) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algo);
        digest.update(data);
        return Base64.encodeBytes(digest.digest());
    }

    /**
     * @param d1
     * @param d2
     * @return boolean
     */
    protected boolean dataWithSameContent(byte[] d1, byte[] d2) {
        boolean result = false;
        if (d1.length == d2.length) {
            byte[] data1 = new byte[d1.length];
            byte[] data2 = new byte[d2.length];
            boolean sameContent = true;
            for (int i = 0; i < d1.length; i++) {
                sameContent = sameContent && (data1[i] == data2[i]);
            }
            result = sameContent;
        }
        return result;
    }

    /**
     * @throws NoSuchAlgorithmException
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test
    public void testCheckHashValueOK() throws Exception {
        byte[] data = getData(fileName4hash);
        String trueHashValue = this.getTrueHashValue(data, defaultAlgo);
        boolean result = cutils.checkHashValue(data, trueHashValue, "SHA1-Digest");
        Assertions.assertThat(result).isTrue();
    }

    @Test
    public void testGetHashValueOK() throws Exception {
        byte[] data = getData(fileName4hash);
        String trueHashValue = getTrueHashValue(data, defaultAlgo);
        String result = cutils.getHashValue(data, "SHA1-Digest");

        Assertions.assertThat(result).isEqualTo(trueHashValue);
    }

    /**
     * A method for verifying validity of a given CMS file
     */
    @Disabled
    @Test()
    public void testCheckCMSDataValidity() throws Exception {
        byte[] signatureFileData = this.getData(signatureFileName);
        byte[] signatureBlockData = this.getData(signatureBlockName);
        boolean res = cutils.checkCMSDataValidity(signatureFileData, signatureBlockData, "SHA1-Digest");
        Assertions.assertThat(res).isTrue();

    }
}
