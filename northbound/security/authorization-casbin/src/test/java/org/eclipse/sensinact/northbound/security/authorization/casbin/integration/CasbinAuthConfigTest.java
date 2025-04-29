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

package org.eclipse.sensinact.northbound.security.authorization.casbin.integration;

import org.eclipse.sensinact.northbound.security.authorization.casbin.Constants;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.Property.Type;
import org.osgi.test.common.annotation.config.WithConfiguration;

public class CasbinAuthConfigTest {

    @WithConfiguration(pid = Constants.CONFIGURATION_PID, location = "?", properties = {
            @Property(key = "allowByDefault", value = "true"),
            @Property(key = "policies", type = Type.Array, value = {
                    "*, .*, .*, sensiNact, .*, .*, read, allow, -10001",
                    "*, .*, .*, sensiNact, .*, .*, .*, deny, -10000",
                    "role:admin, .*, .*, other, .*, .*, write, deny, -1001",
                    "role:admin, .*, .*, .*,   , , , allow, -1000",
                    "role:admin, .*, .*, provider, .*, .*, .*, deny, -1001",
                    "role:user, .*, .*, provider,  control, .*, act, allow, 0",
                    "role:user, .*, .*, other, .*, .*, write, allow, 9998",
                    "role:user, .*, .*, .*, .*, .*, read, allow, 9999",
                    "role:user, .*, .*, .*, .*, .*, .*, deny, 10000",
            }) })
    void basicTest() throws Exception {

    }
}
