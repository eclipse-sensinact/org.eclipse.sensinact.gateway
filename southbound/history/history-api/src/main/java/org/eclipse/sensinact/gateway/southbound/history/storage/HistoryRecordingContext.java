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

import org.osgi.annotation.versioning.ProviderType;

/**
 * What the engine gives a {@link HistoryRecordingStepFactory} to create the
 * step of a history backend. Implemented by the engine only, so that it can
 * gain methods in a minor version.
 */
@ProviderType
public interface HistoryRecordingContext {

    /** The name of the history provider of the backend. */
    String providerName();

    /** The storage of the backend, which the step writes to. */
    HistoryStorage storage();
}
