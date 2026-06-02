/*********************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector.ProviderSelection;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector.ResourceSelection;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelectorFilterFactory;
import org.eclipse.sensinact.filters.resource.selector.api.Selection;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection.CheckType;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection.OperationType;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.eclipse.sensinact.northbound.session.SnapshotUpdate;
import org.eclipse.sensinact.northbound.session.impl.SessionManager;
import org.eclipse.sensinact.northbound.session.impl.TestUserInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

@WithConfiguration(pid = SessionManager.CONFIGURATION_PID, properties = @Property(key = "auth.policy", value = "AUTHENTICATED_ONLY"))
public class SessionSubscribeTest {

    private static final UserInfo ANON = new TestUserInfo("<ANON>", false);
    private static final UserInfo BOB = new TestUserInfo("bob", true);
    private static final UserInfo FRED = new TestUserInfo("Fred", true);

    private static final String MODEL_URI = "https://sensinact.eclipse.org/test/model";
    private static final String MODEL = "model";
    private static final String PROVIDER = "provider";
    private static final String PROVIDER_TOPIC = MODEL + "/" + PROVIDER + "/*";
    private static final String SERVICE = "service";
    private static final String RESOURCE = "resource";
    private static final Integer VALUE = 42;
    private static final Integer VALUE_2 = 84;

    @InjectService
    SensiNactSessionManager sessionManager;

    @InjectService
    GatewayThread thread;

    @InjectService
    DataUpdate push;

    @InjectService
    ResourceSelectorFilterFactory filterFactory;

    @AfterEach
    void stop() {
        SensiNactSession session = sessionManager.getDefaultSession(ANON);
        session.activeListeners().keySet().forEach(session::removeListener);
        session = sessionManager.getDefaultSession(BOB);
        session.activeListeners().keySet().forEach(session::removeListener);
    }

    @AfterEach
    void deleteProvider(@InjectService GatewayThread gt) throws Exception {
        gt.execute(new AbstractTwinCommand<Void>() {
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

    /**
     * Show that data updates result in events received by subscribers
     *
     * @throws Exception
     */
    @Test
    void basicSubscribe() throws Exception {

        BlockingQueue<ResourceDataNotification> queue = new ArrayBlockingQueue<>(32);

        SensiNactSession session = sessionManager.getDefaultSession(BOB);
        session.addListener(List.of(PROVIDER_TOPIC), (t, e) -> queue.offer(e), null, null, null);

        assertNull(queue.poll(500, TimeUnit.MILLISECONDS));

        pushDto(VALUE);

        ResourceDataNotification notification = queue.poll(1, TimeUnit.SECONDS);

        assertNotNull(notification);

        assertEquals(PROVIDER, notification.provider());
        assertEquals(ProviderPackage.Literals.PROVIDER__ADMIN.getName(), notification.service());
        assertEquals(ProviderPackage.Literals.ADMIN__DESCRIPTION.getName(), notification.resource());
        assertEquals(null, notification.oldValue());

        notification = queue.poll(1, TimeUnit.SECONDS);

        assertEquals(PROVIDER, notification.provider());
        assertEquals(ProviderPackage.Literals.PROVIDER__ADMIN.getName(), notification.service());
        assertEquals(ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName(), notification.resource());
        assertEquals(null, notification.oldValue());
        assertEquals(PROVIDER, notification.newValue());

        notification = queue.poll(1, TimeUnit.SECONDS);

        assertEquals(PROVIDER, notification.provider());
        assertEquals(ProviderPackage.Literals.PROVIDER__ADMIN.getName(), notification.service());
        assertEquals(ProviderPackage.Literals.ADMIN__MODEL.getName(), notification.resource());
        assertEquals(null, notification.oldValue());

        notification = queue.poll(1, TimeUnit.SECONDS);

        assertEquals(PROVIDER, notification.provider());
        assertEquals(ProviderPackage.Literals.PROVIDER__ADMIN.getName(), notification.service());
        assertEquals(ProviderPackage.Literals.ADMIN__MODEL_PACKAGE_URI.getName(), notification.resource());
        assertEquals(null, notification.oldValue());

        notification = queue.poll(1, TimeUnit.SECONDS);

        assertNotNull(notification);

        assertEquals(PROVIDER, notification.provider());
        assertEquals(SERVICE, notification.service());
        assertEquals(RESOURCE, notification.resource());
        assertEquals(null, notification.oldValue());
        assertEquals(VALUE, notification.newValue());

        assertNull(queue.poll(500, TimeUnit.MILLISECONDS));

        pushDto(VALUE_2);

        notification = queue.poll(1, TimeUnit.SECONDS);

        assertNotNull(notification);

        assertEquals(PROVIDER, notification.provider());
        assertEquals(SERVICE, notification.service());
        assertEquals(RESOURCE, notification.resource());
        assertEquals(VALUE, notification.oldValue());
        assertEquals(VALUE_2, notification.newValue());

        assertNull(queue.poll(500, TimeUnit.MILLISECONDS));

    }

    private void pushDto(Integer value) {
        pushDto(PROVIDER, value);
    }

    private void pushDto(String provider, Integer value) {
        GenericDto dto = new GenericDto();
        dto.modelPackageUri = MODEL_URI;
        dto.model = MODEL;
        dto.provider = provider;
        dto.service = SERVICE;
        dto.resource = RESOURCE;
        dto.value = value;
        dto.type = Integer.class;

        push.pushUpdate(dto);
    }

    /**
     * Show that data updates result in events received by subscribers
     *
     * @throws Exception
     */
    @Test
    void basicSubscribeWithoutPermission() throws Exception {

        BlockingQueue<ResourceDataNotification> queue = new ArrayBlockingQueue<>(32);

        SensiNactSession session = sessionManager.getDefaultSession(ANON);
        session.addListener(List.of(PROVIDER_TOPIC), (t, e) -> queue.offer(e), null, null, null);

        assertNull(queue.poll(500, TimeUnit.MILLISECONDS));

        pushDto(VALUE);

        assertNull(queue.poll(1, TimeUnit.SECONDS));
    }

    /**
     * Show that data updates result in events received by subscribers
     *
     * @throws Exception
     */
    @Test
    void snapshotSubscribe() throws Exception {

        final String beforeProvider = "before";
        final String beforeNoMatch = "beforeNoMatch";

        BlockingQueue<SnapshotUpdate> queue = new ArrayBlockingQueue<>(32);

        ResourceSelection friendlyName = new ResourceSelection(
                new Selection(ProviderPackage.Literals.PROVIDER__ADMIN.getName()),
                new Selection(ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName()), List.of());

        ResourceSelection highValue = new ResourceSelection(new Selection(SERVICE), new Selection(RESOURCE),
                List.of(new ValueSelection(VALUE.toString(), OperationType.GREATER_THAN, false, CheckType.VALUE)));

        ICriterion criterion = filterFactory
                .parseResourceSelector(new ResourceSelector(List.of(new ProviderSelection(new Selection(MODEL_URI),
                        new Selection(MODEL), null, List.of(friendlyName, highValue), List.of())), List.of()));

        pushDto(beforeProvider, VALUE_2);
        pushDto(beforeNoMatch, VALUE);

        SensiNactSession session = sessionManager.getDefaultSession(BOB);
        session.subscribe(criterion, queue::add);

        SnapshotUpdate update = queue.poll(1, TimeUnit.SECONDS);
        assertNotNull(update);

        // Just the initial matching provider
        assertEquals(1, update.arriving().size());
        assertEquals(0, update.modified().size());
        assertEquals(0, update.departing().size());

        assertEquals(Set.of(beforeProvider), update.arriving().keySet());
        assertEquals(VALUE_2,
                update.arriving().get(beforeProvider).getResource(SERVICE, RESOURCE).getValue().getValue());
        assertFalse(
                update.arriving().get(beforeProvider).getResource(ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                        ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName()).getValue().isEmpty());

        // Modified match
        pushDto(beforeProvider, VALUE + 1);

        update = queue.poll(1, TimeUnit.SECONDS);
        assertNotNull(update);

        assertEquals(0, update.arriving().size());
        assertEquals(1, update.modified().size());
        assertEquals(0, update.departing().size());

        assertEquals(Set.of(beforeProvider), update.modified().keySet());
        assertEquals(VALUE + 1,
                update.modified().get(beforeProvider).getResource(SERVICE, RESOURCE).getValue().getValue());
        assertFalse(
                update.modified().get(beforeProvider).getResource(ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                        ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName()).getValue().isEmpty());

        // New match
        pushDto(beforeNoMatch, VALUE_2);

        update = queue.poll(1, TimeUnit.SECONDS);
        assertNotNull(update);

        assertEquals(1, update.arriving().size());
        assertEquals(0, update.modified().size());
        assertEquals(0, update.departing().size());

        assertEquals(Set.of(beforeNoMatch), update.arriving().keySet());
        assertEquals(VALUE_2,
                update.arriving().get(beforeNoMatch).getResource(SERVICE, RESOURCE).getValue().getValue());
        assertFalse(update.arriving().get(beforeNoMatch).getResource(ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName()).getValue().isEmpty());

        thread.execute(new AbstractTwinCommand<Void>() {
            @Override
            protected Promise<Void> call(SensinactDigitalTwin twin, PromiseFactory pf) {
                SensinactProvider sp = twin.getProvider(beforeProvider);
                if (sp != null) {
                    sp.delete();
                }
                return pf.resolved(null);
            }
        });

        update = queue.poll(1, TimeUnit.SECONDS);
        assertNotNull(update);

        assertEquals(0, update.arriving().size());
        assertEquals(0, update.modified().size());
        assertEquals(1, update.departing().size());

        assertEquals(Set.of(beforeProvider), update.departing());

        assertNull(queue.poll(500, TimeUnit.MILLISECONDS));
    }

    /**
     * Show that data updates result in events received by subscribers
     *
     * @throws Exception
     */
    @Test
    void basicSnapshotSubscribeWithoutPermission() throws Exception {

        final String beforeProvider = "before";
        final String beforeNoMatch = "beforeNoMatch";

        BlockingQueue<SnapshotUpdate> queue = new ArrayBlockingQueue<>(32);

        ResourceSelection friendlyName = new ResourceSelection(
                new Selection(ProviderPackage.Literals.PROVIDER__ADMIN.getName()),
                new Selection(ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName()), List.of());

        ResourceSelection highValue = new ResourceSelection(new Selection(SERVICE), new Selection(RESOURCE),
                List.of(new ValueSelection(VALUE.toString(), OperationType.GREATER_THAN, false, CheckType.VALUE)));

        ICriterion criterion = filterFactory
                .parseResourceSelector(new ResourceSelector(List.of(new ProviderSelection(new Selection(MODEL_URI),
                        new Selection(MODEL), null, List.of(friendlyName, highValue), List.of())), List.of()));

        pushDto(beforeProvider, VALUE_2);
        pushDto(beforeNoMatch, VALUE);

        SensiNactSession session = sessionManager.getDefaultSession(ANON);
        session.subscribe(criterion, queue::add);

        SnapshotUpdate update = queue.poll(1, TimeUnit.SECONDS);
        assertNotNull(update);

        // No providers visible at all
        assertEquals(0, update.arriving().size());
        assertEquals(0, update.modified().size());
        assertEquals(0, update.departing().size());

        // New match not visible either
        pushDto(beforeNoMatch, VALUE_2);
        assertNull(queue.poll(1, TimeUnit.SECONDS));
    }

    @Test
    void subscribeSpecialCharactersTopic() throws Exception {

        SensiNactSession session = sessionManager.getDefaultSession(FRED);
        Random random = new Random();

        for (String providerName : List.of("some~provider", "some#other$provider", "πάροχος", "sağlayıcı", "प्रदाता",
                "المزود", "供應商", "постачальник", "éà@()/:-?øþæ€ł🔟")) {
            BlockingQueue<ResourceDataNotification> queue = new ArrayBlockingQueue<>(32);
            String subId = session.addListener(List.of(MODEL + "/" + providerName + "/*"), (t, e) -> queue.offer(e),
                    null, null, null);
            assertNotNull(subId, "No subscription created for provider " + providerName);

            try {
                // Wait for the listener to be registered in the OSGi service registry
                // (registration is asynchronous in doAddListener)
                assertNull(queue.poll(500, TimeUnit.MILLISECONDS),
                        "Unexpected early notification for provider " + providerName);

                int newValue = random.nextInt(32000);
                pushDto(providerName, newValue);

                ResourceDataNotification notification;
                do {
                    notification = queue.poll(10, TimeUnit.SECONDS);
                    assertNotNull(notification, "No notification received for provider " + providerName);
                    assertEquals(providerName, notification.provider());
                } while (!RESOURCE.equals(notification.resource()));
                assertEquals(SERVICE, notification.service());
                assertNull(notification.oldValue(), "Got an old value");
                assertEquals(newValue, notification.newValue());
            } finally {
                session.removeListener(subId);

                thread.execute(new AbstractTwinCommand<Void>() {
                    @Override
                    protected Promise<Void> call(SensinactDigitalTwin twin, PromiseFactory pf) {
                        SensinactProvider sp = twin.getProvider(providerName);
                        if (sp != null) {
                            sp.delete();
                        }
                        return pf.resolved(null);
                    }
                }).getValue();
            }
        }
    }
}
