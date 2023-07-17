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
package org.eclipse.sensinact.gateway.northbound.security.oidc;

import java.util.Set;
import java.util.function.Function;

import org.eclipse.sensinact.core.security.UserInfo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

public class JwsUserInfo implements UserInfo {

    private final String subject;

    private final Set<String> groups;

    public JwsUserInfo(Jws<Claims> jws, Function<Jws<Claims>, Set<String>> groupMapper) {
        this.subject = jws.getBody().getSubject();
        this.groups = groupMapper.apply(jws);
    }

    @Override
    public String getUserId() {
        return subject;
    }

    @Override
    public boolean isMemberOfGroup(String group) {
        return groups.contains(group);
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

}
