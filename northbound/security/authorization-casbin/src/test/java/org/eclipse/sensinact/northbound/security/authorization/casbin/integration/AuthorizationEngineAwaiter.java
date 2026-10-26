/*********************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Kentyou - initial implementation
 **********************************************************************/

package org.eclipse.sensinact.northbound.security.authorization.casbin.integration;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.sensinact.core.authorization.NotPermittedException;
import org.eclipse.sensinact.northbound.session.SensiNactSession;

/**
 * Helper for tests that need the session manager to be using a configured Authorization Engine
 */
final class AuthorizationEngineAwaiter {

    private AuthorizationEngineAwaiter() {
    }

    /**
     * Injecting the Authorization Engine service only proves that it is registered. The session manager binds it
     * dynamically, and expires all of its sessions when it does. Wait until a new session is denied access by the
     * probe, which only happens once the engine is bound.
     *
     * @param sessions Creates a new session of the user to probe with
     * @param probe    Performs an access that the engine denies, but that the default policy allows
     */
    static void awaitEngineBound(final Supplier<SensiNactSession> sessions, final Consumer<SensiNactSession> probe)
            throws InterruptedException {
        final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        RuntimeException last = null;
        do {
            final SensiNactSession session = sessions.get();
            try {
                probe.accept(session);
            } catch (NotPermittedException e) {
                return;
            } catch (RuntimeException e) {
                // For example a session expired by the engine being bound: try again with a new one
                last = e;
            } finally {
                session.expire();
            }
            Thread.sleep(50);
        } while (System.nanoTime() < deadline);
        fail("The session manager was not bound to the Authorization Engine", last);
    }
}
