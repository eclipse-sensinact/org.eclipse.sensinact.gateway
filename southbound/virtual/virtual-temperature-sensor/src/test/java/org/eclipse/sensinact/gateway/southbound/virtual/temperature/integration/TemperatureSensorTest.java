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
package org.eclipse.sensinact.gateway.southbound.virtual.temperature.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.osgi.annotation.bundle.Requirement;
import org.osgi.namespace.service.ServiceNamespace;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithConfiguration;

@WithConfiguration(pid = "sensinact.session.manager", properties = @Property(key = "auth.policy", value = "ALLOW_ALL"))
@Requirement(namespace = ServiceNamespace.SERVICE_NAMESPACE, filter = "(objectClass=org.eclipse.sensinact.northbound.session.SensiNactSessionManager)")
public class TemperatureSensorTest {

    private static final UserInfo USER = UserInfo.ANONYMOUS;

    @InjectService
    SensiNactSessionManager sessionManager;

    @AfterEach
    void stop() {
        SensiNactSession session = sessionManager.getDefaultSession(USER);
        session.activeListeners().keySet().forEach(session::removeListener);
    }

    /**
     * Show that data updates result in events received by subscribers
     *
     * @throws Exception
     */
    @Test
    void basicSubscribe(@InjectService ConfigurationAdmin configAdmin) throws Exception {

        BlockingQueue<ResourceDataNotification> queue = new ArrayBlockingQueue<>(32);

        SensiNactSession session = sessionManager.getDefaultSession(USER);
        session.addListener(List.of("*"), (t, e) -> queue.offer(e), null, null, null);

        Configuration config = configAdmin.createFactoryConfiguration("sensinact.virtual.temperature", "?");
        config.update(new Hashtable<String, Object>(
                Map.of("name", "temp1", "latitude", 1.0d, "longitude", 2.0d, "interval", 1000L)));

        ResourceDataNotification notification = queue.poll(5, TimeUnit.SECONDS);
        assertEquals("temp1", notification.provider());
        assertEquals(ProviderPackage.Literals.PROVIDER__ADMIN.getName(), notification.service());
        assertEquals(ProviderPackage.Literals.ADMIN__DESCRIPTION.getName(), notification.resource());
        assertEquals(null, notification.oldValue());

        notification = queue.poll(1, TimeUnit.SECONDS);

        assertNotNull(notification);
        assertEquals("temp1", notification.provider());
        assertEquals(ProviderPackage.Literals.PROVIDER__ADMIN.getName(), notification.service());
        assertEquals(ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName(), notification.resource());

        notification = queue.poll(5, TimeUnit.SECONDS);

        assertNotNull(notification);
        assertEquals("temp1", notification.provider());
        assertEquals(ProviderPackage.Literals.PROVIDER__ADMIN.getName(), notification.service());
        assertEquals(ProviderPackage.Literals.ADMIN__LOCATION.getName(), notification.resource());
        assertNull(notification.oldValue());
        assertInstanceOf(Point.class, notification.newValue());
        Point p = (Point) notification.newValue();
        assertEquals(1.0d, p.coordinates().latitude());
        assertEquals(2.0d, p.coordinates().longitude());

        notification = queue.poll(5, TimeUnit.SECONDS);

        assertNotNull(notification);
        assertEquals("temp1", notification.provider());
        assertEquals(ProviderPackage.Literals.PROVIDER__ADMIN.getName(), notification.service());
        assertEquals(ProviderPackage.Literals.ADMIN__MODEL.getName(), notification.resource());

        notification = queue.poll(5, TimeUnit.SECONDS);

        assertNotNull(notification);
        assertEquals("temp1", notification.provider());
        assertEquals(ProviderPackage.Literals.PROVIDER__ADMIN.getName(), notification.service());
        assertEquals(ProviderPackage.Literals.ADMIN__MODEL_PACKAGE_URI.getName(), notification.resource());

        notification = queue.poll(100, TimeUnit.MILLISECONDS);
        assertNotNull(notification);
        assertEquals("temp1", notification.provider());
        assertEquals("sensor", notification.service());
        assertEquals("temperature", notification.resource());

//      TODO this should be null, not 0.0
//      assertNull(notification.oldValue);

        Double value = (Double) notification.newValue();
        assertEquals(15.0d, value, 15.0d);

        assertTrue(queue.isEmpty());

        notification = queue.poll(1100, TimeUnit.MILLISECONDS);
        assertNotNull(notification);
        assertEquals("temp1", notification.provider());
        assertEquals("sensor", notification.service());
        assertEquals("temperature", notification.resource());
        assertEquals(value, notification.oldValue());
        value = (Double) notification.newValue();
        assertEquals(15.0d, value, 15.0d);

        assertNull(queue.poll(500, TimeUnit.MILLISECONDS));

        notification = queue.poll(600, TimeUnit.MILLISECONDS);
        assertNotNull(notification);
        assertEquals("temp1", notification.provider());
        assertEquals("sensor", notification.service());
        assertEquals("temperature", notification.resource());
        assertEquals(value, notification.oldValue());
        value = (Double) notification.newValue();
        assertEquals(15.0d, value, 15.0d);

    }
}
