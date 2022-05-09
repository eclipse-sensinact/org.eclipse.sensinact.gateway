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
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.HashMap;
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
@SignatureValidator(type = "mock")
@Component(immediate = true)
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
    private final CryptographicUtils cryptoUtils;
    private final KeyStoreManager ksm;
    private Mediator mediator;

    @Activate
    public BundleValidationImpl(BundleContext ctx) throws KeyStoreManagerException, NoSuchAlgorithmException {
        this.mediator = new Mediator(ctx);
        this.validated = new HashMap<String, ValidBundleKey>();

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

    /**
     * @inheritDoc
     * @see BundleValidation#
     * check(org.osgi.framework.Bundle)
     */
    public String check(Bundle bundle) throws BundleValidationException {
        if (bundle == null) {
            LOG.debug("null bundle");
            return null;
        }
        LOG.debug("check bundle: {}", bundle.getLocation());

        int hashcode = bundle.hashCode();
        String bundleName = bundle.getSymbolicName();

        ValidBundleKey validBundleKey = this.validated.get(bundleName);

        if (validBundleKey != null && validBundleKey.hashcode == hashcode) {
            return validBundleKey.key;
        }
        boolean isSigned = false;

        final Enumeration<URL> entries = bundle.findEntries("/META-INF", "*", true);

        while (entries.hasMoreElements()) {
            URL url = entries.nextElement();
            if (url.toExternalForm().endsWith(".RSA") || url.toExternalForm().endsWith("DSA")) {
                isSigned = true;
                break;
            }
        }
        if(!isSigned) {
        	return null;
        }
        SignedBundle sjar;
		try {
			sjar = new SignedBundle(mediator, bundle, cryptoUtils);
	        sjar.setKeyStoreManager(ksm);
	        SignatureFile signatureFile = sjar.getSignatureFile();
	        String sha1 = signatureFile.getManifestHash();
	        LOG.debug("{} {} is valid? {}", FILE, bundle.getLocation(), sha1 != null);
	        this.validated.put(bundleName, new ValidBundleKey(hashcode, bundleName, sha1));
	        return sha1;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bundleName;
    }
}
