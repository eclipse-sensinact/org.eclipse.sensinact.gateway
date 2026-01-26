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

import java.util.function.Consumer;

/**
 * Session activity checker, called by the SensiNactSessionManager before
 * session expiration to check whether the session is still active and should be
 * extended or not
 */
public interface SensiNactSessionActivityChecker {

    /**
     * Checks <strong>asynchronously</strong> the activity of the given session.
     *
     * @param session        Session to check
     * @param resultCallback Method to call with the result of the activity check
     */
    void checkActivity(SensiNactSession session, Consumer<Boolean> resultCallback);
}
