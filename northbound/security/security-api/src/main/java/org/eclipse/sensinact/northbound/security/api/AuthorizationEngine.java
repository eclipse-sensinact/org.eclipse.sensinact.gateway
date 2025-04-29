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

/**
 * The {@link AuthorizationEngine} is responsible for checking whether a given
 * user has access to the defined resources
 */
public interface AuthorizationEngine {

    /**
     * Create a {@link PreAuthorizer} for the supplied user.
     *
     * @param user User description
     * @return A pre-authorizer for the given user
     */
    PreAuthorizer createAuthorizer(UserInfo user);

}
