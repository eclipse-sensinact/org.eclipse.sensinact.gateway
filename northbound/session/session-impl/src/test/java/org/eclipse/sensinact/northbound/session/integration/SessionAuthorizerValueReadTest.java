/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.northbound.session.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.eclipse.sensinact.core.authorization.Authorizer;
import org.eclipse.sensinact.core.authorization.PermissionLevel;
import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.ValueType;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceValueFilter;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.northbound.security.api.AuthorizationEngine;
import org.eclipse.sensinact.northbound.security.api.PreAuthorizer;
import org.eclipse.sensinact.northbound.security.api.PreAuthorizer.PreAuth;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.eclipse.sensinact.northbound.session.ResourceDescription;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.eclipse.sensinact.northbound.session.impl.TestUserInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * The value of a resource needs READ: DESCRIBE gives its name, its type and its metadata, not its value. A caller that
 * cannot read a resource sees it as a resource that has no value yet.
 */
@WithConfiguration(pid = "sensinact.session.manager", properties = {
        @Property(key = "auth.policy", value = "ALLOW_ALL"),
        @Property(key = "name", value = "SessionAuthorizerValueReadTest"),
        @Property(key = "authorization.target", value = "(scope=SessionAuthorizerValueReadTest)")
})
public class SessionAuthorizerValueReadTest {

    private static final String PROVIDER = "ValueReadProvider";

    private static final UserInfo BOB = new TestUserInfo("bob", true);

    /** The resource of the provider that the caller may read, and the one it may not */
    private static final String READABLE = "readable";
    private static final String HIDDEN = "hidden";

    private final List<String> readCalls = new CopyOnWriteArrayList<>();

    private ServiceRegistration<AuthorizationEngine> registration;

    @InjectBundleContext
    BundleContext context;

    @InjectService(filter = "(name=SessionAuthorizerValueReadTest)", timeout = 5000)
    SensiNactSessionManager sessionManager;

    @BeforeEach
    void setup(@InjectService DataUpdate push) throws Exception {
        for (String resource : new String[] { READABLE, HIDDEN }) {
            GenericDto dto = new GenericDto();
            dto.provider = PROVIDER;
            dto.service = "svc";
            dto.resource = resource;
            dto.value = 42;
            dto.type = Integer.class;
            dto.timestamp = Instant.now();
            push.pushUpdate(dto).getValue();
        }
        useEngine(PreAuth.UNKNOWN);
    }

    @AfterEach
    void cleanup(@InjectService GatewayThread thread) throws Exception {
        registration.unregister();
        thread.execute(new AbstractTwinCommand<Void>() {
            @Override
            protected Promise<Void> call(SensinactDigitalTwin twin, PromiseFactory pf) {
                SensinactProvider sp = twin.getProvider(PROVIDER);
                if (sp != null) {
                    sp.delete();
                }
                return pf.resolved(null);
            }
        }).getValue();
    }

    /** Use an engine that answers the pre-authorization of READ as given and denies READ on the hidden resource */
    private void useEngine(PreAuth readPreAuth) throws InterruptedException {
        if (registration != null) {
            registration.unregister();
        }
        Hashtable<String, Object> properties = new Hashtable<>();
        properties.put("scope", "SessionAuthorizerValueReadTest");
        registration = context.registerService(AuthorizationEngine.class, new Engine(readPreAuth), properties);
        // The session manager binds the engine dynamically and expires its sessions: wait until a new one uses it
        final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            try {
                sessionManager.getDefaultSession(BOB).describeResource(PROVIDER, "svc", HIDDEN);
                // The engine is bound once its answer can be seen: UNKNOWN asks the authorizer, ALLOW shows the
                // hidden value, DENY hides the readable one
                final boolean bound = switch (readPreAuth) {
                case UNKNOWN -> !readCalls.isEmpty();
                case ALLOW -> sessionManager.getDefaultSession(BOB).describeResource(PROVIDER, "svc", HIDDEN).value != null;
                case DENY -> sessionManager.getDefaultSession(BOB).describeResource(PROVIDER, "svc", READABLE).value == null;
                };
                if (bound) {
                    readCalls.clear();
                    return;
                }
            } catch (RuntimeException e) {
                // A session expired by the binding: try again with a new one
            }
            Thread.sleep(50);
        }
        throw new AssertionError("The engine was never used");
    }

    private SensiNactSession session() {
        return sessionManager.getDefaultSession(BOB);
    }

    private static void assertNoValue(ResourceSnapshot rs) {
        assertFalse(rs.isSet());
        assertNull(rs.getValue().getValue());
        assertNull(rs.getValue().getTimestamp());
        // The rest of the description is kept
        assertEquals(Integer.class, rs.getType());
    }

    @Test
    void testDescribeResource() {
        ResourceDescription readable = session().describeResource(PROVIDER, "svc", READABLE);
        assertEquals(42, readable.value);
        assertNotNull(readable.timestamp);

        ResourceDescription hidden = session().describeResource(PROVIDER, "svc", HIDDEN);
        assertNull(hidden.value);
        assertNull(hidden.timestamp);
        assertEquals(ValueType.UPDATABLE, hidden.valueType);
        assertEquals(Integer.class, hidden.contentType);
        assertEquals(readable.metadata.keySet(), hidden.metadata.keySet());
        assertFalse(hidden.metadata.isEmpty());
    }

    @Test
    void testProviderSnapshot() {
        ProviderSnapshot snapshot = session().providerSnapshot(PROVIDER,
                EnumSet.noneOf(SensinactDigitalTwin.SnapshotOption.class));
        assertEquals(42, snapshot.getResource("svc", READABLE).getValue().getValue());
        assertNoValue(snapshot.getResource("svc", HIDDEN));
        // DESCRIBE keeps the metadata
        assertFalse(snapshot.getResource("svc", HIDDEN).getMetadata().isEmpty());
        assertEquals(session().describeResource(PROVIDER, "svc", HIDDEN).metadata,
                snapshot.getResource("svc", HIDDEN).getMetadata());
        // The parents of a resource give no other way to the value
        assertNoValue(snapshot.getResource("svc", HIDDEN).getService().getResource(HIDDEN));
        assertNoValue(snapshot.getResource("svc", HIDDEN).getService().getProvider().getResource("svc", HIDDEN));
    }

    @Test
    void testServiceAndResourceSnapshots() {
        ServiceSnapshot service = session().serviceSnapshot(PROVIDER, "svc");
        assertEquals(42, service.getResource(READABLE).getValue().getValue());
        assertNoValue(service.getResource(HIDDEN));
        assertNoValue(service.getProvider().getResource("svc", HIDDEN));

        ResourceSnapshot hidden = session().resourceSnapshot(PROVIDER, "svc", HIDDEN);
        assertNoValue(hidden);
        assertNoValue(hidden.getService().getProvider().getResource("svc", HIDDEN));
        assertEquals(42, session().resourceSnapshot(PROVIDER, "svc", READABLE).getValue().getValue());
    }

    @Test
    void testFilteredSnapshot() {
        ProviderSnapshot snapshot = session().filteredSnapshot(null).stream()
                .filter(p -> PROVIDER.equals(p.getName())).findFirst().orElseThrow();
        assertEquals(42, snapshot.getResource("svc", READABLE).getValue().getValue());
        assertNoValue(snapshot.getResource("svc", HIDDEN));
    }

    /** A filter on the values, which must not tell whether the hidden value is the one it looks for */
    private static ICriterion valueIs(String resource, Object expected) {
        return new ICriterion() {
            @Override
            public BiPredicate<ProviderSnapshot, GeoJsonObject> getLocationFilter() {
                return null;
            }

            @Override
            public Predicate<ProviderSnapshot> getProviderFilter() {
                return ps -> PROVIDER.equals(ps.getName());
            }

            @Override
            public Predicate<ServiceSnapshot> getServiceFilter() {
                return null;
            }

            @Override
            public Predicate<ResourceSnapshot> getResourceFilter() {
                return null;
            }

            @Override
            public ResourceValueFilter getResourceValueFilter() {
                return (p, resources) -> resources.stream().anyMatch(
                        r -> r.getName().equals(resource) && expected.equals(r.getValue().getValue()));
            }
        };
    }

    @Test
    void testValueFilterIsNotAnOracle() {
        assertEquals(1, session().filteredSnapshot(valueIs(READABLE, 42)).size());
        assertTrue(session().filteredSnapshot(valueIs(HIDDEN, 42)).isEmpty());
    }

    @Test
    void testPreAuthorizerShortCircuits() throws Exception {
        // ALLOW and DENY are not asked again to the authorizer
        useEngine(PreAuth.ALLOW);
        assertEquals(42, session().describeResource(PROVIDER, "svc", HIDDEN).value);
        assertEquals(42, session().providerSnapshot(PROVIDER, EnumSet.noneOf(
                SensinactDigitalTwin.SnapshotOption.class)).getResource("svc", HIDDEN).getValue().getValue());
        assertTrue(readCalls.isEmpty(), readCalls.toString());

        useEngine(PreAuth.DENY);
        assertNull(session().describeResource(PROVIDER, "svc", READABLE).value);
        assertNoValue(session().providerSnapshot(PROVIDER, EnumSet.noneOf(
                SensinactDigitalTwin.SnapshotOption.class)).getResource("svc", READABLE));
        assertTrue(readCalls.isEmpty(), readCalls.toString());
    }

    @Test
    void testOneAuthorizerCallPerResource() {
        session().providerSnapshot(PROVIDER, EnumSet.noneOf(SensinactDigitalTwin.SnapshotOption.class));
        assertEquals(1, readCalls.stream().filter(c -> c.endsWith("/" + HIDDEN)).count(), readCalls.toString());
        assertEquals(1, readCalls.stream().filter(c -> c.endsWith("/" + READABLE)).count(), readCalls.toString());
    }

    private final class Engine implements AuthorizationEngine {

        private final PreAuth readPreAuth;

        Engine(PreAuth readPreAuth) {
            this.readPreAuth = readPreAuth;
        }

        @Override
        public PreAuthorizer createPreAuthorizer(UserInfo user) {
            return new PreAuthorizer() {
                @Override
                public PreAuth preAuthProvider(PermissionLevel level, String provider) {
                    return PreAuth.UNKNOWN;
                }

                @Override
                public PreAuth preAuthService(PermissionLevel level, String provider, String service) {
                    return PreAuth.UNKNOWN;
                }

                @Override
                public PreAuth preAuthResource(PermissionLevel level, String provider, String service,
                        String resource) {
                    return level == PermissionLevel.READ ? readPreAuth : PreAuth.UNKNOWN;
                }
            };
        }

        @Override
        public Authorizer createAuthorizer(UserInfo user) {
            return new Authorizer() {
                @Override
                public boolean hasProviderPermission(PermissionLevel level, String uri, String model, String provider) {
                    return true;
                }

                @Override
                public boolean hasServicePermission(PermissionLevel level, String uri, String model, String provider,
                        String service) {
                    return true;
                }

                @Override
                public boolean hasResourcePermission(PermissionLevel level, String uri, String model, String provider,
                        String service, String resource) {
                    if (level != PermissionLevel.READ) {
                        return true;
                    }
                    readCalls.add(provider + "/" + service + "/" + resource);
                    return !HIDDEN.equals(resource);
                }
            };
        }
    }
}
