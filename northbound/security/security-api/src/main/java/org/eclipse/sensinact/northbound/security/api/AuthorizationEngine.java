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

/**
 * The {@link AuthorizationEngine} is responsible for checking whether a
 * given user has access to the defined resources
 */
public interface AuthorizationEngine {

    /**
     * Create an {@link Authorizer} for the supplied user
     * @param user
     * @return
     */
    Authorizer createAuthorizer(UserInfo user);

    /**
     * The Authorizer is designed to be cacheable and Thread safe.
     */
    public interface Authorizer {

        public enum PreAuth {
            /** Auth is known and failed - deny access */
            DENY,
            /** Auth is known and successful - permit access */
            ALLOW,
            /** Auth is unknown - use a full auth method **/
            UNKNOWN }

        /**
         * Check pre-authorization for the named provider.
         * <p>
         * Pre authorisation may use the existing cache to avoid needing a full lookup
         * including model name and uri. This can be helpful to avoid touching the
         * gateway thread when you don't know the model name or package uri.
         *
         * @param level
         * @param provider
         * @return
         */
        PreAuth preAuthProvider(PermissionLevel level, String provider);

        /**
         * Check pre-authorization for the named service.
         * <p>
         * Pre authorisation may use the existing cache to avoid needing a full lookup
         * including model name and uri. This can be helpful to avoid touching the
         * gateway thread when you don't know the model name or package uri.
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
         * Pre authorisation may use the existing cache to avoid needing a full lookup
         * including model name and uri. This can be helpful to avoid touching the
         * gateway thread when you don't know the model name or package uri.
         *
         * @param level
         * @param provider
         * @return
         */
        PreAuth preAuthResource(PermissionLevel level, String provider, String service, String resource);

        /**
         * Test whether this authorizer has the necessary access level for the named provider
         *
         * @param level
         * @param modelPackageUri
         * @param model
         * @param provider
         * @return
         */
        boolean hasProviderPermission(PermissionLevel level, String modelPackageUri, String model, String provider);

        /**
         * Test whether the user has the necessary access level for the named service
         *
         * @param level
         * @param modelPackageUri
         * @param model
         * @param provider
         * @param service
         * @return
         */
        boolean hasServicePermission(PermissionLevel level, String modelPackageUri, String model, String provider, String service);

        /**
         * Test whether the user has the necessary access level for the named resource
         *
         * @param level
         * @param modelPackageUri
         * @param model
         * @param provider
         * @param service
         * @param resource
         * @return
         */
        boolean hasResourcePermission(PermissionLevel level, String modelPackageUri, String model, String provider, String service, String resource);

        /**
         * Restrict a collection of services to those which are visible
         * @param modelPackageUri
         * @param model
         * @param provider
         * @param services
         * @return
         */
        Collection<String> visibleServices(String modelPackageUri, String model, String provider, Collection<String> services);

        /**
         * Restrict a collection of resources to those which are visible
         * @param modelPackageUri
         * @param model
         * @param provider
         * @param resources
         * @return
         */
        Collection<String> visibleResources(String modelPackageUri, String model, String provider, String service, Collection<String> resources);
    }

    public enum PermissionLevel {
        DESCRIBE,
        READ,
        UPDATE,
        ACT
    }

    public class NotPermittedException extends RuntimeException {

        private static final long serialVersionUID = 2073211687446420674L;

        public NotPermittedException(String message) {
            super(message);
        }
    }
}
