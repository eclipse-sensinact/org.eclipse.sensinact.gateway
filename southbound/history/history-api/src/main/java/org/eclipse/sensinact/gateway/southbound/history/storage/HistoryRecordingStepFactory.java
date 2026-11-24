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
*   Kentyou - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.history.storage;

import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryProvider;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * Whiteboard service creating the {@link HistoryRecordingStep} of a history
 * backend, in place of the built-in step that writes each record at once.
 *
 * For each backend, the engine uses the factory with the highest
 * {@code service.ranking} that serves the backend's provider name and gives a
 * step, and the built-in step when there is none. The factories are checked
 * again when one appears, disappears or changes its properties; when the
 * selected factory changes, the step of the backend is replaced and the
 * previous one is closed.
 */
@ConsumerType
public interface HistoryRecordingStepFactory {

    /**
     * Service property (String, String[] or Collection of String, optional):
     * the history provider names this factory serves; absent means all.
     */
    String PROP_NAME = HistoryProvider.PROP_NAME;

    /**
     * Creates the step of a backend, which writes to the storage of the
     * context. A {@code null} result declines the backend and lets the next
     * factory, or the built-in step, serve it; a {@link RuntimeException} is
     * logged and does the same. A factory is asked again on the next change of
     * the factories.
     *
     * Called while the engine holds its lock: it must return quickly, must not
     * wait for another service, must not register or unregister services and
     * must not call the history.
     *
     * When a backend is replaced by a newer registration with the same
     * provider name, the new step is created before the previous one is
     * closed: a step that keeps a resource per provider name, such as a file,
     * must allow for two steps of the same name for a short time.
     */
    HistoryRecordingStep create(HistoryRecordingContext context);
}
