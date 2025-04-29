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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import org.casbin.jcasbin.util.function.CustomFunction;
import org.eclipse.sensinact.core.authorization.PermissionLevel;
import org.eclipse.sensinact.northbound.security.api.PreAuthorizer.PreAuth;
import org.eclipse.sensinact.northbound.security.api.UserInfo;

import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

public class ModelAuthorizationChecker extends CustomFunction {

    private static final long serialVersionUID = 1L;

    /**
     * Associated user session information
     */
    private final UserInfo userInfo;

    /**
     * Internal permission cache
     */
    private final Map<SensinactAccess, PreAuth> internalPermissionCache = new HashMap<>();

    /**
     * Method to call to get permission from sensiNact model
     */
    private final BiFunction<UserInfo, SensinactAccess, PreAuth> modelPermissionExtractor;

    /**
     * @param userInfo                 User session information
     * @param modelPermissionExtractor Method to call to get permission from
     *                                 sensiNact model
     */
    public ModelAuthorizationChecker(final UserInfo userInfo,
            final BiFunction<UserInfo, SensinactAccess, PreAuth> modelPermissionExtractor) {
        this.userInfo = userInfo;
        this.modelPermissionExtractor = modelPermissionExtractor;
    }

    @Override
    public String getName() {
        return Constants.AUTH_FROM_MODEL;
    }

    @Override
    public AviatorString call(final Map<String, Object> env, final AviatorObject objSubject,
            final AviatorObject objModelPackageUri, final AviatorObject objModel, final AviatorObject objProvider,
            final AviatorObject objService, final AviatorObject objResource, final AviatorObject objLevel) {

        final String subject = objSubject.stringValue(env);
        if (subject == null || !subject.equals(userInfo.getUserId())) {
            throw new IllegalArgumentException(String.format("Trying to check an invalid user '%s'. Expected '%s'",
                    subject, userInfo.getUserId()));
        }

        final SensinactAccess snaAccess = new SensinactAccess(objModelPackageUri.stringValue(env),
                objModel.stringValue(env), objProvider.stringValue(env), objService.stringValue(env),
                objResource.stringValue(env), PermissionLevel.valueOf(objLevel.stringValue(env)));

        final PreAuth permission = internalPermissionCache.computeIfAbsent(snaAccess, sub -> Optional
                .ofNullable(modelPermissionExtractor).map(f -> f.apply(userInfo, snaAccess)).orElse(PreAuth.UNKNOWN));
        return new AviatorString(permission.name());
    };
}
