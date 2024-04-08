/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.northbound.security.api;

public interface UserInfo {

    public static final UserInfo ANONYMOUS = new AnonymousUser();

    public String getUserId();

    public boolean isMemberOfGroup(String group);

    boolean isAnonymous();

    boolean isAuthenticated();

}

class AnonymousUser implements UserInfo {

    @Override
    public String getUserId() {
        return "<ANONYMOUS>";
    }

    @Override
    public boolean isMemberOfGroup(String group) {
        return false;
    }

    @Override
    public boolean isAnonymous() {
        return true;
    }

    @Override
    public boolean isAuthenticated() {
        return false;
    }

}
