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
import java.time.Duration;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.dynamic.HttpClientTransportDynamic;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.sensinact.core.security.UserInfo;
import org.eclipse.sensinact.gateway.northbound.security.oidc.Certificates.KeyInfo;
import org.eclipse.sensinact.northbound.security.api.Authenticator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtHandlerAdapter;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SigningKeyResolver;
import io.jsonwebtoken.jackson.io.JacksonDeserializer;

@Component(configurationPid = "sensinact.northbound.auth.oidc", configurationPolicy = ConfigurationPolicy.REQUIRE)
public class OIDCTokenValidator {

    private static final Logger LOG = LoggerFactory.getLogger(OIDCTokenValidator.class);

    public @interface AuthenticationServiceConfig {

        String discoveryURL();

        boolean ignore_ssl_errors() default false;

        String keystore() default "";

        String _keystore_password() default "";

        String truststore() default "";

        String _truststore_password() default "";

        int tokenRefresh() default 600;

        int gracePeriod() default 300;

        String realm();
    }

    private final ObjectMapper mapper = JsonMapper.builder().build();

    private boolean running = true;

    private BundleContext ctx;

    private AuthenticationServiceConfig configuration;

    private HttpClient client;

    private List<KeyInfo> keys;

    private ServiceRegistration<Authenticator> reg;

    private final Object lock = new Object();

    @Activate
    void activate(BundleContext ctx, AuthenticationServiceConfig configuration) throws Exception {

        this.configuration = configuration;
        this.ctx = ctx;

        String discovery = configuration.discoveryURL();
        if (discovery == null || discovery.isBlank()) {
            throw new IllegalArgumentException("The OpenId Connect discovery URL is not set.");
        }

        this.client = getClient();
        client.getExecutor().execute(this::checkAndUpdate);
    }

    @Deactivate
    void stop() {
        ServiceRegistration<?> toUnregister;
        synchronized (lock) {
            running = false;
            toUnregister = this.reg;
            this.reg = null;
        }

        safeUnregister(toUnregister);

        try {
            client.stop();
        } catch (Exception e) {
            // Not a problem
        }
    }

    private HttpClient getClient() throws Exception {
        // SSL configuration
        final SslContextFactory.Client sslContextFactory = new SslContextFactory.Client(
                configuration.ignore_ssl_errors());
        String keystore = configuration.keystore();
        if (!keystore.isBlank()) {
            sslContextFactory.setKeyStorePath(keystore);
            String keystorePw = configuration._keystore_password();
            if (!keystorePw.isBlank()) {
                sslContextFactory.setKeyStorePassword(keystorePw);
            }
        }

        String truststore = configuration.truststore();
        if (!truststore.isBlank()) {
            sslContextFactory.setTrustStorePath(truststore);
            String truststorePw = configuration._truststore_password();
            if (!truststorePw.isBlank()) {
                sslContextFactory.setTrustStorePassword(truststorePw);
            }
        }

        // Setup the connector using shared resources
        final ClientConnector clientConnector = new ClientConnector();
        clientConnector.setSslContextFactory(sslContextFactory);
        clientConnector.setConnectTimeout(Duration.ofSeconds(5));

        // Construct the client with shared address resolver
        final HttpClient client = new HttpClient(new HttpClientTransportDynamic(clientConnector));
        client.start();
        return client;
    }

    private void checkAndUpdate() {
        List<KeyInfo> loadKeys = loadKeys();

        ServiceRegistration<?> toUnregister;
        boolean reschedule;
        if (loadKeys == null) {
            synchronized (lock) {
                keys = null;
                toUnregister = this.reg;
                this.reg = null;
                reschedule = running;
            }
        } else {
            synchronized (lock) {
                if (!loadKeys.equals(keys)) {
                    keys = loadKeys;
                    toUnregister = this.reg;
                    this.reg = null;
                }
            }
            JwtParser parser = Jwts.parserBuilder().deserializeJsonWith(new JacksonDeserializer<>(mapper))
                    .setSigningKeyResolver(new KeyResolver(loadKeys)).build();
            ServiceRegistration<Authenticator> registration = ctx.registerService(Authenticator.class,
                    new Validator(configuration.realm(), parser), null);

            synchronized (lock) {
                if (loadKeys.equals(keys) && this.reg == null && running) {
                    this.reg = registration;
                    toUnregister = null;
                } else {
                    toUnregister = registration;
                }
                reschedule = running;
            }
        }
        safeUnregister(toUnregister);

        if (reschedule) {
            client.getScheduler().schedule(this::checkAndUpdate, configuration.tokenRefresh(), TimeUnit.SECONDS);
        }
    }

    private void safeUnregister(ServiceRegistration<?> reg) {
        if (reg != null) {
            try {
                reg.unregister();
            } catch (IllegalStateException ise) {
            }
        }
    }

    private List<KeyInfo> loadKeys() {
        try {
            InputStreamResponseListener listener = new InputStreamResponseListener();
            client.newRequest(configuration.discoveryURL()).accept("application/json").send(listener);

            JsonNode discovered = mapper.readTree(listener.getInputStream());

            String certs = discovered.get("jwks_uri").textValue();

            if (certs == null || certs.isBlank()) {
                LOG.warn("No URI available to query public keys");
                return null;
            } else {
                listener = new InputStreamResponseListener();
                client.newRequest(certs).accept("application/json").send(listener);
                return mapper.readValue(listener.getInputStream(), Certificates.class).getKeys();
            }
        } catch (Exception e) {
            LOG.error("An error occurred while loading the validation keys", e);
            return null;
        }
    }

    private static class Validator implements Authenticator {

        private final JwtParser parser;
        private final String realm;

        public Validator(String realm, JwtParser parser) {
            super();
            this.realm = realm;
            this.parser = parser;
        }

        @Override
        public UserInfo authenticate(String user, String credential) {
            return parser.parse(credential, new JwtHandlerAdapter<>() {

                @Override
                public UserInfo onClaimsJws(Jws<Claims> jws) {
                    return new JwsUserInfo(jws, j -> Set.of());
                }

            });
        }

        @Override
        public String getRealm() {
            return realm;
        }

        @Override
        public Scheme getScheme() {
            return Scheme.TOKEN;
        }
    }

    private static class KeyResolver implements SigningKeyResolver {

        private final List<KeyInfo> keys;
        private final Map<String, Key> cachedKeys = new ConcurrentHashMap<>();

        public KeyResolver(List<KeyInfo> keys) {
            this.keys = List.copyOf(keys);
        }

        @Override
        public Key resolveSigningKey(@SuppressWarnings("rawtypes") JwsHeader header, Claims claims) {
            return fromCache(header);
        }

        private Key fromCache(JwsHeader<?> header) {
            return cachedKeys.computeIfAbsent(header.getKeyId(), k -> createKey(k, header.getAlgorithm()));
        }

        private Key createKey(String id, String algorithm) {
            return keys.stream().filter(k -> id.equalsIgnoreCase(k.getKeyId())).findFirst()
                    .map(k -> toKey(SignatureAlgorithm.forName(algorithm), k)).orElse(null);
        }

        private Key toKey(SignatureAlgorithm alg, KeyInfo ki) {
            try {
                switch (alg) {
                case NONE:
                default:
                    return null;
                case ES256:
                case ES384:
                case ES512:
                    return toECKey(ki);
                case HS256:
                case HS384:
                case HS512:
                    return toSecretKey(ki);
                case PS256:
                case PS384:
                case PS512:
                case RS256:
                case RS384:
                case RS512:
                    return toRSAKey(ki);
                }
            } catch (GeneralSecurityException gse) {
                LOG.error("Failed to create verification key for {}", ki.getKeyId(), gse);
                return null;
            }
        }

        private static final Map<String, String> EC_CURVE_NAME_MAPPING = Map.of("ES256", "secp256r1", "ES384",
                "secp384r1", "ES512", "secp521r1");

        private Key toECKey(KeyInfo ki)
                throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidParameterSpecException {

            Decoder decoder = Base64.getDecoder();

            BigInteger x = new BigInteger(1, decoder.decode(ki.getEcXCoordinate()));
            BigInteger y = new BigInteger(1, decoder.decode(ki.getEcYCoordinate()));

            AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
            parameters.init(new ECGenParameterSpec(EC_CURVE_NAME_MAPPING.get(ki.getEcCurve())));

            return KeyFactory.getInstance("EC").generatePublic(
                    new ECPublicKeySpec(new ECPoint(x, y), parameters.getParameterSpec(ECParameterSpec.class)));

        }

        private Key toSecretKey(KeyInfo ki) throws InvalidKeySpecException, NoSuchAlgorithmException {
            Decoder decoder = Base64.getDecoder();
            return new SecretKeySpec(decoder.decode(ki.getSymmetricKey()),
                    SignatureAlgorithm.forName(ki.getAlgorithm()).getJcaName());
        }

        private Key toRSAKey(KeyInfo ki) throws InvalidKeySpecException, NoSuchAlgorithmException {
            Decoder decoder = Base64.getDecoder();

            BigInteger modulus = new BigInteger(1, decoder.decode(ki.getRsaModulus()));
            BigInteger publicExponent = new BigInteger(1, decoder.decode(ki.getRsaExponent()));

            return KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, publicExponent));
        }

        @Override
        public Key resolveSigningKey(@SuppressWarnings("rawtypes") JwsHeader header, String plaintext) {
            return fromCache(header);
        }

    }
}
