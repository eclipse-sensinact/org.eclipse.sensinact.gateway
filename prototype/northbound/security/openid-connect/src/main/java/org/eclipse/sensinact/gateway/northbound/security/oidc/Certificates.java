/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.northbound.security.oidc;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Certifcates wraps certifcate response from the third party authentication
 * server
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Certificates {

    private List<KeyInfo> keys;

    public List<KeyInfo> getKeys() {
        return keys;
    }

    public void setKeys(List<KeyInfo> keys) {
        this.keys = keys;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KeyInfo {
        @JsonProperty("kid")
        String keyId;

        @JsonProperty("kty")
        String type;

        @JsonProperty(value = "use", required = false)
        String use;

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

        public String getUse() {
            return use;
        }

        public void setUse(String use) {
            this.use = use;
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

        @Override
        public int hashCode() {
            return Objects.hash(algorithm, ecCurve, ecXCoordinate, ecYCoordinate, keyId, rsaExponent, rsaModulus,
                    symmetricKey, type);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            KeyInfo other = (KeyInfo) obj;
            return Objects.equals(algorithm, other.algorithm) && Objects.equals(ecCurve, other.ecCurve)
                    && Objects.equals(ecXCoordinate, other.ecXCoordinate)
                    && Objects.equals(ecYCoordinate, other.ecYCoordinate) && Objects.equals(keyId, other.keyId)
                    && Objects.equals(rsaExponent, other.rsaExponent) && Objects.equals(rsaModulus, other.rsaModulus)
                    && Objects.equals(symmetricKey, other.symmetricKey) && Objects.equals(type, other.type);
        }

    }
}
