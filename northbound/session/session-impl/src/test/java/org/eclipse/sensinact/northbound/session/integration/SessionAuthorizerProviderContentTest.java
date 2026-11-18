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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.eclipse.sensinact.core.authorization.Authorizer;
import org.eclipse.sensinact.core.authorization.PermissionLevel;
import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactService;
import org.eclipse.sensinact.northbound.security.api.AuthorizationEngine;
import org.eclipse.sensinact.northbound.security.api.PreAuthorizer;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
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
 * An authorizer that wants to know what a provider or a service contains to decide DESCRIBE (to allow it when something
 * inside is allowed) must be given the twin objects, which have the services and resources: the names do not, and a
 * snapshot has no service yet when it is tested. The session gives the twin objects it holds.
 */
@WithConfiguration(pid = "sensinact.session.manager", properties = {
        @Property(key = "auth.policy", value = "ALLOW_ALL"),
        @Property(key = "name", value = "SessionAuthorizerProviderContentTest"),
        @Property(key = "authorization.target", value = "(scope=SessionAuthorizerProviderContentTest)")
})
public class SessionAuthorizerProviderContentTest {

    private static final String PROVIDER = "TwinObjectsProvider";

    private static final UserInfo BOB = new TestUserInfo("bob", true);

    /** What the authorizer was given: the calls with an object, with what it could read in it, and the calls by name */
    private static final class Recorder {
        final List<String> objects = new CopyOnWriteArrayList<>();
        final List<String> names = new CopyOnWriteArrayList<>();

        void clear() {
            objects.clear();
            names.clear();
        }
    }

    private final Recorder recorder = new Recorder();

    private ServiceRegistration<AuthorizationEngine> registration;

    @InjectBundleContext
    BundleContext context;

    @InjectService(filter = "(name=SessionAuthorizerProviderContentTest)", timeout = 5000)
    SensiNactSessionManager sessionManager;

    @BeforeEach
    void setup(@InjectService DataUpdate push, @InjectService GatewayThread thread) throws Exception {
        // A provider with two services, the first with two resources and the second with one
        for (String[] path : new String[][] { { "svc1", "r1" }, { "svc1", "r2" }, { "svc2", "r3" } }) {
            GenericDto dto = new GenericDto();
            dto.provider = PROVIDER;
            dto.service = path[0];
            dto.resource = path[1];
            dto.value = 1;
            dto.type = Integer.class;
            dto.timestamp = Instant.now();
            push.pushUpdate(dto).getValue();
        }
        Hashtable<String, Object> properties = new Hashtable<>();
        properties.put("scope", "SessionAuthorizerProviderContentTest");
        registration = context.registerService(AuthorizationEngine.class, new RecordingEngine(recorder), properties);
        awaitEngineBound();
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

    /** The session manager binds the engine dynamically and expires its sessions: wait until a new one uses it */
    private void awaitEngineBound() throws InterruptedException {
        final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            try {
                sessionManager.getDefaultSession(BOB).listProviders();
            } catch (RuntimeException e) {
                // A session expired by the binding: try again with a new one
            }
            if (!recorder.objects.isEmpty() || !recorder.names.isEmpty()) {
                recorder.clear();
                return;
            }
            Thread.sleep(50);
        }
        throw new AssertionError("The engine was never used");
    }

    private SensiNactSession session() {
        return sessionManager.getDefaultSession(BOB);
    }

    private static boolean has(List<String> calls, String prefix) {
        return calls.stream().anyMatch(c -> c.startsWith(prefix));
    }

    @Test
    void testListProviders() {
        session().listProviders();
        // The provider twin: its services and, in them, its resources
        assertTrue(has(recorder.objects, "provider DESCRIBE " + PROVIDER + " [admin, svc1, svc2]"),
                recorder.objects.toString());
        assertTrue(recorder.objects.stream().anyMatch(c -> c.contains(PROVIDER) && c.contains("svc1=[r1, r2]")
                && c.contains("svc2=[r3]")), recorder.objects.toString());
    }

    @Test
    void testDescribeAndSnapshotOfAProvider() {
        session().providerSnapshot(PROVIDER, EnumSet.noneOf(SensinactDigitalTwin.SnapshotOption.class));
        assertTrue(has(recorder.objects, "provider DESCRIBE " + PROVIDER + " [admin, svc1, svc2]"),
                recorder.objects.toString());
        // The services of its snapshot are tested with their twin, which has the resources
        assertTrue(recorder.objects.contains("service DESCRIBE " + PROVIDER + "/svc1 [r1, r2]"),
                recorder.objects.toString());
        assertTrue(recorder.objects.contains("service DESCRIBE " + PROVIDER + "/svc2 [r3]"),
                recorder.objects.toString());
    }

    @Test
    void testFilteredSnapshot() {
        session().filteredSnapshot(null);
        // The snapshots are tested before they have any service: the twin gives them
        assertTrue(has(recorder.objects, "provider DESCRIBE " + PROVIDER + " [admin, svc1, svc2]"),
                recorder.objects.toString());
        assertTrue(recorder.objects.contains("service DESCRIBE " + PROVIDER + "/svc1 [r1, r2]"),
                recorder.objects.toString());
    }

    @Test
    void testDescribeAndSnapshotOfAService() {
        session().describeService(PROVIDER, "svc1");
        assertEquals(List.of("service DESCRIBE " + PROVIDER + "/svc1 [r1, r2]"),
                recorder.objects.stream().filter(c -> c.contains(PROVIDER + "/svc1")).toList());
        recorder.clear();
        session().serviceSnapshot(PROVIDER, "svc2");
        assertTrue(recorder.objects.contains("service DESCRIBE " + PROVIDER + "/svc2 [r3]"),
                recorder.objects.toString());
    }

    @Test
    void testAServiceThatDoesNotExistIsTestedByName() {
        session().describeService(PROVIDER, "nothing");
        // No twin object to give: the names, as before
        assertTrue(recorder.names.contains("service DESCRIBE " + PROVIDER + "/nothing"), recorder.names.toString());
    }

    // The engine under test

    private static final class RecordingEngine implements AuthorizationEngine {

        private final Recorder recorder;

        RecordingEngine(Recorder recorder) {
            this.recorder = recorder;
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
                    return PreAuth.UNKNOWN;
                }
            };
        }

        @Override
        public Authorizer createAuthorizer(UserInfo user) {
            return new Authorizer() {
                @Override
                public boolean hasProviderPermission(PermissionLevel level, String uri, String model, String provider) {
                    recorder.names.add("provider " + level + " " + provider);
                    return true;
                }

                @Override
                public boolean hasProviderPermission(PermissionLevel level, SensinactProvider provider) {
                    // Read now: a twin object is valid during its command only
                    Map<String, List<String>> below = new TreeMap<>();
                    provider.getServices().forEach((name, service) -> below.put(name,
                            new TreeSet<>(service.getResources().keySet()).stream().toList()));
                    recorder.objects.add("provider " + level + " " + provider.getName() + " "
                            + new TreeSet<>(provider.getServices().keySet()) + " " + below);
                    return true;
                }

                @Override
                public boolean hasServicePermission(PermissionLevel level, String uri, String model, String provider,
                        String service) {
                    recorder.names.add("service " + level + " " + provider + "/" + service);
                    return true;
                }

                @Override
                public boolean hasServicePermission(PermissionLevel level, SensinactService service) {
                    recorder.objects.add("service " + level + " " + service.getProvider().getName() + "/"
                            + service.getName() + " " + new TreeSet<>(service.getResources().keySet()));
                    return true;
                }

                @Override
                public boolean hasResourcePermission(PermissionLevel level, String uri, String model, String provider,
                        String service, String resource) {
                    return true;
                }
            };
        }
    }
}
