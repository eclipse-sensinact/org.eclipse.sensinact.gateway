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

package org.eclipse.sensinact.northbound.security.authorization.casbin;

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.northbound.security.api.AuthorizationEngine;
import org.eclipse.sensinact.northbound.security.api.PreAuthorizer;
import org.eclipse.sensinact.northbound.security.api.PreAuthorizer.PreAuth;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, configurationPid = Constants.CONFIGURATION_PID, configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class CasbinAuthorizationEngine implements AuthorizationEngine {

    private static final Logger logger = LoggerFactory.getLogger(CasbinAuthorizationEngine.class);

    /**
     * sensiNact gateway thread
     */
    @Reference
    GatewayThread gateway;

    /**
     * Flag to allow or deny unspecified authorizations
     */
    boolean allowByDefault;

    @Activate
    void activate(CasbinAuthorizationConfiguration configuration) {
        this.allowByDefault = configuration.allowByDefault();
    }

    @Override
    public PreAuthorizer createAuthorizer(final UserInfo user) {
        return new CasbinPreAuthorizer(user, this::extractAuthFromModel, allowByDefault);
    }

    private PreAuth extractAuthFromModel(final UserInfo user, final SensinactAccess rcPath) {
        try {
            return gateway.execute(new AbstractSensinactCommand<PreAuth>() {
                @Override
                protected Promise<PreAuth> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
                        PromiseFactory pf) {
                    // TODO load from model
                    return pf.resolved(PreAuth.UNKNOWN);
                }
            }).getValue();
        } catch (Exception e) {
            logger.error("Error extracting authorization from model", e);
            return PreAuth.DENY;
        }
    }
}
