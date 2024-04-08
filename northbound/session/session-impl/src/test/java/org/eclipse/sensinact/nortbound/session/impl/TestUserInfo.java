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