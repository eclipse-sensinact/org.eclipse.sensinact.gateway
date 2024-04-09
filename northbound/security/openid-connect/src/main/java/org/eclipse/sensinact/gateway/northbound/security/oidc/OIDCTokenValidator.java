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

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.dynamic.HttpClientTransportDynamic;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.sensinact.gateway.northbound.security.oidc.Certificates.KeyInfo;
import org.eclipse.sensinact.northbound.security.api.Authenticator;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
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
import io.jsonwebtoken.JwtHandlerAdapter;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
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
        LOG.info("Starting OIDC validation for {}", discovery);

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
}
