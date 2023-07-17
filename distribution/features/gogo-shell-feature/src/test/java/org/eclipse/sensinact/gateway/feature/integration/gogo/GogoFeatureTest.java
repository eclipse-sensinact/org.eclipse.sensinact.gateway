/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.gateway.feature.integration.gogo;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.sensinact.gateway.feature.utilities.test.ServerProcessHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GogoFeatureTest {

    private static ServerProcessHandler server = new ServerProcessHandler();

    @BeforeEach
    void startServer() throws Exception {
        server.startSensinact();
    }

    @AfterEach
    void stopServer() throws Exception {
        server.stopSensinact(true);
    }

    @Test
    void checkPrompt() throws Exception {
        // Clear current output
        server.getOutput(true);

        boolean prompt = false;
        for (int i = 0; i < 5; i++) {
            server.sendCommand("");
            prompt = server.getOutput(true).endsWith("g! ");
            if (prompt)
                break;
            Thread.sleep(1000);
        }

        assertTrue(prompt, "No shell prompt");
    }

}
