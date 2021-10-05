/*
* Copyright (c) 2021 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core.security.user.openid;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.sensinact.gateway.core.security.user.openid.KeyCollection.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JWT handling RS256 and HS256 algorithms
 */
public class JsonWebToken {

	private static final String ALGORITHM_KEY = "alg";
	
	private static final Logger LOG = LoggerFactory.getLogger(JsonWebToken.class);
	
	private final String token;
	private final Map<String,Object> payload;
	
	private final boolean isValid;

	/**
	 * A simple constructor for use with decoded token data
	 * 
	 * @param plainToken the plain JSON data of the JsonWebToken to be instantiated
	 */
	public JsonWebToken(String plainToken) {
		this.token = null;
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> payload;
		boolean valid;
		try {
			payload = mapper.readValue(plainToken,new TypeReference<Map<String, Object>>() {});
			valid = true;
		} catch(IOException e) {
			LOG.error("Unable to read the JWT payload", e);
			payload = Collections.<String,Object>emptyMap();
			valid = false;
		}
		this.payload = payload;
		this.isValid = valid;
	}

	/**
	 * A constructor which validates an encoded, signed JWT.
	 * 
	 * @param data the encoded data of the JsonWebToken to be instantiated
	 * @param key the String key allowing to validate the data of the JsonWebToken to 
	 * be instantiated
	 */
	public JsonWebToken(String data, List<Keys> keys) {
		this.token = data;
		ObjectMapper mapper = new ObjectMapper();
		Decoder decoder = Base64.getDecoder();
		Encoder encoder = Base64.getEncoder();
		
		int part1 = data.indexOf(".");
		int part2 = data.lastIndexOf(".");
		
		String header = data.substring(0, part1);
		while(((4 - header.length() % 4) % 4)!=0) 
			header = header.concat("=");
		
		header = header.replace("-", "+");
		header = header.replace("_", "/");	
		header = new String(decoder.decode(header));
		
		Map<String, Object> headerMap;
		try {
			headerMap = mapper.readValue(header,new TypeReference<Map<String, Object>>() {});

		} catch(IOException e) {
			LOG.error("Unable to read the JWT header", e);
			this.payload = Collections.emptyMap();
			this.isValid = false;
			return;
		}
		String payload = data.substring(part1 + 1, part2); 
		while(((4 - payload.length() % 4) % 4)!=0)
			payload = payload.concat("=");
		
		payload = payload.replace("-", "+");
		payload = payload.replace("_", "/");
		payload = new String(decoder.decode(payload));

		boolean result = false;
		try {
			String signature = new String(data.substring(part2 + 1).getBytes("UTF-8"));
			while(((4 - signature.length() % 4) % 4)!=0) 
				signature = signature.concat("=");
			
			signature = signature.replace("-", "+");
			signature = signature.replace("_", "/");
			
			byte[] signatureBytes = decoder.decode(signature);
					
			String b64header = encoder.encodeToString(header.getBytes("UTF-8"));
			b64header = b64header.split("=")[0]; // Remove any trailing '='s
			b64header = b64header.replace('+', '-'); // 62nd char of encoding
			b64header = b64header.replace('/', '_'); // 63rd char of encoding
			
			String b64payload =  encoder.encodeToString(payload.getBytes("UTF-8"));
			b64payload = b64payload.split("=")[0]; // Remove any trailing '='s
			b64payload = b64payload.replace('+', '-'); // 62nd char of encoding
			b64payload = b64payload.replace('/', '_'); // 63rd char of encoding
			String testdata = b64header + "." + b64payload;

			String algorithm = String.valueOf(headerMap.get(ALGORITHM_KEY));
			Keys ki;
			switch(algorithm) {		
				case "RS256":		
					ki = keys.stream()
						.filter(k -> "RSA".equals(k.getType()) && "RS256".equals(k.getAlgorithm()))
						.findFirst()
						.orElse(null);
					
					if(ki == null) {
						throw new GeneralSecurityException("No suitable key to decrypt RS256");
					}
					
					KeyFactory keyFactory = KeyFactory.getInstance("RSA");
					Signature sig = Signature.getInstance("SHA256withRSA");	
					
			        String modulusBase64 =  ki.getRsaModulus();
					while(((4 - modulusBase64.length() % 4) % 4)!=0)
						modulusBase64 = modulusBase64.concat("=");
					
					modulusBase64 = modulusBase64.replace("-", "+");
					modulusBase64 = modulusBase64.replace("_", "/");
					byte[] modulusBase64Bytes = decoder.decode(modulusBase64);
					
			        String exponentBase64 = ki.getRsaExponent();
					while(((4 - exponentBase64.length() % 4) % 4)!=0) 
						exponentBase64 = exponentBase64.concat("=");
					
					exponentBase64 = exponentBase64.replace("-", "+");
					exponentBase64 = exponentBase64.replace("_", "/");
					byte[] exponentBase64Bytes = decoder.decode(exponentBase64);
			         		        
			        BigInteger modulus = new BigInteger(1, modulusBase64Bytes);
			        BigInteger publicExponent = new BigInteger(1, exponentBase64Bytes);

			        PublicKey pubKey = keyFactory.generatePublic(new RSAPublicKeySpec(modulus, publicExponent));					
					sig.initVerify(pubKey);
					sig.update(testdata.getBytes("UTF-8"));
					result = sig.verify(signatureBytes);
					break;
				case "HS256":
					ki = keys.stream()
						.filter(k -> "oct".equals(k.getType()))
						.findFirst()
						.orElse(null);
				
					if(ki == null) {
						throw new GeneralSecurityException("No suitable key to decrypt HS256");
					}
					
					String keyBase64 =  ki.getSymmetricKey();
					while(((4 - keyBase64.length() % 4) % 4)!=0)
						keyBase64 = keyBase64.concat("=");
					
					keyBase64 = keyBase64.replace("-", "+");
					keyBase64 = keyBase64.replace("_", "/");
					byte[] keyBytes = decoder.decode(keyBase64);
					
					Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
					SecretKeySpec secret_key = new SecretKeySpec(keyBytes, "HmacSHA256");
					sha256_HMAC.init(secret_key);
					byte compsign[] = sha256_HMAC.doFinal(testdata.getBytes());
					String newsignature = encoder.encodeToString(compsign);
					newsignature = newsignature.split("=")[0]; // Remove any trailing '='s
					newsignature = newsignature.replace('+', '-'); // 62nd char of encoding
					newsignature = newsignature.replace('/', '_'); // 63rd char of encoding				
					result = (new String(signature).equals(newsignature));
					if (!result)
						LOG.error("Invalid signature " + newsignature + " / " + data.substring(part2 + 1));
			default:
				LOG.error("Unknown algorithm {} while decrypting the token", algorithm);
				break;
			}
		} catch (GeneralSecurityException |  IllegalArgumentException | UnsupportedEncodingException e) {
			LOG.error("Error while decrypting the token", e);
			result = false;
		}
		
		Map<String, Object> parsedPayload;
		try {
			parsedPayload = new ObjectMapper().readValue(payload, new TypeReference<Map<String, Object>>() {});

		} catch(IOException e) {
			
			LOG.error("Unable to parse the JWT payload", e);
			parsedPayload = Collections.<String,Object>emptyMap();
			result = false;
		}
		this.payload = parsedPayload;
		this.isValid = result;
	}
	
	/**
	 * Returns true if this JsonWebToken is valid, meaning that its data has been 
	 * validated by the intermediate of a String key ; returns false otherwise
	 * 
	 * @return true of this JsonWebToken is valid; false otherwise
	 */
	public boolean isValid() {
		return isValid;
	}
	
	/**
	 * Returns the Object of this JsonWebToken's payload, whose key is passed as parameter
	 * 
	 * @return this JsonWebToken's payload object for the specified key
	 */
	public Object claim(String key) {
		return this.payload.get(key);
	}
	
	public String getToken() {
		return this.token;
	}
	
	/**
	 * Returns this JsonWebToken's raw String data
	 * 
	 * @return this JsonWebToken's raw String data
	 */
	public String token() {
		return this.token;
	}

	
	/**
	 * Returns true if this token has expired
	 * @return
	 */
	public boolean expired() {
		return willHaveExpiredAt(Instant.now());
	}
	
	private boolean willHaveExpiredAt(Instant instant) {
		Object exp = this.payload.get("exp");
		if(exp != null) {
			long expiration = Long.parseLong(String.valueOf(exp));
			if(instant.isAfter(Instant.ofEpochSecond(expiration))) {
				return true;
			}
		} else {
			return true;
		}
		
		Object nbf = this.payload.get("nbf");
		if(nbf != null) {
			long notBefore = Long.parseLong(String.valueOf(nbf));
			if(instant.isBefore(Instant.ofEpochSecond(notBefore))) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Returns true if the token will be valid after the given interval
	 * @param time in seconds
	 * @return
	 */
	public boolean willHaveExpiredIn(long time) {
		return willHaveExpiredAt(Instant.now().plusSeconds(time));
	}
	
	public int remainingValidity() {
		Instant now = Instant.now();
		
		long result;
		
		Object exp = this.payload.get("exp");
		if(exp != null) {
			long expiration = Long.parseLong(String.valueOf(exp));
			if(now.isAfter(Instant.ofEpochSecond(expiration))) {
				result = -1;
			} else {
				result = expiration - now.getEpochSecond();
			}
		} else {
			result = Integer.MAX_VALUE;
		}

		if(result >= 0) {
			Object nbf = this.payload.get("nbf");
			if(nbf != null) {
				long notBefore = Long.parseLong(String.valueOf(nbf));
				if(now.isBefore(Instant.ofEpochSecond(notBefore))) {
					result = -1;
				}
			}
		}
		
		if(result > Integer.MAX_VALUE) {
			result = Integer.MAX_VALUE;
		}
		
		return (int) result;
	}
}
