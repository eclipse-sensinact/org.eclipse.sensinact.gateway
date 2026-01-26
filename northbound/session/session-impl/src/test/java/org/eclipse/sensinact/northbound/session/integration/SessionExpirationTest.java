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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.eclipse.sensinact.northbound.session.impl.SessionManager;
import org.eclipse.sensinact.northbound.session.impl.TestUserInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithConfiguration;

@WithConfiguration(pid = SessionManager.CONFIGURATION_PID, properties = {
        @Property(key = "auth.policy", value = "AUTHENTICATED_ONLY"),
        @Property(key = "expiry", value = "" + SessionExpirationTest.SESSION_EXPIRY_SECONDS),
        @Property(key = "activity.check.interval", value = ""
                + SessionExpirationTest.SESSION_ACTIVITY_INTERVAL_SECONDS),
        @Property(key = "activity.check.extension", value = ""
                + SessionExpirationTest.SESSION_ACTIVITY_EXTENSION_SECONDS),
        @Property(key = "activity.check.threshold", value = "1"),
})
public class SessionExpirationTest {

    public static final int SESSION_EXPIRY_SECONDS = 5;
    public static final int SESSION_ACTIVITY_INTERVAL_SECONDS = 1;
    public static final int SESSION_ACTIVITY_EXTENSION_SECONDS = 2;

    private static final UserInfo BOB = new TestUserInfo("bob", true);

    @InjectService
    SensiNactSessionManager sessionManager;

    @AfterEach
    void stop() {
        SensiNactSession session = sessionManager.getDefaultSession(BOB);
        session.activeListeners().keySet().forEach(session::removeListener);
    }

    @Test
    void testExplicitExpirationNotification() throws Exception {

        // Create another session to ensure different sessions are created
        final BlockingQueue<SensiNactSession> expirationNotification = new ArrayBlockingQueue<>(1);
        SensiNactSession session = sessionManager.createNewSession(BOB);
        session.addExpirationListener(s -> expirationNotification.add(s));

        // Explicitly expire the second session
        session.expire();
        SensiNactSession expiredSession = expirationNotification.poll(500, TimeUnit.MILLISECONDS);
        assertNotNull(expiredSession, "Session expiration listener was not notified");
        assertEquals(session, expiredSession, "Notified session does not match the expired session");

        // Ensure no further notifications are sent
        session.expire();
        expiredSession = expirationNotification.poll(500, TimeUnit.MILLISECONDS);
        assertNull(expiredSession, "Session expiration listener was notified multiple times");

        // Ensure the session manager forgot about this session
        assertNull(sessionManager.getSession(BOB, session.getSessionId()));
    }

    @Test
    void testAutomaticExpirationNotification() throws Exception {

        // Create a session that will expire automatically
        final BlockingQueue<SensiNactSession> expirationNotification1 = new ArrayBlockingQueue<>(1);
        SensiNactSession session1 = sessionManager.createNewSession(BOB);
        session1.addExpirationListener(s -> expirationNotification1.add(s));

        // Create another session to ensure different sessions are created
        final BlockingQueue<SensiNactSession> expirationNotification2 = new ArrayBlockingQueue<>(1);
        SensiNactSession session2 = sessionManager.createNewSession(BOB);
        assertNotEquals(session1, session2);
        session2.addExpirationListener(s -> expirationNotification2.add(s));

        // Explicitly expire the second session
        session2.expire();
        SensiNactSession expiredSession = expirationNotification2.poll(500, TimeUnit.MILLISECONDS);
        assertNotNull(expiredSession, "Session expiration listener was not notified");
        assertEquals(session2, expiredSession, "Notified session does not match the expired session");

        // Ensure the session manager forgot about this session
        assertNull(sessionManager.getSession(BOB, session2.getSessionId()));

        // Ensure the other session is still known
        assertEquals(session1, sessionManager.getSession(BOB, session1.getSessionId()));

        // Wait for expiration
        Duration toWait = Duration.between(Instant.now(), session1.getExpiry());
        assertTrue(toWait.toSeconds() < SESSION_EXPIRY_SECONDS, "Session expiry time is too long");
        Thread.sleep(toWait.plusMillis(100).toMillis());

        // Use the session to trigger expiration
        assertThrows(IllegalStateException.class, session1::listProviders);

        // Wait for session to expire automatically
        expiredSession = expirationNotification1.poll(500, TimeUnit.MILLISECONDS);
        assertNotNull(expiredSession, "Session expiration listener was not notified");
        assertEquals(session1, expiredSession, "Notified session does not match the expired session");
        assertNull(sessionManager.getSession(BOB, session1.getSessionId()));
    }

    @Test
    void testExpirationWithActivityChecker() throws Exception {
        final CountDownLatch expirationLatch = new CountDownLatch(1);
        final AtomicReference<Instant> expirationTime = new AtomicReference<>();
        final AtomicBoolean activityCheck = new AtomicBoolean(false);

        SensiNactSession session = sessionManager.createNewSession(BOB, (s, callback) -> {
            callback.accept(activityCheck.get());
        });
        session.addExpirationListener(s -> {
            expirationTime.set(Instant.now());
            expirationLatch.countDown();
        });

        final Instant initialExpiry = session.getExpiry();
        assertNotNull(initialExpiry, "Session expiry is null after creation");

        // First, simulate activity to extend the session
        activityCheck.set(true);

        // Wait for after the expiry time to allow the activity check to occur
        Duration toWait = Duration.between(Instant.now(), initialExpiry);
        assertTrue(toWait.toSeconds() < SESSION_EXPIRY_SECONDS, "Session expiry time is too long");
        Thread.sleep(toWait.plusMillis(200).toMillis());

        // Ensure the session was extended
        assertNull(expirationTime.get(), "Session was expired despite activity");
        final Instant newExpiry = session.getExpiry();
        assertNotNull(newExpiry, "Session expiry is null after activity check");
        assertTrue(newExpiry.isAfter(initialExpiry), "Session expiry was not extended after activity check");
        assertFalse(newExpiry.isBefore(initialExpiry.plusSeconds(2)),
                "Session expiry was not properly extended after activity check");

        // Now, simulate inactivity to allow expiration
        activityCheck.set(false);

        // Wait for session to expire automatically
        final Instant finalExpiry = session.getExpiry();
        assertNotNull(finalExpiry, "Session expiry is null before expiration");
        toWait = Duration.between(Instant.now(), finalExpiry);
        assertTrue(toWait.toSeconds() < SESSION_EXPIRY_SECONDS + SESSION_ACTIVITY_EXTENSION_SECONDS,
                "Session expiry time is too long: " + toWait);
        assertTrue(expirationLatch.await(toWait.plusSeconds(SESSION_ACTIVITY_INTERVAL_SECONDS).toMillis(),
                TimeUnit.MILLISECONDS),
                "Session did not expire as expected; now=" + Instant.now() + ", expiry=" + finalExpiry);
        assertTrue(!expirationTime.get().isBefore(finalExpiry),
                "Expiration notification time is before session expiry");
        assertNull(session.getExpiry(), "Session expiry is not null after expiration");
        assertNull(sessionManager.getSession(BOB, session.getSessionId()),
                "Session manager still knows about the expired session");
    }
}
