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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.eclipse.sensinact.core.authorization.Authorizer;
import org.eclipse.sensinact.core.authorization.NotPermittedException;
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
    /** The questions of UPDATE: the model and the target */
    private final List<String> updateCalls = new CopyOnWriteArrayList<>();

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
                for (String name : new String[] { PROVIDER, "CreatedProvider", "NewProvider", "ModelProvider" }) {
                    SensinactProvider sp = twin.getProvider(name);
                    if (sp != null) {
                        sp.delete();
                    }
                }
                return pf.resolved(null);
            }
        }).getValue();
    }

    /** Use an engine that answers the pre-authorization of READ as given and denies READ on the hidden resource */
    private void useEngine(PreAuth readPreAuth) throws InterruptedException {
        useEngine(readPreAuth, PreAuth.UNKNOWN);
    }

    private void useEngine(PreAuth readPreAuth, PreAuth updatePreAuth) throws InterruptedException {
        if (registration != null) {
            registration.unregister();
        }
        Hashtable<String, Object> properties = new Hashtable<>();
        properties.put("scope", "SessionAuthorizerValueReadTest");
        final Engine engine = new Engine(readPreAuth, updatePreAuth);
        registration = context.registerService(AuthorizationEngine.class, engine, properties);
        // The session manager binds the engine dynamically and expires its sessions: wait until a new one uses it
        final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            try {
                sessionManager.getDefaultSession(BOB).describeResource(PROVIDER, "svc", HIDDEN);
                // The engine is bound once a session asked it for its pre-authorizer
                if (engine.used) {
                    readCalls.clear();
                    updateCalls.clear();
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

    /** A filter on the providers or the services, with no value filter */
    private static ICriterion filter(Predicate<ProviderSnapshot> providerFilter,
            Predicate<ServiceSnapshot> serviceFilter) {
        return new ICriterion() {
            @Override
            public BiPredicate<ProviderSnapshot, GeoJsonObject> getLocationFilter() {
                return null;
            }

            @Override
            public Predicate<ProviderSnapshot> getProviderFilter() {
                return providerFilter;
            }

            @Override
            public Predicate<ServiceSnapshot> getServiceFilter() {
                return serviceFilter;
            }

            @Override
            public Predicate<ResourceSnapshot> getResourceFilter() {
                return null;
            }

            @Override
            public ResourceValueFilter getResourceValueFilter() {
                return null;
            }
        };
    }

    @Test
    void testFilteredSnapshotProviderFilter() {
        List<ProviderSnapshot> snapshots = session().filteredSnapshot(filter(ps -> PROVIDER.equals(ps.getName()), null));
        assertEquals(List.of(PROVIDER), snapshots.stream().map(ProviderSnapshot::getName).toList());
        assertEquals(42, snapshots.get(0).getResource("svc", READABLE).getValue().getValue());
        assertNoValue(snapshots.get(0).getResource("svc", HIDDEN));
    }

    @Test
    void testFilteredSnapshotServiceFilter(@InjectService DataUpdate push) throws Exception {
        // A second service, which the filter must leave out
        GenericDto dto = new GenericDto();
        dto.provider = PROVIDER;
        dto.service = "other";
        dto.resource = "extra";
        dto.value = 42;
        dto.type = Integer.class;
        push.pushUpdate(dto).getValue();

        List<ProviderSnapshot> snapshots = session().filteredSnapshot(filter(null, ss -> "svc".equals(ss.getName())));
        assertTrue(snapshots.stream().flatMap(ps -> ps.getServices().stream()).allMatch(
                ss -> "svc".equals(ss.getName())));
        ProviderSnapshot snapshot = snapshots.stream().filter(p -> PROVIDER.equals(p.getName())).findFirst()
                .orElseThrow();
        assertEquals(List.of("svc"), snapshot.getServices().stream().map(ServiceSnapshot::getName).toList());
        assertEquals(42, snapshot.getResource("svc", READABLE).getValue().getValue());
        assertNoValue(snapshot.getResource("svc", HIDDEN));
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

    private String modelUriOf(GatewayThread thread, String provider) throws Exception {
        return thread.execute(new AbstractTwinCommand<String>() {
            @Override
            protected Promise<String> call(SensinactDigitalTwin twin, PromiseFactory pf) {
                return pf.resolved(twin.getProvider(provider).getModelPackageUri());
            }
        }).getValue();
    }

    private Object valueOf(GatewayThread thread, String provider, String resource) throws Exception {
        return thread.execute(new AbstractTwinCommand<Object>() {
            @Override
            protected Promise<Object> call(SensinactDigitalTwin twin, PromiseFactory pf) {
                SensinactProvider sp = twin.getProvider(provider);
                if (sp == null) {
                    return pf.resolved(null);
                }
                return twin.getResource(provider, "svc", resource).getValue(Object.class).map(tv -> tv.getValue());
            }
        }).getValue();
    }

    /** The update of a provider writes resources: each one needs UPDATE and one refusal refuses all of them */
    @Test
    void testSetProviderAsksUpdateOfEachResource(@InjectService DataUpdate push, @InjectService GatewayThread thread)
            throws Exception {
        session().setProvider(PROVIDER, new HashMap<>(Map.of("svc/" + READABLE, 1)), push);
        assertEquals(1, valueOf(thread, PROVIDER, READABLE));

        // One resource that is refused: nothing is written, the one that is allowed neither
        assertThrows(NotPermittedException.class, () -> session().setProvider(PROVIDER,
                new HashMap<>(Map.of("svc/" + READABLE, 2, "svc/" + HIDDEN, 2)), push));
        assertEquals(1, valueOf(thread, PROVIDER, READABLE));
        assertEquals(42, valueOf(thread, PROVIDER, HIDDEN));

        // The creation of a provider is decided the same way
        assertThrows(NotPermittedException.class, () -> session().setProvider("CreatedProvider",
                new HashMap<>(Map.of("svc/" + HIDDEN, 2)), push));
        assertNull(valueOf(thread, "CreatedProvider", HIDDEN));
    }

    /** The model of an existing provider is its own: another one is refused, its own one is accepted */
    @Test
    void testSetProviderRefusesAnotherModelThanTheProviderOne(@InjectService DataUpdate push,
            @InjectService GatewayThread thread) throws Exception {
        final String uri = modelUriOf(thread, PROVIDER);
        assertThrows(IllegalArgumentException.class, () -> session().setProvider(PROVIDER,
                new HashMap<>(Map.of("@model", "otherModel", "svc/" + READABLE, 3)), push));
        assertThrows(IllegalArgumentException.class, () -> session().setProvider(PROVIDER,
                new HashMap<>(Map.of("@modelPackageUri", "https://example.com/other/", "svc/" + READABLE, 3)), push));
        assertEquals(42, valueOf(thread, PROVIDER, READABLE));

        // A user who cannot write is not told that the provider exists: same refusal as for a missing provider
        assertThrows(NotPermittedException.class, () -> session().setProvider(PROVIDER,
                new HashMap<>(Map.of("@model", "otherModel", "svc/" + HIDDEN, 3)), push));
        assertThrows(NotPermittedException.class, () -> session().setProvider("NewProvider",
                new HashMap<>(Map.of("@model", "otherModel", "svc/" + HIDDEN, 3)), push));
        // A blank package is an absent one
        updateCalls.clear();
        session().setProvider(PROVIDER, new HashMap<>(Map.of("@modelPackageUri", " ", "svc/" + READABLE, 3)), push);
        assertEquals(3, valueOf(thread, PROVIDER, READABLE));
        assertEquals(List.of(uri + " " + PROVIDER + " " + PROVIDER + "/svc/" + READABLE), updateCalls);
        updateCalls.clear();

        // Nothing to write: no refusal, which would only tell that the provider exists
        session().setProvider(PROVIDER, new HashMap<>(Map.of("@model", "otherModel")), push);
        updateCalls.clear();

        session().setProvider(PROVIDER,
                new HashMap<>(Map.of("@modelPackageUri", uri, "@model", PROVIDER, "svc/" + READABLE, 3)), push);
        assertEquals(3, valueOf(thread, PROVIDER, READABLE));
        assertEquals(List.of(uri + " " + PROVIDER + " " + PROVIDER + "/svc/" + READABLE), updateCalls);
    }

    /** A new provider is asked about the model and package that the push writes: the requested ones, else its own name and the default package */
    @Test
    void testSetProviderAsksTheModelOfANewProvider(@InjectService DataUpdate push, @InjectService GatewayThread thread)
            throws Exception {
        // The default package of the core, which setProvider copies
        assertEquals("https://eclipse.org/sensinact/" + PROVIDER, modelUriOf(thread, PROVIDER));
        session().setProvider("NewProvider", new HashMap<>(Map.of("svc/" + READABLE, 6)), push);
        assertEquals(6, valueOf(thread, "NewProvider", READABLE));
        assertEquals(List.of("https://eclipse.org/sensinact/NewProvider NewProvider NewProvider/svc/" + READABLE), updateCalls);

        updateCalls.clear();
        session().setProvider("ModelProvider", new HashMap<>(Map.of("@model", "requestedModel", "svc/" + READABLE, 7)),
                push);
        assertEquals(7, valueOf(thread, "ModelProvider", READABLE));
        assertEquals(List.of("https://eclipse.org/sensinact/requestedModel requestedModel ModelProvider/svc/" + READABLE), updateCalls);
    }

    /** Nothing to write, nothing to ask */
    @Test
    void testSetProviderWithoutResource(@InjectService DataUpdate push) throws Exception {
        session().setProvider(PROVIDER, new HashMap<>(), push);
        assertTrue(updateCalls.isEmpty(), updateCalls.toString());
    }

    /** The pre-authorizer answers first: a DENY refuses everything before any question or write */
    @Test
    void testSetProviderPreAuthorizerDeny(@InjectService DataUpdate push, @InjectService GatewayThread thread)
            throws Exception {
        useEngine(PreAuth.UNKNOWN, PreAuth.DENY);
        assertThrows(NotPermittedException.class, () -> session().setProvider(PROVIDER,
                new HashMap<>(Map.of("svc/" + READABLE, 4, "svc/" + HIDDEN, 4)), push));
        assertTrue(updateCalls.isEmpty(), updateCalls.toString());
        assertEquals(42, valueOf(thread, PROVIDER, READABLE));
    }

    /** An ALLOW of the pre-authorizer is not asked again, the other resources are */
    @Test
    void testSetProviderPreAuthorizerAllow(@InjectService DataUpdate push, @InjectService GatewayThread thread)
            throws Exception {
        useEngine(PreAuth.UNKNOWN, PreAuth.ALLOW);
        session().setProvider(PROVIDER, new HashMap<>(Map.of("svc/" + READABLE, 5, "svc/" + HIDDEN, 5)), push);
        assertEquals(5, valueOf(thread, PROVIDER, READABLE));
        assertEquals(5, valueOf(thread, PROVIDER, HIDDEN));
        assertEquals(1, updateCalls.size(), updateCalls.toString());
        assertTrue(updateCalls.get(0).endsWith("/" + READABLE), updateCalls.toString());
    }

    private final class Engine implements AuthorizationEngine {

        private final PreAuth readPreAuth;

        private final PreAuth updatePreAuth;

        /** Set when a session uses this engine, which tells it from the previous one */
        volatile boolean used;

        Engine(PreAuth readPreAuth, PreAuth updatePreAuth) {
            this.readPreAuth = readPreAuth;
            this.updatePreAuth = updatePreAuth;
        }

        @Override
        public PreAuthorizer createPreAuthorizer(UserInfo user) {
            used = true;
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
                    return switch (level) {
                    case READ -> readPreAuth;
                    case UPDATE -> HIDDEN.equals(resource) ? updatePreAuth : PreAuth.UNKNOWN;
                    default -> PreAuth.UNKNOWN;
                    };
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
                    if (level == PermissionLevel.UPDATE) {
                        updateCalls.add(uri + " " + model + " " + provider + "/" + service + "/" + resource);
                        return !HIDDEN.equals(resource);
                    }
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
