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
package org.eclipse.sensinact.gateway.feature.integration.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.StringReader;

import org.eclipse.sensinact.gateway.feature.utilities.test.ServerProcessHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CoreFeatureTest {

    private static ServerProcessHandler server = new ServerProcessHandler();

    @BeforeEach
    void startServer() throws Exception {
        server.startSensinact();
        checkPrompt();
    }

    void checkPrompt() throws Exception {
        // Clear the input buffer so we have less to search
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

    @AfterEach
    void stopServer() throws Exception {
        server.stopSensinact(true);
    }

    @Test
    void checkAllResolved() throws Exception {
        server.sendCommand("lb");

        // A short wait as lb can take time to complete its output
        Thread.sleep(100);

        String bundles = server.getOutput(true);
        assertFalse(bundles.isBlank());
        assertFalse(bundles.contains("Installed"), "Some bundles were not resolved:\n" + bundles);

        // There should be 40 lines (36 bundles, 2 header lines and 2 trailing lines)
        try (BufferedReader br = new BufferedReader(new StringReader(bundles))) {
            assertEquals(42, br.lines().count());
        }
    }
}
