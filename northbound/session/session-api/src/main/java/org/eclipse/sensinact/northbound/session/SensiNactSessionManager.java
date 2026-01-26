/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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

import java.util.List;

import org.eclipse.sensinact.northbound.security.api.UserInfo;

/**
 * SensiNact Session Manager
 */
public interface SensiNactSessionManager {

    /**
     * Gets or creates the default session for the given user
     *
     * <ul>
     * <li>The default session is the same for each subsequent call with the same
     * user.</li>
     * <li>The default session is <strong>not</strong> one of those created by
     * createNewSession().</li>
     * </ul>
     *
     * @param user Session user
     * @return The default session for the given user
     */
    SensiNactSession getDefaultSession(UserInfo user);

    /**
     * Returns a specific session for the given user
     *
     * @param user      Session user
     * @param sessionId Session ID
     * @return The specific session, null if not found
     */
    SensiNactSession getSession(UserInfo user, String sessionId);

    /**
     * Lists all session IDs associated to the given user
     *
     * @param user Session user
     * @return List of session IDs
     */
    List<String> getSessionIds(UserInfo user);

    /**
     * Creates a new session for the given user and associates the given activity
     * checker to it
     *
     * @param user            Session user
     * @param activityChecker Session activity checker
     * @return A new session for the given user
     */
    SensiNactSession createNewSession(UserInfo user, SensiNactSessionActivityChecker activityChecker);

    /**
     * Creates a new session for the given user, without activity checker
     *
     * @param user Session user
     * @return A new session for the given user
     */
    default SensiNactSession createNewSession(UserInfo user) {
        return this.createNewSession(user, null);
    }

    /**
     * Gets or creates the default anonymous session
     *
     * <ul>
     * <li>The default anonymous session is the same for each subsequent call. This
     * means multiple anonymous clients will share the same default session.</li>
     * <li>The default session is <strong>not</strong> one of those created by
     * createNewSession().</li>
     * </ul>
     *
     * @return The default anonymous session
     */
    SensiNactSession getDefaultAnonymousSession();

    /**
     * Gets a specific anonymous session
     *
     * @param sessionId Anonymous session ID
     * @return The specific anonymous session, null if not found
     */
    SensiNactSession getAnonymousSession(String sessionId);

    /**
     * {@return the list all anonymous session IDs}
     */
    List<String> getAnonymousSessionIds();

    /**
     * Creates a new anonymous session with the given activity checker
     *
     * @param activityChecker Session activity checker
     * @return A new anonymous session
     */
    SensiNactSession createNewAnonymousSession(SensiNactSessionActivityChecker activityChecker);

    /**
     * Creates a new anonymous session, without activity checker
     *
     * @return A new anonymous session
     */
    default SensiNactSession createNewAnonymousSession() {
        return this.createNewAnonymousSession(null);
    }
}
