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

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.spec.SecretKeySpec;

import org.eclipse.sensinact.gateway.northbound.security.oidc.Certificates.KeyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.Locator;
import io.jsonwebtoken.ProtectedHeader;

class KeyResolver implements Locator<Key> {

    private static final Logger LOG = LoggerFactory.getLogger(KeyResolver.class);

    private final List<KeyInfo> keys;
    private final Map<String, Key> cachedKeys = new ConcurrentHashMap<>();

    public KeyResolver(List<KeyInfo> keys) {
        this.keys = List.copyOf(keys);
    }

    @Override
    public Key locate(Header header) {
        Objects.requireNonNull(header, "Null JWT header");
        if (header instanceof ProtectedHeader ph) {
            return fromCache(ph);
        }
        return null;
    }

    private Key fromCache(ProtectedHeader header) {
        return cachedKeys.computeIfAbsent(header.getKeyId(), k -> createKey(k, header.getAlgorithm()));
    }

    private Key createKey(String id, String algorithm) {
        return keys.stream().filter(k -> id.equalsIgnoreCase(k.getKeyId())).findFirst()
                .map(k -> toKey(algorithm, k)).orElse(null);
    }

    private Key toKey(String alg, KeyInfo ki) {
        try {
            switch (alg) {
                default:
                    return null;
                case "ES256":
                case "ES384":
                case "ES512":
                    return toECKey(ki);
                case "HS256":
                case "HS384":
                case "HS512":
                    return toSecretKey(ki);
                case "PS256":
                case "PS384":
                case "PS512":
                case "RS256":
                case "RS384":
                case "RS512":
                    return toRSAKey(ki);
            }
        } catch (IllegalArgumentException | GeneralSecurityException gse) {
            LOG.error("Failed to create verification key for {}", ki.getKeyId(), gse);
            return null;
        }
    }

    private static final Map<String, String> EC_CURVE_NAME_MAPPING = Map.of("ES256", "secp256r1", "ES384", "secp384r1",
            "ES512", "secp521r1");

    private Key toECKey(KeyInfo ki)
            throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidParameterSpecException {

        Decoder decoder = Base64.getUrlDecoder();

        BigInteger x = new BigInteger(1, decoder.decode(ki.getEcXCoordinate()));
        BigInteger y = new BigInteger(1, decoder.decode(ki.getEcYCoordinate()));

        AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
        parameters.init(new ECGenParameterSpec(EC_CURVE_NAME_MAPPING.get(ki.getEcCurve())));

        return KeyFactory.getInstance("EC").generatePublic(
                new ECPublicKeySpec(new ECPoint(x, y), parameters.getParameterSpec(ECParameterSpec.class)));

    }

    private Key toSecretKey(KeyInfo ki) throws InvalidKeySpecException, NoSuchAlgorithmException {
        Decoder decoder = Base64.getUrlDecoder();
        return new SecretKeySpec(decoder.decode(ki.getSymmetricKey()),
                ki.getAlgorithm().replace("HS", "HmacSHA"));
    }

    private Key toRSAKey(KeyInfo ki) throws InvalidKeySpecException, NoSuchAlgorithmException {
        Decoder decoder = Base64.getUrlDecoder();

        BigInteger modulus = new BigInteger(1, decoder.decode(ki.getRsaModulus()));
        BigInteger publicExponent = new BigInteger(1, decoder.decode(ki.getRsaExponent()));

        return KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, publicExponent));
    }
}
