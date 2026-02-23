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

package org.eclipse.sensinact.northbound.session;

/**
 * Listener interface for session expiration events
 */
public interface SensiNactSessionExpirationListener {

    /**
     * Callback method invoked when a session expires
     *
     * @param session The expired SensiNact session
     */
    void sessionExpired(SensiNactSession session);
}
