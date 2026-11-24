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

import org.osgi.annotation.versioning.ConsumerType;

/**
 * The step of a history backend that records a record accepted by the ingest
 * filters. The built-in step writes it at once with
 * {@link HistoryStorage#store(java.util.List)}; a
 * {@link HistoryRecordingStepFactory} can provide another one, for example to
 * write in groups or to keep records while the storage is unavailable.
 *
 * The engine keeps every filtering decision, so a step only sees the records
 * that are to be recorded.
 */
@ConsumerType
public interface HistoryRecordingStep extends AutoCloseable {

    /**
     * Records one record. Called on the event handler thread, in the order of
     * delivery: it must return quickly, must not register services or call the
     * history, and a step that writes later must keep the order of the records
     * of one resource. It must never block: a step whose buffer is full drops
     * the record and counts it rather than wait, since a blocked call also
     * delays the replacement of the step.
     *
     * The engine considers the record recorded when this method returns
     * normally, and uses it as the last stored value of its resource for the
     * change conditions of the filters; a later failure of a deferred write is
     * not seen by the engine. A {@link RuntimeException} is logged by the
     * engine and leaves the last stored value unchanged.
     */
    void record(HistoricalRecord record);

    /**
     * Called when the backend stops or when the step is replaced; a step that
     * keeps records writes them if it can. The engine waits for a record being
     * handled before it closes the step, so {@link #record(HistoricalRecord)}
     * is never called during or after this method. The records that follow go
     * to the new step at once, so they may be written before the records this
     * step still holds. Called on the thread of a service event or of the
     * deactivation of the engine, outside the lock of the engine and before
     * the storage is released: it must not block for long.
     */
    @Override
    default void close() {
    }
}
