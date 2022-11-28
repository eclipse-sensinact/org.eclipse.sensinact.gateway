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

import static java.lang.ProcessBuilder.Redirect.PIPE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CoreFeatureTest {

    private Process OSGI_PROCESS;

    private ByteArrayOutputStream output = new ByteArrayOutputStream();

    private InputStream teeInput;

    private class TeeInputStream extends InputStream {
        private final OutputStream sink;
        private final InputStream source;

        public TeeInputStream(OutputStream sink, InputStream source) {
            this.sink = sink;
            this.source = source;
        }

        @Override
        public int read() throws IOException {
            int read = source.read();
            if (read != -1) {
                sink.write(read);
            }
            return read;
        }

        @Override
        public int available() throws IOException {
            return source.available();
        }

        @Override
        public void close() throws IOException {
            try {
                sink.close();
            } finally {
                source.close();
            }
        }
    }

    @BeforeEach
    void startServer() throws Exception {

        OSGI_PROCESS = new ProcessBuilder("java", "-Dsensinact.config.dir=src/it/resources/config", "-jar",
                "target/it/launcher.jar").redirectInput(PIPE).redirectOutput(PIPE).redirectErrorStream(true).start();

        teeInput = new TeeInputStream(output, OSGI_PROCESS.getInputStream());

        checkPrompt();
    }

    void checkPrompt() throws Exception {
        // Clear the input buffer so we have less to search
        readAllInput();

        boolean prompt = false;
        for (int i = 0; i < 5; i++) {
            sendCommand("");
            prompt = readAllInput().endsWith("g! ");
            if (prompt)
                break;
            Thread.sleep(1000);
        }

        assertTrue(prompt, "No shell prompt");
    }

    @AfterEach
    void stopServer() throws Exception {

        try {
            readAllInput();
            System.out.print(output.toString(UTF_8));
        } catch (IOException ioe) {
        }

        try {
            sendCommand("exit 0");
            OSGI_PROCESS.waitFor(5, SECONDS);
        } finally {
            if (OSGI_PROCESS.isAlive()) {
                OSGI_PROCESS.destroyForcibly();
            }
        }
    }

    private String readAllInput() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (teeInput.available() > 0) {
            baos.write(teeInput.readNBytes(teeInput.available()));
        }
        return baos.toString(UTF_8);
    }

    private void sendCommand(String command) throws IOException {
        OutputStream stream = OSGI_PROCESS.getOutputStream();
        stream.write(("\n" + command + "\n").getBytes(UTF_8));
        stream.flush();
    }

    @Test
    void checkAllResolved() throws IOException {
        sendCommand("lb");

        String bundles = readAllInput();
        assertFalse(bundles.contains("Installed"), "Some bundles were not resolved:\n" + bundles);
    }

}
