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

import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.prototype.SensiNactSession;
import org.eclipse.sensinact.prototype.SensiNactSessionManager;
import org.eclipse.sensinact.prototype.notification.ResourceDataNotification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.service.cm.Configuration;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.service.ServiceExtension;

@ExtendWith({ ServiceExtension.class, ConfigurationExtension.class })
public class TemperatureSensorTest {

    private static final String USER = "user";

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
    void basicSubscribe(
            @InjectConfiguration(withConfig = @WithConfiguration(pid = "sensinact.virtual.temperature", location = "?")) Configuration config)
            throws Exception {

        BlockingQueue<ResourceDataNotification> queue = new ArrayBlockingQueue<>(32);

        SensiNactSession session = sessionManager.getDefaultSession(USER);
        session.addListener(List.of("*"), (t, e) -> queue.offer(e), null, null, null);

        config.update(new Hashtable<String, Object>(
                Map.of("name", "temp1", "latitude", 1.0d, "longitude", 2.0d, "interval", 1000L)));

        ResourceDataNotification notification = queue.poll(5, TimeUnit.SECONDS);

        assertNotNull(notification);
        assertEquals("temp1", notification.provider);
        assertEquals("admin", notification.service);
        assertEquals("friendlyName", notification.resource);

        notification = queue.poll(5, TimeUnit.SECONDS);

        assertNotNull(notification);
        assertEquals("temp1", notification.provider);
        assertEquals("admin", notification.service);
        assertEquals("location", notification.resource);
        assertNull(notification.oldValue);
        assertInstanceOf(Point.class, notification.newValue);
        Point p = (Point) notification.newValue;
        assertEquals(1.0d, p.coordinates.latitude);
        assertEquals(2.0d, p.coordinates.longitude);

        notification = queue.poll(5, TimeUnit.SECONDS);

        assertNotNull(notification);
        assertEquals("temp1", notification.provider);
        assertEquals("admin", notification.service);
        assertEquals("modelUri", notification.resource);

        notification = queue.poll(100, TimeUnit.MILLISECONDS);
        assertNotNull(notification);
        assertEquals("temp1", notification.provider);
        assertEquals("sensor", notification.service);
        assertEquals("temperature", notification.resource);

//      TODO this should be null, not 0.0
//      assertNull(notification.oldValue);

        Double value = (Double) notification.newValue;
        assertEquals(15.0d, value, 15.0d);

        assertTrue(queue.isEmpty());

        notification = queue.poll(1100, TimeUnit.MILLISECONDS);
        assertNotNull(notification);
        assertEquals("temp1", notification.provider);
        assertEquals("sensor", notification.service);
        assertEquals("temperature", notification.resource);
        assertEquals(value, notification.oldValue);
        value = (Double) notification.newValue;
        assertEquals(15.0d, value, 15.0d);

        assertNull(queue.poll(500, TimeUnit.MILLISECONDS));

        notification = queue.poll(600, TimeUnit.MILLISECONDS);
        assertNotNull(notification);
        assertEquals("temp1", notification.provider);
        assertEquals("sensor", notification.service);
        assertEquals("temperature", notification.resource);
        assertEquals(value, notification.oldValue);
        value = (Double) notification.newValue;
        assertEquals(15.0d, value, 15.0d);

    }
}
