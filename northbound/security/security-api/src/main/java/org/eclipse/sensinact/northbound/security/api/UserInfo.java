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

import java.util.Collection;
import java.util.List;

/**
 * Provides informations about a user
 */
public interface UserInfo {

    /**
     * Unique anonymous user
     */
    public static final UserInfo ANONYMOUS = new AnonymousUser();

    /**
     * Name of the anonymous group
     */
    public static final String ANONYMOUS_GROUP = "anonymous";

    /**
     * Returns the user ID
     *
     * @return the user ID
     */
    public String getUserId();

    /**
     * Returns the list of groups the user is a member of
     */
    public Collection<String> getGroups();

    /**
     * Checks if the user is a member of the given group.
     *
     * Implementations should try to cache this information
     *
     * @param group Group name
     * @return True if the user is a member of the given group
     */
    default public boolean isMemberOfGroup(String group) {
        final Collection<String> groups = getGroups();
        return groups != null && groups.contains(group);
    }

    /**
     * Checks if the user is a member of all of the given groups. This method allows
     * to avoid calling {@link #isMemberOfGroup(String)} for each individual group
     * when it becomes expensive due to the underlying authorization/authentication
     * provider.
     *
     * This method returns false if the given collection is empty.
     *
     * @param groups Collection of group names
     * @return True if the user is a member of all of the given groups
     */
    default public boolean isMemberOfAllGroups(Collection<String> groups) {
        return !groups.isEmpty() && groups.stream().allMatch(this::isMemberOfGroup);
    }

    /**
     * Checks if the user is a member of any of the given groups. This method allows
     * to avoid calling {@link #isMemberOfGroup(String)} for each individual group
     * when it becomes expensive due to the underlying authorization/authentication
     * provider.
     *
     * This method returns false if the given collection is empty.
     *
     * @param groups Collection of group names
     * @return True if the user is a member of one of the given groups
     */
    default public boolean isMemberOfAnyGroup(Collection<String> groups) {
        return !groups.isEmpty() && groups.stream().anyMatch(this::isMemberOfGroup);
    }

    /**
     * Indicates if the user is anonymous, i.e. not authenticated
     *
     * @return true if the user is anonymous
     */
    default boolean isAnonymous() {
        return !isAuthenticated();
    }

    /**
     * Indicates if the user is authenticated, i.e. not anonymous
     *
     * @return true if the user is authenticated
     */
    boolean isAuthenticated();
}

class AnonymousUser implements UserInfo {

    @Override
    public String getUserId() {
        return "<ANONYMOUS>";
    }

    @Override
    public Collection<String> getGroups() {
        return List.of(ANONYMOUS_GROUP);
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
