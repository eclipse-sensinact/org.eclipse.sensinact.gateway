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
package org.eclipse.sensinact.gateway.northbound.security.oidc.integration;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.stream.Stream;

import javax.crypto.spec.SecretKeySpec;

import org.eclipse.sensinact.gateway.northbound.security.oidc.Certificates;
import org.eclipse.sensinact.gateway.northbound.security.oidc.Certificates.KeyInfo;
import org.eclipse.sensinact.northbound.security.api.Authenticator;
import org.eclipse.sensinact.prototype.security.UserInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.osgi.service.cm.Configuration;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.sun.net.httpserver.HttpServer;

import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.jackson.io.JacksonSerializer;

/**
 * Tests variable handling
 */
@ExtendWith(ConfigurationExtension.class)
public class ValidatorTest {

    private static final Map<String, String> EC_CURVE_NAME_MAPPING = Map.of("ES256", "secp256r1", "ES384", "secp384r1",
            "ES512", "secp521r1");
    private final Map<SignatureAlgorithm, KeyPair> asymmetricKeys = new HashMap<>();
    private final Map<SignatureAlgorithm, Key> keys = new HashMap<>();
    private final ObjectMapper mapper = JsonMapper.builder().build();
    private HttpServer httpServer;
    private String DISCOVERY;

    @BeforeEach
    public void startCertificateEndpoint() throws Exception {

        for (SignatureAlgorithm sa : SignatureAlgorithm.values()) {
            if (sa == SignatureAlgorithm.NONE)
                continue;

            if (sa.isHmac()) {
                byte[] key = new byte[sa.getMinKeyLength()];
                new SecureRandom().nextBytes(key);
                keys.put(sa, new SecretKeySpec(key, sa.getJcaName()));
            } else if (sa.isRsa()) {
                KeyPairGenerator rsaKpg = KeyPairGenerator.getInstance("RSA");
                rsaKpg.initialize(sa.getMinKeyLength());
                asymmetricKeys.put(sa, rsaKpg.genKeyPair());
            } else if (sa.isEllipticCurve()) {
                KeyPairGenerator ecKpg = KeyPairGenerator.getInstance("EC");
                ecKpg.initialize(new ECGenParameterSpec(EC_CURVE_NAME_MAPPING.get(sa.getValue())));
                asymmetricKeys.put(sa, ecKpg.genKeyPair());
            } else {
                throw new IllegalArgumentException("Unknown key type " + sa.getDescription());
            }
        }

        Certificates certificates = new Certificates();

        certificates.setKeys(Stream.concat(asymmetricKeys.entrySet().stream().map(e -> {
            try {
                SignatureAlgorithm sa = e.getKey();
                KeyInfo info = new KeyInfo();
                info.setAlgorithm(sa.getValue());
                info.setKeyId(sa.getDescription());
                Encoder encoder = Base64.getEncoder();
                if (sa.isRsa()) {
                    KeyFactory kf = KeyFactory.getInstance("RSA");
                    info.setType("RSA");
                    RSAPublicKeySpec spec = kf.getKeySpec(e.getValue().getPublic(), RSAPublicKeySpec.class);
                    info.setRsaModulus(encoder.encodeToString(spec.getModulus().toByteArray()));
                    info.setRsaExponent(encoder.encodeToString(spec.getPublicExponent().toByteArray()));
                } else if (sa.isEllipticCurve()) {
                    KeyFactory kf = KeyFactory.getInstance("EC");
                    info.setType("EC");
                    ECPublicKeySpec spec = kf.getKeySpec(e.getValue().getPublic(), ECPublicKeySpec.class);
                    info.setEcCurve(sa.getValue());
                    info.setEcXCoordinate(encoder.encodeToString(spec.getW().getAffineX().toByteArray()));
                    info.setEcYCoordinate(encoder.encodeToString(spec.getW().getAffineY().toByteArray()));
                } else {
                    throw new IllegalArgumentException("Unknown key type " + sa.getDescription());
                }
                return info;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }), keys.entrySet().stream().map(e -> {
            try {
                SignatureAlgorithm sa = e.getKey();
                KeyInfo info = new KeyInfo();
                info.setAlgorithm(sa.getValue());
                info.setKeyId(sa.getDescription());
                Encoder encoder = Base64.getEncoder();
                if (sa.isHmac()) {
                    info.setType("HMAC");
                    info.setSymmetricKey(encoder.encodeToString(e.getValue().getEncoded()));
                } else {
                    throw new IllegalArgumentException("Unknown key type " + sa.getDescription());
                }
                return info;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        })).collect(toList()));

        httpServer = com.sun.net.httpserver.HttpServer.create();
        httpServer.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        httpServer.start();
        InetSocketAddress address = httpServer.getAddress();

        String base = String.format("http://%s:%d/", address.getAddress().getHostAddress(), address.getPort());

        DISCOVERY = base + "discovery";

        httpServer.createContext("/discovery", ex -> {
            ex.sendResponseHeaders(200, 0);
            mapper.writeValue(ex.getResponseBody(), Map.of("jwks_uri", base + "certificates"));
        });
        httpServer.createContext("/certificates", ex -> {
            ex.sendResponseHeaders(200, 0);
            mapper.writeValue(ex.getResponseBody(), certificates);
        });

    }

    @AfterEach
    public void clear() {
        httpServer.stop(0);
        httpServer = null;
        asymmetricKeys.clear();
        keys.clear();
    }

    @EnumSource(value = SignatureAlgorithm.class, mode = Mode.EXCLUDE, names = "NONE")
    @ParameterizedTest
    void testValidSignatures(SignatureAlgorithm alg,
            @InjectService(cardinality = 0) ServiceAware<Authenticator> service,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(name = "test", factoryPid = "sensinact.northbound.auth.oidc", location = "?")) Configuration config)
            throws Exception {

        config.update(new Hashtable<>(Map.of("realm", "test", "discoveryURL", DISCOVERY)));

        Date start = new Date(Instant.now().minus(Duration.ofHours(1)).toEpochMilli());
        Date end = new Date(Instant.now().plus(Duration.ofHours(1)).toEpochMilli());

        Key key = asymmetricKeys.containsKey(alg) ? asymmetricKeys.get(alg).getPrivate() : keys.get(alg);

        String token = Jwts.builder().setSubject("test").setIssuedAt(start).setExpiration(end)
                .setHeaderParam(JwsHeader.KEY_ID, alg.getDescription())
                .serializeToJsonWith(new JacksonSerializer<>(mapper)).signWith(key, alg).compact();

        UserInfo info = service.waitForService(5000).authenticate(null, token);

        assertEquals("test", info.getUserId());
        assertTrue(info.isAuthenticated());

    }
}
