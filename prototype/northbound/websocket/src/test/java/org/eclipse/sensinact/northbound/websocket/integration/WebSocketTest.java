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
package org.eclipse.sensinact.northbound.websocket.integration;

import org.eclipse.sensinact.prototype.PrototypePush;
import org.eclipse.sensinact.prototype.SensiNactSessionManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.service.ServiceExtension;

@ExtendWith(ServiceExtension.class)
public class BasicTest {

    @InjectService
    SensiNactSessionManager sessionManager;

    @InjectService
    PrototypePush push;

    /**
     * Check the <code>/sensinact/</code> endpoint: full description of providers,
     * services and resources
     */
    @Test
    void test() throws Exception {
        // Wait here
        System.out.println("WAITING....");
        Thread.sleep(3600 * 1000);
        System.out.println("Stopping.");
    }
}
