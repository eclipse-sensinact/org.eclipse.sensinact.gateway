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
package org.eclipse.sensinact.gateway.southbound.history.core.osgi;

import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryRecordingContext;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryStorage;

/** The context given to a recording step factory for one backend. */
record RecordingContext(String providerName, HistoryStorage storage) implements HistoryRecordingContext {
}
