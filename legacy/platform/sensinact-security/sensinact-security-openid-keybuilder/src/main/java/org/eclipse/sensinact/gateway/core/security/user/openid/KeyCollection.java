/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.security.user.openid;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * KeyCollection wraps certifcate response from the third party authentication server 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyCollection {

	private List<Keys> keys;
	
	public List<Keys> getKeys() {
		return keys;
	}

	public void setKeys(List<Keys> keys) {
		this.keys = keys;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Keys {
		@JsonProperty("kid")
		String keyId;
		
		@JsonProperty("kty")
		String type;
		
		@JsonProperty("alg")
		String algorithm;
		
		@JsonProperty(value = "n", required = false)
		String rsaModulus;

		@JsonProperty(value = "e", required = false)
		String rsaExponent;

		@JsonProperty(value = "crv", required = false)
		String ecCurve;
		
		@JsonProperty(value = "x", required = false)
		String ecXCoordinate;

		@JsonProperty(value = "y", required = false)
		String ecYCoordinate;
		
		@JsonProperty(value = "k")
		String symmetricKey;

		public String getKeyId() {
			return keyId;
		}

		public void setKeyId(String keyId) {
			this.keyId = keyId;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getAlgorithm() {
			return algorithm;
		}

		public void setAlgorithm(String algorithm) {
			this.algorithm = algorithm;
		}

		public String getRsaModulus() {
			return rsaModulus;
		}

		public void setRsaModulus(String rsaModulus) {
			this.rsaModulus = rsaModulus;
		}

		public String getRsaExponent() {
			return rsaExponent;
		}

		public void setRsaExponent(String rsaExponent) {
			this.rsaExponent = rsaExponent;
		}

		public String getEcCurve() {
			return ecCurve;
		}

		public void setEcCurve(String ecCurve) {
			this.ecCurve = ecCurve;
		}

		public String getEcXCoordinate() {
			return ecXCoordinate;
		}

		public void setEcXCoordinate(String ecXCoordinate) {
			this.ecXCoordinate = ecXCoordinate;
		}

		public String getEcYCoordinate() {
			return ecYCoordinate;
		}

		public void setEcYCoordinate(String ecYCoordinate) {
			this.ecYCoordinate = ecYCoordinate;
		}

		public String getSymmetricKey() {
			return symmetricKey;
		}

		public void setSymmetricKey(String symmetricKey) {
			this.symmetricKey = symmetricKey;
		}
	}
}