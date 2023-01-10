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
package org.eclipse.sensinact.gateway.southbound.http.factory.integration;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Runs keytool to manage certificates
 */
public class KeyToolUtils {

    private final String execPath;

    public KeyToolUtils() throws Exception {
        final Path javaBinDir = Paths.get(ProcessHandle.current().info().command().get()).getParent();

        String tool = "keytool";
        if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            tool += ".exe";
        }

        execPath = javaBinDir.resolve(tool).toString();
    }

    public int runTool(final Object... args) throws IOException {
        return runTool(null, null, args);
    }

    public int runTool(final Redirect input, final Redirect output, final Object... args) throws IOException {
        final List<String> allArgs = new ArrayList<>();
        allArgs.add(execPath);
        allArgs.addAll(Arrays.stream(args).map(Object::toString).collect(Collectors.toList()));

        final ProcessBuilder builder = new ProcessBuilder(allArgs);
        if (input != null) {
            builder.redirectInput(input);
            builder.redirectErrorStream();
        }
        if (output != null) {
            builder.redirectOutput(output);
        }

        final Process process = builder.start();
        try {
            return process.waitFor();
        } catch (InterruptedException e) {
            process.destroyForcibly();
            return 127;
        }
    }
}
