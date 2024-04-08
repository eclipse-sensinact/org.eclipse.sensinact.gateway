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

import static org.eclipse.sensinact.northbound.security.api.AuthorizationEngine.Authorizer.PreAuth.ALLOW;

import java.util.Collection;
import java.util.List;

import org.eclipse.sensinact.northbound.security.api.AuthorizationEngine.Authorizer;
import org.eclipse.sensinact.northbound.security.api.AuthorizationEngine.PermissionLevel;

class AllowAllAuthorizer implements Authorizer {

    @Override
    public PreAuth preAuthProvider(PermissionLevel level, String provider) {
        return ALLOW;
    }

    @Override
    public PreAuth preAuthService(PermissionLevel level, String provider, String service) {
        return ALLOW;
    }

    @Override
    public PreAuth preAuthResource(PermissionLevel level, String provider, String service, String resource) {
        return ALLOW;
    }

    @Override
    public boolean hasProviderPermission(PermissionLevel level, String modelPackageUri, String model,
            String provider) {
        return true;
    }

    @Override
    public boolean hasServicePermission(PermissionLevel level, String modelPackageUri, String model,
            String provider, String service) {
        return true;
    }

    @Override
    public boolean hasResourcePermission(PermissionLevel level, String modelPackageUri, String model,
            String provider, String service, String resource) {
        return true;
    }

    @Override
    public Collection<String> visibleServices(String modelPackageUri, String model, String provider,
            Collection<String> services) {
        return List.copyOf(services);
    }

    @Override
    public Collection<String> visibleResources(String modelPackageUri, String model, String provider,
            String service, Collection<String> resources) {
        return List.copyOf(resources);
    }
    
}