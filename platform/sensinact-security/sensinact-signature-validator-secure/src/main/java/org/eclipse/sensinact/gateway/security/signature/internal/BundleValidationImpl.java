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

import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.security.signature.api.BundleValidation;
import org.eclipse.sensinact.gateway.security.signature.api.SignatureValidator;
import org.eclipse.sensinact.gateway.security.signature.exception.BundleValidationException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the BundleValidation service
 */
@Component
@SignatureValidator(type = "secure")
public class BundleValidationImpl implements BundleValidation {
	
	private static final Logger LOG = LoggerFactory.getLogger(BundleValidationImpl.class);
    // ********************************************************************//
    // 						NESTED DECLARATIONS 						   //
    // ********************************************************************//

    private final class ValidBundleKey {
        public final int hashcode;
        public final String name;
        public final String key;

        public ValidBundleKey(int hashcode, String name, String key) {
            this.hashcode = hashcode;
            this.name = name;
            this.key = key;
        }
    }

    private final class UnknownBundleKey {
        public final int hashcode;
        public final String name;

        public UnknownBundleKey(int hashcode, String name) {
            this.hashcode = hashcode;
            this.name = name;
        }
    }

    // ********************************************************************//
    // 						ABSTRACT DECLARATIONS 						   //
    // ********************************************************************//
    // ********************************************************************//
    // 						STATIC DECLARATIONS 							//
    // ********************************************************************//
    private static final String FILE = "file";

    // ********************************************************************//
    // 						INSTANCE DECLARATIONS 							//
    // ********************************************************************//
    private final Map<String, ValidBundleKey> validated;
    private final Map<String, UnknownBundleKey> unknown;
    private final CryptographicUtils cryptoUtils;
    private final KeyStoreManager ksm;
    private Mediator mediator;

    @Activate
    public BundleValidationImpl(BundleContext ctx) throws KeyStoreManagerException, NoSuchAlgorithmException {
        this.mediator = new Mediator(ctx);
        this.validated = new HashMap<>();
        this.unknown = new HashMap<>();

        this.cryptoUtils = new CryptographicUtils();
        this.ksm = new KeyStoreManager(this.getKeyStoreFileName(), this.getKeyStorePassword());
    }

    protected String getKeyStoreFileName() {
        return (String) this.mediator.getProperty("org.eclipse.sensinact.gateway.security.jks.filename");
    }

    protected String getKeyStorePassword() {
        return (String) this.mediator.getProperty("org.eclipse.sensinact.gateway.security.jks.password");
    }

    protected String getSignerPassword() {
        return (String) this.mediator.getProperty("org.eclipse.sensinact.gateway.security.signer.password");
    }

    @Override
    public String check(Bundle bundle) throws BundleValidationException {
        if (bundle == null) {
            LOG.debug("null bundle");
            return null;
        }
        LOG.debug("check bundle: {}", bundle.getLocation());

        int hashcode = bundle.hashCode();
        String bundleName = bundle.getSymbolicName();
        ValidBundleKey validBundleKey = this.validated.get(bundleName);

        if (validBundleKey != null && validBundleKey.hashcode == hashcode) 
            return validBundleKey.key;
        
        UnknownBundleKey unknownBundleKey = this.unknown.get(bundleName);
        if(unknownBundleKey != null && unknownBundleKey.hashcode == hashcode)
        	return null;
        
        boolean isSigned = false;

        final Enumeration<URL> entries = bundle.findEntries("/META-INF", "*", true);

        while (entries.hasMoreElements()) {
            URL url = entries.nextElement();
            if (url.toExternalForm().endsWith(".RSA") || url.toExternalForm().endsWith("DSA")) {
                isSigned = true;
                break;
            }
        }
        String sha1 = null;
        if (isSigned) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(FILE + " " + bundle.getLocation() + " is signed");
            }
            try {
                SignedBundle sjar = new SignedBundle(mediator, bundle, cryptoUtils);
                sjar.setKeyStoreManager(ksm);

                Map<String, Certificate> validCertificates = sjar.getValidCertificates(this.getSignerPassword());
                Iterator<Map.Entry<String, Certificate>> signers = validCertificates.entrySet().iterator();

                final List<Certificate> certs4validSig = new ArrayList<Certificate>();
                Certificate currentCert = null;
                String signer = "";
                Map.Entry<String,Certificate> entry = null;

                while (signers.hasNext()) {
                    entry = (Map.Entry<String,Certificate>) signers.next();
                    signer = entry.getKey();

                    LOG.debug("signers: {}", signers);
                    currentCert = validCertificates.get(signer);
                    SignatureFile signatureFile = sjar.getSignatureFile(signer);

                    if (signatureFile == null) {
                        continue;
                    }
                    if (sjar.checkCoherence(signer, currentCert, signatureFile.getHashAlgo())) {
                        certs4validSig.add(currentCert);
                        if (LOG.isInfoEnabled()) {
                            LOG.debug("certificate for " + signer + " valid");
                        }
                    }
                    if (certs4validSig.size() == 0) {
                        this.unknown.put(bundleName, new UnknownBundleKey(hashcode, bundleName));
                        sha1 = null;
                    } else {
                        sha1 = signatureFile.getManifestHash();
                    }
                }
            } catch (Exception e) {
                this.unknown.put(bundleName, new UnknownBundleKey(hashcode, bundleName));
                throw new BundleValidationException(e);
            }
        }
        if(sha1!=null)
        	this.validated.put(bundleName, new ValidBundleKey(hashcode, bundleName, sha1));
        else 
            this.unknown.put(bundleName, new UnknownBundleKey(hashcode, bundleName));
        return sha1;
    }
}
