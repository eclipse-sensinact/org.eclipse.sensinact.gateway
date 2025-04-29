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

package org.eclipse.sensinact.core.authorization;

import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.SensinactService;

/**
 * The Authorizer is designed to be cacheable and Thread safe.
 */
public interface Authorizer {

    /**
     * Test whether this authorizer has the necessary access level for the named
     * provider
     *
     * @param level
     * @param modelPackageUri
     * @param model
     * @param provider
     * @return
     */
    boolean hasProviderPermission(PermissionLevel level, String modelPackageUri, String model, String provider);

    default boolean hasProviderPermission(PermissionLevel level, SensinactProvider provider) {
        return provider != null && hasProviderPermission(level, provider.getModelPackageUri(), provider.getModelName(),
                provider.getName());
    }

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
    boolean hasServicePermission(PermissionLevel level, String modelPackageUri, String model, String provider,
            String service);

    default boolean hasServicePermission(PermissionLevel level, SensinactService service) {
        if (service == null) {
            return false;
        }

        final SensinactProvider provider = service.getProvider();
        return hasServicePermission(level, provider.getModelPackageUri(), provider.getModelName(), provider.getName(),
                service.getName());
    }

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
    boolean hasResourcePermission(PermissionLevel level, String modelPackageUri, String model, String provider,
            String service, String resource);

    default boolean hasResourcePermission(PermissionLevel level, SensinactResource resource) {
        if (resource == null) {
            return false;
        }

        final SensinactService service = resource.getService();
        final SensinactProvider provider = service.getProvider();
        return hasResourcePermission(level, provider.getModelPackageUri(), provider.getModelName(), provider.getName(),
                service.getName(), resource.getName());
    }
}
