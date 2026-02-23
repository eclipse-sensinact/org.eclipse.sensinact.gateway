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

import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * Session activity checker, called by the SensiNactSessionManager before
 * session expiration to check whether the session is still active and should be
 * extended or not
 */
public interface SensiNactSessionActivityChecker {

    /**
     * Checks <strong>asynchronously</strong> the activity of the given session.
     *
     * @param pf             PromiseFactory to create Promises
     * @param session        Session to check
     * @return A promise with the activity status: true if the session is active,
     *         false otherwise
     */
    Promise<Boolean> checkActivity(PromiseFactory pf, SensiNactSession session);
}
