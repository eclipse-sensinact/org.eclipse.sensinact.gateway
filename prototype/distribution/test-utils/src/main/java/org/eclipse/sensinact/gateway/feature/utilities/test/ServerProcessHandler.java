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
package org.eclipse.sensinact.gateway.feature.utilities.test;

import static java.lang.ProcessBuilder.Redirect.PIPE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.io.OutputStream;

public class ServerProcessHandler {

    /**
     * Server process pointer
     */
    private Process process;

    /**
     * Output consumer thread
     */
    private Thread outputThread;

    /**
     * Output consumer
     */
    private InputStreamConsumer consumer;

    private String configFolder;

    public void startSensinact() throws IOException {
        String javaCmd = ProcessHandle.current().info().command().orElse("java");
        process = new ProcessBuilder(javaCmd, "-Dsensinact.config.dir=" + getConfigFolder(), "-jar",
                "target/it/launcher.jar").redirectInput(PIPE).redirectOutput(PIPE).redirectErrorStream(true).start();

        // Show live data and keep track of the output
        consumer = new InputStreamConsumer(process.getInputStream(), true, true);
        outputThread = new Thread(consumer);
        outputThread.start();
    }

    /**
     * Set the config folder to be used by this server. <code>null</code> means
     * <code>src/it/resources/config</code>
     *
     * @param folder
     */
    public void setConfigFolder(String folder) {
        configFolder = folder;
    }

    private String getConfigFolder() {
        return configFolder == null ? "src/it/resources/config" : configFolder;
    }

    /**
     * Checks if the process is alive
     */
    public boolean isAlive() {
        return process != null && process.isAlive();
    }

    /**
     * Gives access to the server process
     */
    public Process getProcess() {
        return process;
    }

    /**
     * Gives access to the output consumer
     */
    public InputStreamConsumer getConsumer() {
        return consumer;
    }

    /**
     * Destroys the server process
     */
    public void stopSensinact() {
        stopSensinact(false);
    }

    /**
     * Stops or destroys the server
     *
     * @param useShell If set, use the "exit 0" shell command to stop the server,
     *                 else destroy it
     */
    public void stopSensinact(boolean useShell) {
        if (process == null) {
            return;
        }

        try {
            if (useShell) {
                try {
                    sendCommand("exit 0");
                } catch (IOException e) {
                    process.destroy();
                }
            } else {
                process.destroy();
            }
            process.waitFor(5, SECONDS);
        } catch (InterruptedException e) {
            // Ignore
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
            }
            process = null;
        }

        if (outputThread != null) {
            try {
                outputThread.join(1000);
            } catch (InterruptedException e) {
                // Ignore
            }
            outputThread = null;
        }
    }

    /**
     * Reads the current state of the output (and keep it)
     */
    public String getOutput() throws IOException {
        return getOutput(false);
    }

    /**
     * Reads the current output buffer
     *
     * @param clear If True, clears the output buffer
     * @throws IOException Error reading output (server not available)
     */
    public String getOutput(boolean clear) throws IOException {
        if (consumer != null) {
            String output = consumer.getOutput();
            if (clear) {
                consumer.clear();
            }
            return output;
        }

        return null;
    }

    /**
     * Writes and flushes data to the server stdin
     *
     * @param data Data to be written (converted in UTF 8)
     * @throws IOException Process not available or error writing to it
     */
    public void write(String data) throws IOException {
        if (process == null) {
            throw new IOException("Server is not running");
        }

        OutputStream stream = process.getOutputStream();
        stream.write(data.getBytes(UTF_8));
        stream.flush();
    }

    /**
     * Sends a command to the Felix shell (includes new line characters)
     *
     * @param command Command to be sent to Felix
     * @throws IOException Process not available or error writing to it
     */
    public void sendCommand(String command) throws IOException {
        write("\n" + command + "\n");
    }
}
