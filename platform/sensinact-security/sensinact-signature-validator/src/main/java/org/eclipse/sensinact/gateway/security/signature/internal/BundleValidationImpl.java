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

import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.security.signature.api.BundleValidation;
import org.eclipse.sensinact.gateway.security.signature.exception.BundleValidationException;

/**
 * An implementation of the BundleValidation service
 */
public class BundleValidationImpl implements BundleValidation 
{

	// ********************************************************************//
	// 						NESTED DECLARATIONS 						   //
	// ********************************************************************//
	
	private final class ValidBundleKey
	{
		public final int hashcode;
		public final String name;
		public final String key;
		
		public ValidBundleKey(int hashcode, String name, String key)
		{
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
	
    public BundleValidationImpl(Mediator mediator)
    		throws KeyStoreManagerException, NoSuchAlgorithmException
    {
    	this.mediator = mediator;
    	this.validated = new HashMap<String,ValidBundleKey>();
    	
		this.cryptoUtils = new CryptographicUtils(mediator);
		this.ksm = new KeyStoreManager(this.getKeyStoreFileName(),
				this.getKeyStorePassword());
	}

    protected String getKeyStoreFileName()
    {
    	return (String) this.mediator.getProperty(
    			"org.eclipse.sensinact.gateway.security.jks.filename");
    }    

    protected String getKeyStorePassword()
    {
    	return (String) this.mediator.getProperty(
    			"org.eclipse.sensinact.gateway.security.jks.password");
    }
    
    protected String getSignerPassword()
    {
    	return (String) this.mediator.getProperty(
    			"org.eclipse.sensinact.gateway.security.signer.password");
    }
	
	/**
	 * @inheritDoc
	 *
	 * @see BundleValidation#
	 * check(org.osgi.framework.Bundle)
	 */
	public String check(Bundle bundle) throws BundleValidationException
	{
		if(bundle == null)
		{
			this.mediator.debug("null bundle");
			return null;
		}
		this.mediator.debug("check bundle: %s",bundle.getLocation());
		
		int hashcode = bundle.hashCode();		
		String bundleName = bundle.getSymbolicName();
		
		ValidBundleKey validBundleKey = this.validated.get(bundleName);
		
		if(validBundleKey != null && validBundleKey.hashcode == hashcode)
		{
			return validBundleKey.key;
		}		
	    boolean isSigned = false;
	    
		final Enumeration<URL> entries = bundle.findEntries("/META-INF", "*", true);		
		
		while(entries.hasMoreElements())
		{
			URL url = entries.nextElement();
			if(url.toExternalForm().endsWith(".RSA")
					||url.toExternalForm().endsWith("DSA"))
			{
				isSigned = true;
				break;
			}
		}	
		String sha1 = null;
		if(isSigned)
		{
			if(this.mediator.isDebugLoggable())
			{
				this.mediator.debug(FILE + " " + bundle.getLocation()+ " is signed");
			}
			try
			{
			    SignedBundle sjar = new SignedBundle(mediator, bundle, cryptoUtils);
			    sjar.setKeyStoreManager(ksm);
			    
			    Map<String,Certificate> validCertificates= 
				sjar.getValidCertificates(this.getSignerPassword());
				
			    Iterator signers = validCertificates.entrySet().iterator();
	
				if(this.mediator.isDebugLoggable())
				{
					this.mediator.debug("signers: "+signers);
				}
			    Map.Entry entry = null;
	
			    final List<Certificate> certs4validSig =  
			    		new ArrayList<Certificate>();
			    Certificate currentCert = null;
			    String signer = "";
			    
			    while(signers.hasNext())
			    {
					entry = (Map.Entry) signers.next();
					signer = (String)entry.getKey();
					
					this.mediator.info("signers: %s",signers);
					currentCert = (Certificate)validCertificates.get(signer);
					SignatureFile signatureFile = sjar.getSignatureFile(signer);
					
					if(signatureFile == null)
					{
						continue;
					}
					if(sjar.checkCoherence(signer, currentCert, signatureFile.getHashAlgo()))
					{
					    certs4validSig.add(currentCert);
						if(this.mediator.isInfoLoggable())
						{
							this.mediator.info("certificate for "+ signer +" valid");
						}
					}					
					if(certs4validSig.size()==0)
					{
						System.out.println("checkCoherence returned false");
					    sha1 = null;
						this.mediator.info("no valid certificate found");

					} else
					{
						sha1 = signatureFile.getManifestHash();
						this.mediator.info("%s certificate(s) found", 
								certs4validSig.size() );
					}
			    }
			} catch(Exception e)
			{
				throw new BundleValidationException(e);
			}			     
		}
		else
		{
			this.mediator.debug("%s %s is not signed", FILE, 
					bundle.getLocation());
		}
		this.mediator.info("%s %s is valid? %s", FILE, 
				bundle.getLocation(), 
				sha1!=null);
		this.validated.put(bundleName, new ValidBundleKey(
				hashcode, bundleName, sha1));
	    return sha1;
	}

}
