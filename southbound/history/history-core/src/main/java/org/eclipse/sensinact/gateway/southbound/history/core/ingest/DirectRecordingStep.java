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
package org.eclipse.sensinact.gateway.southbound.history.core.ingest;

import java.util.List;
import java.util.Objects;

import org.eclipse.sensinact.gateway.southbound.history.storage.HistoricalRecord;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryRecordingStep;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryStorage;

/**
 * Built-in recording step: writes each record to the storage at once, so that
 * a failure of the storage reaches the engine.
 */
public class DirectRecordingStep implements HistoryRecordingStep {

    private final HistoryStorage storage;

    public DirectRecordingStep(HistoryStorage storage) {
        this.storage = Objects.requireNonNull(storage, "storage must not be null");
    }

    @Override
    public void record(HistoricalRecord record) {
        storage.store(List.of(record));
    }
}
