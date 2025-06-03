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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.eclipse.sensinact.northbound.session.impl.TestUserInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

@WithConfiguration(pid = "sensinact.session.manager", properties = @Property(key = "auth.policy", value = "AUTHENTICATED_ONLY"))
public class SessionSubscribeTest {

    private static final UserInfo ANON = new TestUserInfo("<ANON>", false);
    private static final UserInfo BOB = new TestUserInfo("bob", true);

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
    DataUpdate push;

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
                if(sp != null) {
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

        GenericDto dto = new GenericDto();
        dto.model = MODEL;
        dto.provider = PROVIDER;
        dto.service = SERVICE;
        dto.resource = RESOURCE;
        dto.value = VALUE;
        dto.type = Integer.class;

        push.pushUpdate(dto);

        ResourceDataNotification notification = queue.poll(1, TimeUnit.SECONDS);

        assertNotNull(notification);

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

        dto.value = VALUE_2;

        push.pushUpdate(dto);

        notification = queue.poll(1, TimeUnit.SECONDS);

        assertNotNull(notification);

        assertEquals(PROVIDER, notification.provider());
        assertEquals(SERVICE, notification.service());
        assertEquals(RESOURCE, notification.resource());
        assertEquals(VALUE, notification.oldValue());
        assertEquals(VALUE_2, notification.newValue());

        assertNull(queue.poll(500, TimeUnit.MILLISECONDS));

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

        GenericDto dto = new GenericDto();
        dto.model = MODEL;
        dto.provider = PROVIDER;
        dto.service = SERVICE;
        dto.resource = RESOURCE;
        dto.value = VALUE;
        dto.type = Integer.class;

        push.pushUpdate(dto);

        assertNull(queue.poll(1, TimeUnit.SECONDS));
    }
}
