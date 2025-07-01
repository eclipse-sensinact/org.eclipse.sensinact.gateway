/*********************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Kentyou - initial implementation
 **********************************************************************/

package org.eclipse.sensinact.northbound.security.api;

import org.eclipse.sensinact.core.authorization.PermissionLevel;

/**
 * The PreAuthorizer is designed to be called by the Northbound providers before
 * moving on to the Authorizer.
 */
public interface PreAuthorizer {

    public enum PreAuth {
        /** Auth is known and failed - deny access */
        DENY,
        /** Auth is known and successful - permit access */
        ALLOW,
        /** Auth is unknown - use a full auth method **/
        UNKNOWN
    }

    /**
     * Check pre-authorization for the named provider.
     * <p>
     * Pre-authorization may use the existing cache to avoid needing a full lookup
     * including model name and URI. This can be helpful to avoid touching the
     * gateway thread when you don't know the model name or package URI.
     *
     * @param level
     * @param provider
     * @return
     */
    PreAuth preAuthProvider(PermissionLevel level, String provider);

    /**
     * Check pre-authorization for the named service.
     * <p>
     * Pre-authorization may use the existing cache to avoid needing a full lookup
     * including model name and URI. This can be helpful to avoid touching the
     * gateway thread when you don't know the model name or package URI.
     *
     * @param level
     * @param provider
     * @param service
     * @return
     */

    PreAuth preAuthService(PermissionLevel level, String provider, String service);

    /**
     * Check pre-authorization for the named resource.
     * <p>
     * Pre-authorization may use the existing cache to avoid needing a full lookup
     * including model name and uri. This can be helpful to avoid touching the
     * gateway thread when you don't know the model name or package URI.
     *
     * @param level
     * @param provider
     * @return
     */
    PreAuth preAuthResource(PermissionLevel level, String provider, String service, String resource);
}
