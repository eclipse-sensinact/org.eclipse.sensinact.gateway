/*
 * Copyright 2025 Kentyou
 * Proprietary and confidential
 *
 * All Rights Reserved.
 * Unauthorized copying of this file is strictly prohibited
 */

package org.eclipse.sensinact.northbound.security.authorization.casbin.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.eclipse.sensinact.core.annotation.dto.NullAction;
import org.eclipse.sensinact.core.authorization.NotPermittedException;
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.northbound.security.api.Authenticator;
import org.eclipse.sensinact.northbound.security.api.AuthorizationEngine;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.eclipse.sensinact.northbound.security.authorization.casbin.Constants;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.Property.Type;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

@WithConfiguration(pid = "sensinact.session.manager", properties = @Property(key = "auth.policy", value = "DENY_ALL"))
public class FineAuthorizationTest {

    /**
     * Basic user info
     */
    record FakeUserInfo(String userId, List<String> groups) implements UserInfo {
        @Override
        public String getUserId() {
            return userId;
        }

        @Override
        public Collection<String> getGroups() {
            return groups;
        }

        @Override
        public boolean isAuthenticated() {
            return true;
        }
    }

    /**
     * Authenticator implementation
     */
    class FakeAuthenticator implements Authenticator {
        @Override
        public UserInfo authenticate(String user, String credential) {
            switch (user) {
            case "anonymous":
                return UserInfo.ANONYMOUS;

            case "admin":
                return new FakeUserInfo("admin", List.of("admin"));

            case "foo":
                return new FakeUserInfo("foo", List.of("user"));

            case "foobar":
                return new FakeUserInfo("foobar", List.of("user"));

            case "externalSensor":
                return new FakeUserInfo("externalSensor", List.of("sensor"));

            default:
                throw new IllegalArgumentException("Unexpected value: " + user);
            }
        }

        @Override
        public String getRealm() {
            return "test";
        }

        @Override
        public Scheme getScheme() {
            return Scheme.TOKEN;
        }
    }

    /**
     * Use the authenticator to have pre-defined users
     */
    private final FakeAuthenticator authenticator = new FakeAuthenticator();

    /**
     * Session manager
     */
    @InjectService
    SensiNactSessionManager sessionManager;

    @BeforeAll
    static void setupProviders(@InjectService GatewayThread thread, @InjectService DataUpdate push) throws Exception {

        thread.execute(new AbstractSensinactCommand<Void>() {
            @Override
            protected Promise<Void> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
                    PromiseFactory promiseFactory) {
                // Remove unexpected providers
                twin.getProviders().stream()
                        .filter(p -> !List.of("sensiNact", "basic", "sensor-related").contains(p.getName()))
                        .forEach(SensinactProvider::delete);
                return promiseFactory.resolved(null);
            }
        }).getValue();

        GenericDto dto = new GenericDto();
        // Basic sensor
        dto.provider = "basic";
        dto.service = "sensor";
        dto.resource = "temperature";
        dto.value = 42;
        dto.type = Integer.class;
        push.pushUpdate(dto).getValue();

        dto.service = "foo";
        dto.resource = "bar";
        dto.value = "hello";
        dto.type = String.class;
        push.pushUpdate(dto).getValue();

        dto.service = "input";
        dto.resource = "comment";
        dto.value = null;
        dto.type = String.class;
        dto.nullAction = NullAction.UPDATE;
        push.pushUpdate(dto).getValue();

        // External sensor
        dto.modelPackageUri = "https://example.org/sensor#";
        dto.model = "temperature-sensor";
        dto.provider = "sensor-related";
        dto.service = "sensor";
        dto.resource = "temperature";
        dto.value = 27;
        dto.type = Integer.class;
        push.pushUpdate(dto).getValue();
    }

    @AfterAll
    static void cleanupProviders(@InjectService GatewayThread thread) throws Exception {
        thread.execute(new AbstractSensinactCommand<Void>() {
            @Override
            protected Promise<Void> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
                    PromiseFactory promiseFactory) {
                List.of("basic", "sensor-related").stream().map(twin::getProvider).filter(Objects::nonNull)
                        .forEach(SensinactProvider::delete);
                return promiseFactory.resolved(null);
            }
        });
    }

    @BeforeEach
    void waitForCleanState(@InjectBundleContext BundleContext ctx) throws Exception {
        final Instant end = Instant.now().plusSeconds(5);

        boolean clear = false;
        do {
            clear = ctx.getServiceReferences(AuthorizationEngine.class, null).size() == 0;
        } while (!clear && Instant.now().isBefore(end));
        assertTrue(clear, "An authorization engine is already there");
    }

    @Test
    @WithConfiguration(pid = Constants.CONFIGURATION_PID, location = "?", properties = {
            @Property(key = "allowByDefault", value = "false"),
            @Property(key = "policies", type = Type.Array, value = { "role:user, *, *, *, *, deny, 1000",
                    "role:user, *, *, *, DESCRIBE|READ, allow, 100",
                    "foobar, *, input, comment, *, allow, 0", "externalSensor, *, *, deny, 10",
                    "externalSensor, *, sensor, *, UPDATE, allow, 0",
                    "role:admin, *, *, *, *, allow, -1", "role:anonymous, *, *, *, *, deny, -1000", }) })
    void testSessionsDenyByDefault(@InjectBundleContext BundleContext ctx) throws Exception {
        final AtomicReference<SensiNactSession> sessionRef = new AtomicReference<>();

        Instant end = Instant.now().plusSeconds(5);
        boolean found = false;
        do {
            found = ctx.getServiceReferences(AuthorizationEngine.class, null).size() == 1;
        } while (!found && Instant.now().isBefore(end));

        assertTrue(found, "Authorization engine not found");

        // Anonymous
        SensiNactSession session = sessionManager.createNewAnonymousSession();
        sessionRef.set(session);
        assertEquals(session, sessionRef.get());

        assertEquals(UserInfo.ANONYMOUS.getUserId(), session.getUserInfo().getUserId());
        assertTrue(session.getUserInfo().isAnonymous());
        assertEquals(List.of(UserInfo.ANONYMOUS_GROUP), session.getUserInfo().getGroups());
        // sensiNact provider is always visible
        assertEquals(Set.of("sensiNact"), listProviders(session));
        assertNotNull(session.getResourceValue("sensiNact", "system", "version", Object.class));
        assertEquals(Set.of("sensiNact"), snapshotProviders(session));

        // ... other accesses are denied
        assertThrows(NotPermittedException.class, () -> sessionRef.get().describeProvider("basic"));
        assertThrows(NotPermittedException.class, () -> sessionRef.get().describeService("basic", "sensor"));
        assertThrows(NotPermittedException.class,
                () -> sessionRef.get().describeResource("basic", "sensor", "temperature"));
        assertThrows(NotPermittedException.class,
                () -> sessionRef.get().describeResourceShort("basic", "sensor", "temperature"));
        assertThrows(NotPermittedException.class,
                () -> sessionRef.get().getResourceValue("basic", "sensor", "temperature", Integer.class));

        // Basic user
        session = sessionManager.createNewSession(authenticator.authenticate("foo", null));
        sessionRef.set(session);
        assertEquals(session, sessionRef.get());

        assertEquals("foo", session.getUserInfo().getUserId());
        assertFalse(session.getUserInfo().isAnonymous());
        assertTrue(session.getUserInfo().isAuthenticated());
        assertEquals(List.of("user"), session.getUserInfo().getGroups());
        assertEquals(Set.of("sensiNact", "basic", "sensor-related"), snapshotProviders(session));
        assertEquals(42, session.getResourceValue("basic", "sensor", "temperature", Integer.class));
        assertEquals("hello", session.getResourceValue("basic", "foo", "bar", String.class));
        assertEquals(27, session.getResourceValue("sensor-related", "sensor", "temperature", Integer.class));

        assertNull(session.getResourceValue("basic", "input", "comment", String.class));
        final String comment = "Hello World!";
        assertThrows(NotPermittedException.class,
                () -> sessionRef.get().setResourceValue("basic", "input", "comment", comment));
        assertNull(session.getResourceValue("basic", "input", "comment", String.class));

        // Defined user
        session = sessionManager.createNewSession(authenticator.authenticate("foobar", null));
        sessionRef.set(session);
        assertEquals(session, sessionRef.get());

        assertEquals("foobar", session.getUserInfo().getUserId());
        assertFalse(session.getUserInfo().isAnonymous());
        assertTrue(session.getUserInfo().isAuthenticated());
        assertEquals(List.of("user"), session.getUserInfo().getGroups());
        assertEquals(Set.of("sensiNact", "basic", "sensor-related"), snapshotProviders(session));
        assertEquals(42, session.getResourceValue("basic", "sensor", "temperature", Integer.class));
        assertEquals("hello", session.getResourceValue("basic", "foo", "bar", String.class));
        assertEquals(27, session.getResourceValue("sensor-related", "sensor", "temperature", Integer.class));

        // TODO: make it read and write a comment
        assertNull(session.getResourceValue("basic", "input", "comment", String.class));
        session.setResourceValue("basic", "input", "comment", comment);
        assertEquals(comment, session.getResourceValue("basic", "input", "comment", String.class));

        // Sensor
        session = sessionManager.createNewSession(authenticator.authenticate("externalSensor", null));
        sessionRef.set(session);
        assertEquals(session, sessionRef.get());

        assertEquals("externalSensor", session.getUserInfo().getUserId());
        assertFalse(session.getUserInfo().isAnonymous());
        assertTrue(session.getUserInfo().isAuthenticated());
        assertEquals(List.of("sensor"), session.getUserInfo().getGroups());
        // ... every authenticated user can see the sensiNact provider
        assertEquals(Set.of("sensiNact"), snapshotProviders(session));
        assertThrows(NotPermittedException.class,
                () -> sessionRef.get().getResourceValue("basic", "sensor", "temperature", Integer.class));
        assertThrows(NotPermittedException.class,
                () -> sessionRef.get().getResourceValue("basic", "foo", "bar", String.class));
        assertThrows(NotPermittedException.class,
                () -> sessionRef.get().getResourceValue("sensor-related", "sensor", "temperature", Integer.class));

        // ... set the value but don't read it
        session.setResourceValue("sensor-related", "sensor", "temperature", 38);
        assertThrows(NotPermittedException.class,
                () -> sessionRef.get().getResourceValue("sensor-related", "sensor", "temperature", Integer.class));

        // Admin
        session = sessionManager.createNewSession(authenticator.authenticate("admin", null));
        sessionRef.set(session);
        assertEquals(session, sessionRef.get());

        assertEquals("admin", session.getUserInfo().getUserId());
        assertFalse(session.getUserInfo().isAnonymous());
        assertTrue(session.getUserInfo().isAuthenticated());
        assertEquals(List.of("admin"), session.getUserInfo().getGroups());
        assertEquals(Set.of("sensiNact", "basic", "sensor-related"), snapshotProviders(session));
        assertEquals(42, session.getResourceValue("basic", "sensor", "temperature", Integer.class));
        assertEquals("hello", session.getResourceValue("basic", "foo", "bar", String.class));
        // ... value must have been updated
        assertEquals(38, session.getResourceValue("sensor-related", "sensor", "temperature", Integer.class));
    }

    Set<String> listProviders(SensiNactSession session) {
        return session.listProviders().stream().map(p -> p.provider).collect(Collectors.toSet());
    }

    Set<String> snapshotProviders(SensiNactSession session) {
        return session.filteredSnapshot(null).stream().map(ProviderSnapshot::getName).collect(Collectors.toSet());
    }
}
