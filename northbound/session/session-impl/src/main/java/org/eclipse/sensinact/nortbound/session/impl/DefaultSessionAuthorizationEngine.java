/*********************************************************************
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.nortbound.session.impl;

import org.eclipse.sensinact.northbound.security.api.AuthorizationEngine;
import org.eclipse.sensinact.northbound.security.api.UserInfo;

public class DefaultSessionAuthorizationEngine implements AuthorizationEngine {

    private final DefaultAuthPolicy policy;

    public DefaultSessionAuthorizationEngine(DefaultAuthPolicy policy) {
        super();
        this.policy = policy;
    }

    @Override
    public Authorizer createAuthorizer(UserInfo user) {
        switch (policy) {
            case ALLOW_ALL:
                return new AllowAllAuthorizer();
            case AUTHENTICATED_ONLY:
                if(user.isAuthenticated()) {
                    return new AllowAllAuthorizer();
                }
            case DENY_ALL:
            default:
                return new DenyAllAuthorizer();
        }
    }

}
