/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.endpoint.test;

import org.eclipse.sensinact.gateway.core.security.AccessLevelOption;
import org.eclipse.sensinact.gateway.core.security.AuthorizationService;
import org.eclipse.sensinact.gateway.core.security.AuthorizationServiceException;

class MyAuthorization implements AuthorizationService {
    /**
     * @inheritDoc
     * @see AuthorizationService#getAuthenticatedAccessLevelOption(java.lang.String, long)
     */
    @Override
    public AccessLevelOption getAuthenticatedAccessLevelOption(String path, long uid) throws AuthorizationServiceException {
        return AccessLevelOption.ANONYMOUS;
    }

    /**
     * @inheritDoc
     * @see AuthorizationService#getAuthenticatedAccessLevelOption(java.lang.String, java.lang.String)
     */
    @Override
    public AccessLevelOption getAuthenticatedAccessLevelOption(String path, String publicKey) throws AuthorizationServiceException {
        return AccessLevelOption.ANONYMOUS;
    }
}