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
package org.eclipse.sensinact.gateway.security.signature.internal;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.security.signature.exception.BundleValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipException;

public class SignedBundleChecker {
	
	private static final Logger LOG = LoggerFactory.getLogger(SignedBundleChecker.class);
    private static final String METADATA_DIR = "/META-INF/";
    private static final String MF_FILE = "MANIFEST.MF";
    Mediator mediator;

    public SignedBundleChecker(Mediator mediator) {
        this.mediator = mediator;
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
    protected boolean checkCoherence(SignedBundle signedJar, String signer, Certificate cert, CryptographicUtils cryptoUtils, String algo) throws Exception {
        boolean coherent = false;
        boolean resourcesOrderValid = false;
        boolean manifestValid = false;
        boolean signatureFileValid = false;
        boolean signatureBlockValid = false;
        resourcesOrderValid = this.checkResourcesOrderValid(signedJar);
        if (LOG.isInfoEnabled()) {
            LOG.info("resourcesOrderValid " + resourcesOrderValid);
        }
        signatureBlockValid = this.checkSignatureBlockValidity(signedJar, signer, cryptoUtils, algo);
        if (LOG.isInfoEnabled()) {
            LOG.info("signatureBlockValid " + signatureBlockValid);
        }
        signatureFileValid = this.checkSignatureFileValidity(signedJar, signer, cryptoUtils);
        if (LOG.isInfoEnabled()) {
            LOG.info("signatureFileValid " + signatureFileValid);
        }
        manifestValid = this.checkManifestValidity(signedJar, cryptoUtils);
        if (LOG.isWarnEnabled()) {
            LOG.info("manifestValid " + manifestValid);
        }
        coherent = resourcesOrderValid && signatureBlockValid && signatureFileValid && manifestValid;
        if (LOG.isWarnEnabled()) {
            LOG.info("coherent " + coherent);
        }
        return coherent;
    }

    protected boolean checkSignatureBlockValidity(final SignatureFile sigFile, final SignatureBlock block, final X509Certificate cert, final CryptographicUtils cryptoUtils, String algo) throws Exception {
        boolean blockValidity = false;
        if (cert == null) {
            if (LOG.isWarnEnabled()) {
                LOG.info("SignedJarFile.checkSignatureBlockValidity, no cert found");
            }
        } else {
            if (LOG.isWarnEnabled()) {
                LOG.info("SignedJarFile.checkSignatureBlockValidity, cert found");
            }
            // check time validity of PK Certificate
            // raises a CertificateException if not valid
            cert.checkValidity();
            // check coherence between the signatureFile and the signature Block
            final byte[] signatureFileData = sigFile.getBytes();
            if (LOG.isDebugEnabled()) {
                LOG.debug("signature file: " + new String(signatureFileData) + ".");
            }
            final byte[] signatureBlock = block.getEncoded();
            // logger.log(Level.ALL, new String(signatureBlock));
            // verify signature
            blockValidity = cryptoUtils.checkCMSDataValidity(signatureFileData, signatureBlock, algo);
        }
        if (LOG.isWarnEnabled()) {
            LOG.info("block validity: " + blockValidity);
        }
        return blockValidity;
    }

    private boolean checkSignatureBlockValidity(final SignedBundle signedJar, final String signer, final CryptographicUtils cryptoUtils, String algo) throws Exception {
        final X509Certificate cert = (X509Certificate) signedJar.getCertificate(signer);
        final SignatureFile sigFile = signedJar.getSignatureFile(signer);
        final SignatureBlock block = signedJar.getSignatureBlock(signer);
        return this.checkSignatureBlockValidity(sigFile, block, cert, cryptoUtils, algo);
    }

    protected boolean checkSignatureFileValidity(final SignedBundle signedJar, final SignatureFile sigFile, final CryptographicUtils cryptoUtils) throws NoSuchAlgorithmException, IOException {
        boolean manifestEntriesValid = false, manifestHashValid = false;
        if (sigFile != null) {
            sigFile.show();
            final String hashValue = sigFile.getManifestHash();
            final String hashAlgo = sigFile.getHashAlgo();
            manifestHashValid = cryptoUtils.checkHashValue(mediator, signedJar.getEntry(METADATA_DIR + MF_FILE), hashValue, hashAlgo);
            LOG.debug("manifest hash valid: %s", manifestHashValid);
            // check the validity of the hashes for the manifest entries
            manifestEntriesValid = SignatureFileChecker.checkEntriesValidity(mediator, signedJar, sigFile, cryptoUtils);
            if (LOG.isWarnEnabled()) {
                LOG.debug("entries hash valid: " + manifestEntriesValid);
            }
        }
        return manifestHashValid && manifestEntriesValid;
    }

    /**
     * A method for checking whether the Signature File is valid (that is to say
     * whether given Manifest digest matches real Manifest of the the archive).
     *
     * @param cryptoUtils TODO
     * @param signer      TODO
     * @param signedJar,  the archive to be checked
     * @return boolean, true if the signature File matches Manifest of archive.
     */
    private boolean checkSignatureFileValidity(final SignedBundle signedJar, final String signer, final CryptographicUtils cryptoUtils) throws IOException, NoSuchAlgorithmException {
        boolean validated = false;
        // get a HashMap with Signature Files (with signers as key)
        final SignatureFile sigFile = signedJar.getSignatureFile(signer);
        if (sigFile != null) {
            if (LOG.isWarnEnabled()) {
                LOG.info("Signature File found");
            }
            validated = this.checkSignatureFileValidity(signedJar, sigFile, cryptoUtils);
        }
        return validated;
    }

    /**
     * A method for checking whether manifest is valid against the archive (that
     * is to say whether given file digests match real file digests).
     *
     * @param signedJar, the archive to be checked
     * @return boolean, true if the digests from Manifest File match the real
     * digest of files in archive.
     */
    private boolean checkManifestValidity(SignedBundle signedJar, final CryptographicUtils cryptoUtils) throws Exception {
        Manifest manifest = new Manifest();
        manifest.read(signedJar.getEntry("/META-INF/MANIFEST.MF").openStream());
        boolean manifestEntriesExist = this.checkManifestEntriesExist(signedJar, manifest);
        if (LOG.isInfoEnabled()) {
            LOG.info("manifestEntriesExist: " + manifestEntriesExist);
        }
        boolean resourcesKnownInManifest = this.checkResourcesKnownInManifest(signedJar, manifest);

        if (LOG.isInfoEnabled()) {
            LOG.info("resourcesKnownInManifest: " + resourcesKnownInManifest);
        }
        boolean hashValuesValid = false;
        if (manifestEntriesExist && resourcesKnownInManifest) {
            hashValuesValid = this.checkHashValuesValid(signedJar, manifest, cryptoUtils);
        }
        if (LOG.isWarnEnabled()) {
            LOG.info("hashValuesValid: " + hashValuesValid);
        }
        final boolean validated = manifestEntriesExist && resourcesKnownInManifest && hashValuesValid;
        if (LOG.isWarnEnabled()) {
            LOG.info("validated: " + validated);
        }
        return validated;
    }

    /**
     * A method for checking whether, for each entry in the manifest, check
     * whether matching file exists. In the case of embedded archives, two valid
     * signatures exist:<br/>
     * - either the embedded archive is signed a a standard resource<br/>
     * - or the resources in the embedded archive are signed as if there were
     * directly stored in the main archive
     *
     * @param signedJar, the jar to be analysed
     * @param manifest,  the manifest of the jar
     * @return boolean, true if all manifest entries exist as resource in the
     * archive.
     */
    private boolean checkManifestEntriesExist(SignedBundle signedJar, final Manifest manifest) throws IOException {
        boolean checked = true;
        final Iterator<String> elements = manifest.getEntries().keySet().iterator();


        while (elements.hasNext() && checked) {
            String element = elements.next();

            if (signedJar.getEntry("/" + element) == null) {
                checked = false;
            }
        }
        return checked;
    }

    /**
     * check whether hash value of each file in the archive matches with
     * pretended hash value in the manifest.<br>
     * This method should only be called when checkManifestEntriesExist and
     * checkJarFilesKnown return true.
     *
     * @param signedJar, the jar to be analysed
     * @param manifest,  the manifest of the jar
     * @return boolean, true if all entries in the manifest have valid hash
     * values when compared to resources in the archive
     */
    protected boolean checkHashValuesValid(SignedBundle signedJar, final Manifest manifest, final CryptographicUtils cryptoUtils) throws BundleValidationException, ZipException, IOException, NoSuchAlgorithmException {
        // TODO JarValidation: the hash values of the resources in embedded
        // archives must be checked if they are available
        boolean checked = true;
        final Map<String, Attributes> entries = manifest.getEntries();
        final Iterator<Entry<String, Attributes>> iter = entries.entrySet().iterator();
        Map.Entry<String, Attributes> entry;
        Attributes data;
        Iterator<Entry<Object, Object>> iter2;
        Map.Entry<Object, Object> entry2;
        Attributes.Name key2;
        String hashValue;
        String file, type;
        //final LogUtils logAll = new LogUtils();
        //logAll.initTimeMeasure();
        while (iter.hasNext()) {
            //final LogUtils logManifestEntry = new LogUtils();
            //logManifestEntry.initTimeMeasure();
            entry = iter.next();
            file = entry.getKey();
            if (LOG.isDebugEnabled()) {
                LOG.debug("file: " + file);
            }
            data = (Attributes) entries.get(file);
            iter2 = data.entrySet().iterator();
            while (iter2.hasNext()) {
                //final LogUtils logHash = new LogUtils();
                //logHash.initTimeMeasure();
                entry2 = iter2.next();
                key2 = (Attributes.Name) entry2.getKey();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("key2: " + key2);
                }
                hashValue = (String) data.get(key2);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("hashValue: " + hashValue);
                }
                type = key2.toString();
                checked = checked && cryptoUtils.checkHashValue(mediator, signedJar.getEntry(file), hashValue, type);
                //logHash.showDuration(perfLogger, file + " hash");
            }
            //logManifestEntry.showDuration(perfLogger, file);
        }
        //logAll.showDuration(perfLogger, "checkHashValuesValid core");
        return checked;
    }

    /**
     * A method for checking whether, for each file in the archive, matching
     * manifest entry exists
     *
     * @param signedJar, the jar to be analysed
     * @param manifest,  the manifest of the jar
     * @return boolean, true if every resource in the archive has an entry in
     * the manifest;
     */
    private boolean checkResourcesKnownInManifest(SignedBundle signedJar, final Manifest manifest) {
        boolean checked = true;
        List<URL> entries = signedJar.getEntries();
        Iterator<URL> iterator = entries.iterator();
        URL entry;
        while (iterator.hasNext() && checked) {
            entry = iterator.next();
            String path = entry.getPath().substring(1);

            if (path.endsWith(".class") && manifest.getAttributes(path) == null) {
                checked = false;
                if (LOG.isWarnEnabled()) {
                    LOG.warn(path + " not referenced in the manifest file");
                }
            }
        }
        return checked;
    }

    /*
     *
     */
    private boolean[] checkEntry(final URL entry, final boolean sfFound, final boolean orFound, final boolean oValid) {
        boolean signatureFileFound = sfFound;
        boolean otherResourcesFound = orFound;
        boolean orderValid = oValid;
        String name = entry.getPath();
        if (!name.endsWith("/")) {
            if (name.endsWith(SignedBundle.METADATA_DIR + SignedBundle.MF_FILE)) {
                if (signatureFileFound || otherResourcesFound) {
                    orderValid = false;
                }
            } else if (name.endsWith(".SF") || name.endsWith(".RSA") || name.endsWith(".DSA")) {
                signatureFileFound = true;
                if (otherResourcesFound) {
                    orderValid = false;
                }
            } else {
                otherResourcesFound = true;
            }
        }
        final boolean[] checkState = new boolean[3];
        checkState[0] = signatureFileFound;
        checkState[1] = otherResourcesFound;
        checkState[2] = orderValid;
        return checkState;
    }

    /**
     * A method for checking whether the resources in a signed archive stream
     * are in a valid order.<br>
     *
     * @param signedJar, the archive to be checked
     * @return boolean, true if the order of resources is valid
     */
    private boolean checkResourcesOrderValid(final SignedBundle signedJar) throws FileNotFoundException, IOException {
        boolean orderValid = false;
        final Manifest manifest = signedJar.getManifest();
        if (manifest != null) {
            orderValid = true;
        }
        boolean signatureFileFound = false;
        boolean otherResourcesFound = false;
        boolean[] checkState;

        List<URL> entries = signedJar.getEntries();

        URL entry;

        for (Iterator<URL> iterator = entries.iterator(); iterator.hasNext(); ) {
            entry = iterator.next();
            checkState = this.checkEntry(entry, signatureFileFound, otherResourcesFound, orderValid);
            signatureFileFound = checkState[0];
            otherResourcesFound = checkState[1];
            orderValid = checkState[2];
        }

        return orderValid;
    }
}
