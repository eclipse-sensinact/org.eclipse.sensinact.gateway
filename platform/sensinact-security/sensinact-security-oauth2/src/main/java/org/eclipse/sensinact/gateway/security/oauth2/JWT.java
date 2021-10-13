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
package org.eclipse.sensinact.gateway.security.oauth2;


import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JWT extends JSONObject {
	private JSONObject header;
	private String alg;
	private boolean validity;

	public JWT(String data, String key) throws JSONException, IOException {
		
		int part1 = data.indexOf(".");
		int part2 = data.lastIndexOf(".");
		
		String header = data.substring(0, part1);
		while(((4 - header.length() % 4) % 4)!=0) {
			header = header.concat("=");
		}
		header = header.replace("-", "+");
		header = header.replace("_", "/");	
		header = new String(Base64.getDecoder().decode(header));
		
		this.header = new JSONObject(header);
		alg = this.header.getString("alg");
		String payload = data.substring(part1 + 1, part2); 
		while(((4 - payload.length() % 4) % 4)!=0) {
			payload = payload.concat("=");
		}
		payload = payload.replace("-", "+");
		payload = payload.replace("_", "/");
		payload = new String(Base64.getDecoder().decode(payload.getBytes()));

		boolean result = false;
		try {
			String signature = new String(data.substring(part2 + 1).getBytes("UTF-8"));
			while(((4 - signature.length() % 4) % 4)!=0) {
				signature = signature.concat("=");
			}
			signature = signature.replace("-", "+");
			signature = signature.replace("_", "/");
			byte[] signatureBytes = Base64.getDecoder().decode(signature);
					
			String b64header = Base64.getEncoder().encodeToString(header.getBytes("UTF-8"));
			b64header = b64header.split("=")[0]; // Remove any trailing '='s
			b64header = b64header.replace('+', '-'); // 62nd char of encoding
			b64header = b64header.replace('/', '_'); // 63rd char of encoding
			
			String b64payload = Base64.getEncoder().encodeToString(payload.getBytes("UTF-8"));
			b64payload = b64payload.split("=")[0]; // Remove any trailing '='s
			b64payload = b64payload.replace('+', '-'); // 62nd char of encoding
			b64payload = b64payload.replace('/', '_'); // 63rd char of encoding
			String testdata = b64header + "." + b64payload;

		    byte[] keyBytes = key.getBytes();
			switch(alg) {		
				case "RS256":		
					KeyFactory keyFactory = KeyFactory.getInstance("RSA");
					Signature sig = Signature.getInstance("SHA256withRSA");	
					
					int index = key.indexOf('.');
			        String modulusBase64 =  key.substring(0,index);
					while(((4 - modulusBase64.length() % 4) % 4)!=0) {
						modulusBase64 = modulusBase64.concat("=");
					}
					modulusBase64 = modulusBase64.replace("-", "+");
					modulusBase64 = modulusBase64.replace("_", "/");
					byte[] modulusBase64Bytes = Base64.getDecoder().decode(modulusBase64);
					
			        String exponentBase64 = key.substring(index+1);
					while(((4 - exponentBase64.length() % 4) % 4)!=0) {
						exponentBase64 = exponentBase64.concat("=");
					}
					exponentBase64 = exponentBase64.replace("-", "+");
					exponentBase64 = exponentBase64.replace("_", "/");
					byte[] exponentBase64Bytes = Base64.getDecoder().decode(exponentBase64);
			         		        
			        BigInteger modulus = new BigInteger(1, modulusBase64Bytes);
			        BigInteger publicExponent = new BigInteger(1, exponentBase64Bytes);

			        PublicKey pubKey = keyFactory.generatePublic(new RSAPublicKeySpec(modulus, publicExponent));					
					sig.initVerify(pubKey);
					sig.update(testdata.getBytes("UTF-8"));
					result = sig.verify(signatureBytes);
					break;
				case "HS256":
					Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
					SecretKeySpec secret_key = new SecretKeySpec(keyBytes, "HmacSHA256");
					sha256_HMAC.init(secret_key);
					byte compsign[] = sha256_HMAC.doFinal(testdata.getBytes());
					String newsignature = Base64.getEncoder().encodeToString(compsign);
					newsignature = newsignature.split("=")[0]; // Remove any trailing '='s
					newsignature = newsignature.replace('+', '-'); // 62nd char of encoding
					newsignature = newsignature.replace('/', '_'); // 63rd char of encoding				
					result = (new String(signature).equals(newsignature));
					if (!result)
						System.out.println("Bad signature " + newsignature + " / " + data.substring(part2 + 1));
			default:
				break;
			}
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		JSONObject original = new JSONObject(payload);
		Iterator<?> it = original.keys();
		while (it.hasNext()) {
			String elem = String.valueOf(it.next());
			this.put(elem, original.get(elem));
		}
		if (this.optString("name") == null) {
			String value;
			value = this.optString("preferred_name");
			if (value != null)
				this.put("name", value);
			else {
				value = this.optString("sub");
				if (value != null)
					this.put("name", value);
			}
		}
		validity = result;
	}

	public JWT() {
		try {
			this.put("sub", new String("anonymous"));
			this.put("name", new String("anonymous"));
			JSONArray roles = new JSONArray();
			roles.put(new String("anonymous"));
			this.put("roles", roles);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public JWT(JSONObject object) {
		super(object);
	}

	public boolean isValid() {
		return validity;
	}

	public String token() {
		String token = null;
		if (header != null) {
			token = "";
			token += Base64.getEncoder().encode(header.toString().getBytes(StandardCharsets.UTF_8));
			token += ".";
			token += Base64.getEncoder().encode(this.toString().getBytes(StandardCharsets.UTF_8));
			String signature = null;
			if (alg.equals("HS256")) {
			}
			token += ".";
			token += signature;
		}
		return token;
	}

	public List<String> roles() {
		JSONArray jroles = optJSONArray("roles");
		List<String> roles = new ArrayList<String>();
		if (jroles != null) {
			for (int i = 0; i < jroles.length(); i++) {
				roles.add(jroles.optString(i));
			}
		} else
			roles.add("anonymous");
		return roles;
	}
}
