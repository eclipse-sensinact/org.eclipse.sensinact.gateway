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
package org.eclipse.sensinact.gateway.launcher.integration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class InputStreamConsumer implements Runnable {

    /**
     * Input stream to read
     */
    private InputStream inStream;

    /**
     * Input stream encoding
     */
    private final Charset encoding;

    /**
     * Whole buffer
     */
    private ByteArrayOutputStream wholeBuffer;

    /**
     * Last line (if live output is set)
     */
    private ByteArrayOutputStream lastLineBuffer;
    private boolean newLine = false;

    public InputStreamConsumer(final InputStream inStream) {
        this(inStream, true, false);
    }

    public InputStreamConsumer(final InputStream inStream, boolean liveOutput, boolean keepData) {
        this(inStream, liveOutput, keepData, StandardCharsets.UTF_8);
    }

    public InputStreamConsumer(final InputStream inStream, boolean liveOutput, boolean keepData, Charset encoding) {
        this.inStream = inStream;
        this.encoding = encoding;

        if (liveOutput) {
            lastLineBuffer = new ByteArrayOutputStream();
        }

        if (keepData) {
            wholeBuffer = new ByteArrayOutputStream();
        }
    }

    public void clear() {
        if (wholeBuffer != null) {
            wholeBuffer = new ByteArrayOutputStream();
        }

        if (lastLineBuffer != null) {
            lastLineBuffer = new ByteArrayOutputStream();
        }
    }

    public String getOutput() {
        if (wholeBuffer == null) {
            return null;
        }
        return wholeBuffer.toString(encoding);
    }

    @Override
    public void run() {
        int nextByte;
        try {
            while ((nextByte = inStream.read()) != -1) {
                if (wholeBuffer != null) {
                    wholeBuffer.write((byte) nextByte);
                }

                if (lastLineBuffer != null) {
                    if (nextByte == '\r' || nextByte == '\n') {
                        if (newLine) {
                            System.out.println(lastLineBuffer.toString(encoding));
                            lastLineBuffer = new ByteArrayOutputStream();
                        }
                        newLine = false;
                    } else {
                        newLine = true;
                        lastLineBuffer.write((byte) nextByte);
                    }
                }
            }
        } catch (IOException e) {
            if (lastLineBuffer != null) {
                if (lastLineBuffer.size() > 0) {
                    System.out.println(lastLineBuffer.toString(encoding));
                }
                lastLineBuffer = null;
            }

            e.printStackTrace();

            try {
                inStream.close();
            } catch (IOException e2) {
                // Silently ignore
            }
        }
    }
}
