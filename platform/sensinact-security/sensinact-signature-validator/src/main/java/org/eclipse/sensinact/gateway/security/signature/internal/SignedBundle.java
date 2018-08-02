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

import org.bouncycastle.cert.jcajce.JcaCertStoreBuilder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.osgi.framework.Bundle;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
            if (this.mediator.isDebugLoggable()) {
                this.mediator.debug("Better performance, data found (signatureFiles)");
            }
        }
        return signatureFiles;
    }

    /**
     * A method for retriving the signature Files of a jar File
     *
     * @return HashMap
     */
    public SignatureFile getSignatureFile(final String givenSigner) throws IOException {
        final String signer = SignedBundle.getSignerShortName(givenSigner);
        if (this.mediator.isDebugLoggable()) {
            this.mediator.debug("SignedJarFile.getSignatureFile, signer: " + signer);
        }
        Map<String, SignatureFile> signatures = this.getSignatureFiles();
        SignatureFile file = signatures.get(signer);

        if (file == null) {
            if (this.mediator.isErrorLoggable()) {
                this.mediator.error("SignedJarFile.getSignatureFile: " + "signature file not available.");
            }
        } else {
            if (this.mediator.isDebugLoggable()) {
                this.mediator.debug("Better performance, data found (signatureFile)");
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
    public Certificate getCertificate(final String signer) throws IOException, CMSException, GeneralSecurityException {
        Certificate cert = null;
        if (certs == null) {
            certs = new HashMap<String, Certificate>();
        }
        cert = certs.get(signer);
        if (cert == null) {
            SignatureBlock block = this.getSignatureBlock(signer);
            SignerInformationStore signers = block.getSignerInfos();
            //CertStore certStore = block.getCertificatesAndCRLs("Collection","SUN");
            JcaCertStoreBuilder builder = new JcaCertStoreBuilder();
            builder.addCertificates(block.getCertificates());
            builder.addCRLs(block.getCRLs());
            builder.setProvider("SUN");
            Iterator iter = signers.getSigners().iterator();
            if (iter.hasNext()) {
                SignerInformation signerInfo = (SignerInformation) iter.next();
                try {
					/*cert = (X509Certificate)this.cryptoUtils.getCertificate(
							signerInfo,	certStore);*/
                    cert = (X509Certificate) this.cryptoUtils.getCertificate(signerInfo, builder.build());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                certs.put(signer, cert);
            }
        } else {
            if (this.mediator.isDebugLoggable()) {
                this.mediator.debug("Better performance, data found (cert)");
            }
        }
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
    public byte[] getSignatureFromBlock(final String signer) throws IOException, CMSException {
        final CMSSignedData pkcs7 = this.getSignatureBlockAsCMS(signer);
        return SignatureBlock.getSignatureFromSignedData(pkcs7);
    }

    public SignatureBlock getSignatureBlock(final String signer) throws IOException, CMSException {
        SignatureBlock block = null;
        if (signatureBlocks == null) {
            signatureBlocks = new HashMap<String, SignatureBlock>();
        } else {
            block = signatureBlocks.get(signer);
        }
        if (block == null) {
            block = SignatureBlock.getInstance(mediator, this, signer);
            signatureBlocks.put(signer, block);
        } else {
            if (this.mediator.isDebugLoggable()) {
                this.mediator.debug("Better performance, data found (block)");
            }
        }
        return block;
    }

    public CMSSignedData getSignatureBlockAsCMS(final String signer) throws IOException, CMSException {
        return new CMSSignedData(this.getSignatureBlock(signer).getEncoded());
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
    public Map<String, Certificate> getCertificates() throws CMSException, IOException, GeneralSecurityException {
        Map<String, Certificate> certs = new HashMap<String, Certificate>();
        Set<String> signers = this.getSignatureFiles().keySet();
        Iterator<String> iter = signers.iterator();

        String signer = "";
        Certificate cert = null;

        while (iter.hasNext()) {
            signer = (String) iter.next();
            cert = this.getCertificate(signer);
            certs.put(signer, cert);
        }
        return certs;
    }

    /**
     * A method for retrieving the list of valid certificates Validity of
     * certificates should be handled by the KeyStoreManager
     *
     * @param String
     * @return List<Certificate>
     */
    private Map<String, Certificate> extractValidCertificates(final String passwd) throws CMSException, IOException, GeneralSecurityException {
        final Map<String, Certificate> validCertificates = new HashMap<String, Certificate>();
        // getCertificates for the different signers
        final Map<String, Certificate> certificates = this.getCertificates();
        // for each certificate
        final Iterator iter = certificates.entrySet().iterator();
        Certificate currentCert = null;
        boolean currentCertificateValid = false;
        String signer = "";
        Map.Entry entry;
        while (iter.hasNext()) {
            entry = (Map.Entry) iter.next();
            signer = (String) entry.getKey();
            currentCert = (Certificate) certificates.get(signer);
            currentCertificateValid = this.isCertValid(currentCert, passwd);
            if (currentCertificateValid) {
                validCertificates.put(signer, ((X509Certificate) currentCert));
            }
        }
        return validCertificates;
    }

    /*
     * Beware not to consider a certificate as valid just because not password
     * is given
     */
    private boolean isCertValid(final Certificate currentCert, final String passwd) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        boolean certificateValid = false;
        if (ksm != null) {
            if (passwd == null) {
                certificateValid = ksm.isTemporallyOK(currentCert);
            } else {
                certificateValid = ksm.isValid(currentCert);
            }
        }
        return certificateValid;
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
    public Map<String, Certificate> getValidCertificates(final String passwd) throws IOException, CMSException, GeneralSecurityException {
        Map<String, Certificate> validCertificates = null;
        if ("".equals(passwd)) {
            validCertificates = new HashMap<String, Certificate>();
        } else {
            validCertificates = this.extractValidCertificates(passwd);
        }
        return validCertificates;
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
        return new SignedBundleChecker(mediator).checkCoherence(this, signer, cert, cryptoUtils, algo);
    }

    public static String getSignerShortName(final String givenSigner) {
        String signer = "";
        if (givenSigner.length() > 8) {
            signer = givenSigner.substring(0, 8);
        } else {
            signer = givenSigner;
        }
        return signer;
    }
}
