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
package org.eclipse.sensinact.core.session;

import java.util.List;

import org.eclipse.sensinact.core.security.UserInfo;

public interface SensiNactSessionManager {

    SensiNactSession getDefaultSession(UserInfo user);

    SensiNactSession getSession(UserInfo user, String sessionId);

    List<String> getSessionIds(UserInfo user);

    SensiNactSession createNewSession(UserInfo user);

    SensiNactSession getDefaultAnonymousSession();

    SensiNactSession getAnonymousSession(String sessionId);

    List<String> getAnonymousSessionIds();

    SensiNactSession createNewAnonymousSession();

}
