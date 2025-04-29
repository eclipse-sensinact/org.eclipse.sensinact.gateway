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

public @interface CasbinAuthorizationConfiguration {

    /**
     * Flag to allow access if the authorization couldn't be determined
     */
    boolean allowByDefault() default false;

    /**
     * Definition of policies
     */
    String[] policies() default {};
}
