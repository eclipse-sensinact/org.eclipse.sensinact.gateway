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

import org.eclipse.sensinact.northbound.security.api.UserInfo;

public class TestUserInfo implements UserInfo {

    private final String user;
    private final boolean authenticated;

    public TestUserInfo(String user, boolean authenticated) {
        this.user = user;
        this.authenticated = authenticated;
    }

    @Override
    public String getUserId() {
        return user;
    }

    @Override
    public boolean isMemberOfGroup(String group) {
        return false;
    }

    @Override
    public boolean isAnonymous() {
        return !authenticated;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public String toString() {
        return "TestUserInfo [user=" + user + ", authenticated=" + authenticated + "]";
    }
}
