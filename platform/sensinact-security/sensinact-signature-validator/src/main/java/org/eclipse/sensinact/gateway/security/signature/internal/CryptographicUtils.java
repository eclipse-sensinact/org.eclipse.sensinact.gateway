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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509CertSelector;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.ess.ESSCertID;
import org.bouncycastle.asn1.ess.ESSCertIDv2;
import org.bouncycastle.asn1.ess.SigningCertificate;
import org.bouncycastle.asn1.ess.SigningCertificateV2;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.cert.jcajce.JcaCertStoreBuilder;
import org.bouncycastle.cert.selector.X509CertificateHolderSelector;
import org.bouncycastle.cert.selector.jcajce.JcaX509CertSelectorConverter;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.util.CryptoUtils;
import org.eclipse.sensinact.gateway.util.IOUtils;
import org.eclipse.sensinact.gateway.util.crypto.Base64;

/**
 * Implementation class of the CryptographicUtils service, using Bouncy Castle
 * Cryptography provider.
 */
public class CryptographicUtils
{
	private Mediator mediator;
    
	/**
	 * Constructor
	 *
	 */
	public CryptographicUtils(Mediator mediator) throws NoSuchAlgorithmException
	{
		this.mediator = mediator;		
		Security.addProvider(new BouncyCastleProvider());
	}

	private boolean checkHashValue(final String realHash,
	        final String pretendedHash)
	{
		if (this.mediator.isDebugLoggable())
		{
			this.mediator.debug("pretended hash:" + pretendedHash);
		}
		boolean validated = false;

		if (this.mediator.isDebugLoggable())
		{
			this.mediator.debug("real Hash Value:" + realHash);
		}
		if (realHash.equals(pretendedHash))
		{
			validated = true;
		}
		if (this.mediator.isDebugLoggable())
		{
			this.mediator.debug("hash valid? " + validated);
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
	public boolean checkHashValue(Mediator mediator,
			URL entry, String hashValue, String algo)
	        throws IOException, NoSuchAlgorithmException
	{

		//final LogUtils logHash = new LogUtils();
		//logHash.initTimeMeasure();
		final String realHash = this.getHashValue(mediator, entry.openStream(), algo);
		//logHash.showDuration(perfLogger, file + " hash extraction");
		//logHash.initTimeMeasure();
		final boolean checked = this.checkHashValue(realHash, hashValue);
		//logHash.showDuration(perfLogger, file + " hash validation");
		return checked;
	}

	public String getHashValue(Mediator mediator,
			InputStream iStream, final String algo)
	        throws IOException, NoSuchAlgorithmException
	{
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
	public boolean checkHashValue(final byte[] data, final String hashValue,
	        final String algo) throws NoSuchAlgorithmException
	{
		boolean validated = false;
		final String realHash = this.getHashValue(data, algo);
		if (realHash.equals(hashValue))
		{
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
	public byte[] digest(byte[] data, String algo)
	        throws NoSuchAlgorithmException
	{
		MessageDigest messageDigest = null;
		if((messageDigest = CryptoUtils.getDigest(algo))!=null)
		{
			return messageDigest.digest(data);
		}		
		else
		{
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
	public String getHashValue(byte[] data, String algo)
	        throws NoSuchAlgorithmException
	{
		return  Base64.encodeBytes(digest(data,algo));
	}

	/**
	 * A method for verifying validity of a given CMS file
	 * 
	 * @param data
	 * @param cmsData
	 * @return boolean
	 */
	public boolean checkCMSDataValidity(final byte[] data, 
			final byte[] cmsData, String algo)
	        throws Exception
	{
		boolean verified = false;
//
//		if (data == null)
//		{
//			System.out.println("no data");
//		}
//		if (cmsData == null)
//		{
//			System.out.println("no cmsData");
//		}
		// set data in CMS file
		final CMSSignedData cmsWithSignedData = new CMSSignedData(
		        new CMSProcessableByteArray(data), cmsData);
		
//		if (cmsWithSignedData.getSignedContent() == null)
//		{
//			System.out.println("no signed content");
//		}
		// verify CMS containing signed data
		verified = this.checkCMSDataValidity(cmsWithSignedData, algo);
		return verified;
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	protected boolean checkCMSDataValidity(final CMSSignedData cmsData,
	        final String securityProvider, String algo)
	        throws Exception
	{
		boolean verified = false;
//		if (cmsData.getSignedContent() == null)
//		{
//			System.out.println("no signed content");
//		}

		// verify CMS containing signed data
		final Collection<SignerInformation> signers = 
				cmsData.getSignerInfos().getSigners();
		
//		if (signers.size() != 1)
//		{
//			System.out.println("Several Signers available");
//		}
		/*CertStore certStore = cmsData.getCertificatesAndCRLs(
			"Collection", securityProvider);*/

        JcaCertStoreBuilder builder = new JcaCertStoreBuilder();
        builder.addCertificates(cmsData.getCertificates());
        builder.addCertificates(cmsData.getCRLs());
        builder.setProvider(securityProvider);

		Iterator<SignerInformation> iter = signers.iterator();
		SignerInformation signerInfo = null;

		while (iter.hasNext())
		{
			signerInfo = iter.next();
			//verified = verified || verify(signerInfo,certStore, algo);
            verified = verified || verify(signerInfo,builder.build(), algo);
		}
		return verified;
	}
	
	@SuppressWarnings("unchecked")
	public Certificate getCertificate(SignerInformation signer,
		CertStore certStore) throws Exception
	{
		X509CertificateHolderSelector x509CertificateHolderSelector =
				new X509CertificateHolderSelector(signer.getSID(
						).getSubjectKeyIdentifier());
		
		X509CertSelector certSelector =
				new JcaX509CertSelectorConverter().getCertSelector(
						x509CertificateHolderSelector);
		Collection<Certificate> certCollection =
		(Collection<Certificate>) certStore.getCertificates(certSelector);

		Iterator<Certificate> certIt = certCollection.iterator();
		Certificate x509Cert = (Certificate) certIt.next();
		return x509Cert;
	}

	public boolean verify(final SignerInformation signer,
		CertStore certStore, String algo) throws Exception
	{
		Certificate x509Cert = getCertificate(signer, certStore);

		JcaSimpleSignerInfoVerifierBuilder sigVerifBuilder =
				new JcaSimpleSignerInfoVerifierBuilder();
		
		SignerInformationVerifier signerInfoVerif = sigVerifBuilder.setProvider(
			BouncyCastleProvider.PROVIDER_NAME).build(x509Cert.getPublicKey());
		
		//the digest verification is included 
		// Verif on public key so that cert verifications are not performed
		//(done in dssl layer)
		boolean rawVerif = signer.verify(signerInfoVerif);
		
		// If RFC 3852 non-conformity -> CMSException
//		System.out.println(String.format("Raw signature verification results in '%1$s'", 
//				rawVerif));
		
		boolean signerCertRefVerif = signingCertificateAttributeVerif(
				signer, x509Cert, algo); 
		
		// TODO : also done in dssl layer. Should this verification be removed from here ?
//		System.out.println(String.format("Signer-cert-ref verification results in '%1$s'", 
//				signerCertRefVerif));
		
		return rawVerif && signerCertRefVerif;
	}

	private boolean signingCertificateAttributeVerif(
			SignerInformation signer, Certificate x509Cert, String algo)
			throws CertificateException, NoSuchAlgorithmException
	{ 
		  boolean signerCertRefVerif = true; 
		  
		  ESSCertID signingCertRef = getSigningCertificateAttribute(
				signer.getSignedAttributes()); 
		  
		  if(signingCertRef!=null)
		  { 
		   //System.out.println(String.format("signer-cert-ref attribute found")); 
		   byte[] certHash = digest(x509Cert.getEncoded(), algo); 
		   signerCertRefVerif = Arrays.equals(certHash, signingCertRef.getCertHash()); 
		   
		  } else 
		  { 
		   ESSCertIDv2 signingCertRefV2 = getSigningCertificateV2Attribute(
				signer.getSignedAttributes()); 
		   
		   if(signingCertRefV2!=null)
		   { 
			//System.out.println(String.format("signer-cert-ref-v2 attribute found")); 
			String hashAlgorithm = signingCertRefV2.getHashAlgorithm().getAlgorithm().getId(); 
		    byte[] certHash = digest(x509Cert.getEncoded(), hashAlgorithm); 
		    signerCertRefVerif = Arrays.equals(certHash, signingCertRefV2.getCertHash()); 
		   } 
		  } 
		  return signerCertRefVerif; 
		 } 
	

	 private static ESSCertID getSigningCertificateAttribute(AttributeTable atab)
	 { 
	  ESSCertID result = null; 
	  if (atab != null) 
	  { 
	   Attribute attr = atab.get(PKCSObjectIdentifiers.id_aa_signingCertificate); 
	   if (attr != null)
	   { 
	    ESSCertID[] signingCerts = SigningCertificate.getInstance(
	    	attr.getAttrValues().getObjectAt(0)).getCerts(); 
	    
	    if(signingCerts!=null && signingCerts.length>0) { 
	     result = signingCerts[0]; 
	    } 
	   } 
	  } 
	  return result; 
	 } 

	 private static ESSCertIDv2 getSigningCertificateV2Attribute(AttributeTable atab)
	 { 
	  ESSCertIDv2 result = null; 
	  if (atab != null)
	  { 
	   Attribute attr = atab.get(PKCSObjectIdentifiers.id_aa_signingCertificateV2); 
	   if (attr != null)
	   { 
	    ESSCertIDv2[] signingCerts = SigningCertificateV2.getInstance(
	    		attr.getAttrValues().getObjectAt(0)).getCerts(); 
	    
	    if(signingCerts!=null && signingCerts.length>0)
	    { 
	     result = signingCerts[0]; 
	    } 
	   } 
	  } 
	  return result; 
	 } 
	 
	public boolean checkCMSDataValidity(final CMSSignedData cmsData, String algo)
	        throws Exception
	{
		return this.checkCMSDataValidity(cmsData, 
				BouncyCastleProvider.PROVIDER_NAME, algo);
	}
}
