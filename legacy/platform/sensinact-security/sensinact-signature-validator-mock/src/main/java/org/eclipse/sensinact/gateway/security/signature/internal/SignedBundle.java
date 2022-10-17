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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Manifest;

/**
 * A SignedJarFile is a particular type of JarFile that contains cryptographic
 * signature(s). Several signatures can exist in a single SignedJarFile. <br>
 * A cryptographic signature of a SignedJarFile is made of several elements
 * :<br>
 * - the hash value for each existing resources in the manifest file,<br>
 * - the SignatureFile, that contains the hash value of the manifest itself,<br>
 * - the SignatureBlock, which contains a digital signature of the Signature
 * File.
 *
 * @author pierre parrend
 * @see JarFile
 */
public class SignedBundle {
	
	private static final Logger LOG = LoggerFactory.getLogger(SignedBundle.class);
    private KeyStoreManager ksm = null;
    private final CryptographicUtils cryptoUtils;
    protected static final String METADATA_DIR = "/META-INF/";
    protected static final String MF_FILE = "MANIFEST.MF";
    // is usefull
    private Map<String, SignatureFile> signatureFiles = null;
    private Map<String, Certificate> certs = null;
    private Map<String, SignatureBlock> signatureBlocks = null;
    private Bundle bundle;
    private Mediator mediator;

    /**
     * Constructor
     *
     * @param f
     * @param cryptoUtils
     * @throws IOException
     */
    public SignedBundle(Mediator mediator, final Bundle bundle, final CryptographicUtils cryptoUtils) throws IOException {
        this.mediator = mediator;
        this.bundle = bundle;
        this.cryptoUtils = cryptoUtils;
    }

    public List<URL> getEntries() {
        List<URL> listEntries = Collections.<URL>list(this.bundle.findEntries("/", "*", true));
        return listEntries;
    }

    /**
     * A method for setting the KeyStoreManager used for analysing or creating
     * this SignedJarFile.
     *
     * @param ksm
     */
    public void setKeyStoreManager(final KeyStoreManager ksm) {
        this.ksm = ksm;
    }

    /**
     * @param string
     * @return
     */
    public URL getEntry(String path) {
        URL entry = this.bundle.getEntry(path);
        return entry;
    }

    /**
     * @return
     */
    public Manifest getManifest() {
        Manifest manifest = new Manifest();
        try {
            manifest.read(this.getEntry(METADATA_DIR + MF_FILE).openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return manifest;
    }

    /**
     * A method for retriving the signature Files of a jar File
     *
     * @return HashMap
     */
    private Map<String, SignatureFile> getSignatureFiles() throws IOException {
        if (signatureFiles == null) {
            signatureFiles = new HashMap<String, SignatureFile>();
            SignatureFile signedFile = null;
            String signerName = "";

            Enumeration<URL> entries = this.bundle.findEntries(METADATA_DIR, "*.SF", true);

            URL jEntry = null;
            while (entries.hasMoreElements()) {
                jEntry = entries.nextElement();
                signedFile = new SignatureFile(this.mediator, jEntry);

                String path = jEntry.getPath();
                int index = path.lastIndexOf('/');
                String signer = path.substring(index + 1, path.lastIndexOf('.'));
                signatureFiles.put(signer, signedFile);
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Better performance, data found (signatureFiles)");
            }
        }
        return signatureFiles;
    }

    /**
     * A method for retriving the signature Files of a jar File
     *
     * @return HashMap
     */
    public SignatureFile getSignatureFile() throws IOException {
        Map<String, SignatureFile> signatures = this.getSignatureFiles();
        SignatureFile file = signatures.values().iterator().next();
        if (file == null) {
            if (LOG.isErrorEnabled()) {
                LOG.error("SignedJarFile.getSignatureFile: " + "signature file not available.");
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Better performance, data found (signatureFile)");
            }
        }
        return file;
    }

    /**
     * A method for retrieving Public Key Certificate of a given Signer of a jar
     * File
     *
     * @param signer
     * @return Certificate.
     */
    public Certificate getCertificate(final String signer) throws IOException, GeneralSecurityException {
        Certificate cert = null;        
        return cert;
    }

    /**
     * A method for retrieving signature from the CMS data of a signed jar.<br>
     * the archive id supposed to be signed only once by the signer
     *
     * @param signer
     * @return byte[]
     * @throws Exception
     */
    public byte[] getSignatureFromBlock(final String signer) throws IOException {
        return new byte[] {};
    }

    public SignatureBlock getSignatureBlock(final String signer) throws IOException {
        SignatureBlock block = null;
        return block;
    }

    /**
     * A method for retrieving the certificates of the different signers of the
     * SignedJarFile.
     *
     * @return List<Certificate>
     * @throws NoSuchAlgorithmException
     * @throws CMSException
     * @throws CertStoreException
     * @throws NoSuchProviderException
     * @throws IOException
     */
    public Map<String, Certificate> getCertificates() throws IOException, GeneralSecurityException {
        Map<String, Certificate> certs = new HashMap<String, Certificate>();
        return certs;
    }

    /**
     * A method for retrieving valid certificates of signers for this
     * SignedJarFile.
     *
     * @param passwd
     * @return List<Certificate>
     * @throws KeyStoreException
     * @throws CertStoreException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws CMSException
     * @throws CertificateException
     */
    public Map<String, Certificate> getValidCertificates(final String passwd) throws IOException, GeneralSecurityException {
        return this.getCertificates();
    }

    /**
     * A method for checking coherence of a Jar File signature. Verification
     * occurs in four steps:<br>
     * - checking whether the resources in a signed archive stream are in a
     * valid order,<br>
     * - checking whether the Signature Block File is valid (that is to say if
     * the signature matches with Signature File and given public key of
     * signer),<br>
     * - checking whether the Signature File is valid (that is to say whether
     * given Manifest digest matches real Manifest of the the archive),<br>
     * - checking whether manifest is valid against the archive (that is to say
     * whether given file digests match real file digests).<br>
     * Current restriction is that multiple signer is not supported.
     *
     * @param cert
     * @return boolean
     */
    public boolean checkCoherence(final String signer, final Certificate cert, String algo) throws Exception {
        return true;
    }

    public static String getSignerShortName(final String givenSigner) {
        String signer = "";
        return signer;
    }
}
