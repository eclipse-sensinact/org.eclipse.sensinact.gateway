/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper methods for InputStream and OutputStream accesses
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class IOUtils {
    private static final Logger LOGGER = Logger.getLogger(IOUtils.class.getCanonicalName());
    private static final int BUFFER_SIZE = 64 * 1024;
    private static final int UNLIMITED = -1;

    /**
     * @param input the InputStream to read
     * @return
     */
    public static byte[] read(InputStream input) {
        return IOUtils.read(input, UNLIMITED, true);
    }

    /**
     * @param input the InputStream to read
     * @return
     */
    public static byte[] read(InputStream input, boolean closeable) {
        return IOUtils.read(input, UNLIMITED, closeable);
    }

    /**
     * @param input the InputStream to read
     * @return
     */
    public static byte[] read(InputStream input, int size) {
        return IOUtils.read(input, size, true);
    }

    /**
     * @param input the InputStream to read
     * @return
     */
    public static byte[] read(InputStream input, int size, boolean closeable) {
        int read = 0;
        int length = 0;
        byte[] content = new byte[length];
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            while (true) {
                if (size > UNLIMITED && length >= size) {
                    break;
                }
                read = input.read(buffer);
                if (read == -1) {
                    break;
                }
                byte[] newContent = new byte[length + read];
                if (length > 0) {
                    System.arraycopy(content, 0, newContent, 0, length);
                }
                System.arraycopy(buffer, 0, newContent, length, read);
                content = newContent;
                newContent = null;
                length += read;
            }
        } catch (IOException e) {
            LOGGER.log(Level.CONFIG, e.getMessage(), e);

        } finally {
            if (closeable) {
                try {
                    input.close();

                } catch (Exception e) {
                    LOGGER.log(Level.CONFIG, e.getMessage(), e);
                }
            }
        }
        return content;
    }

    /**
     * @param content the bytes array to write
     * @param output  the OutputStream to write in
     */
    public static void write(byte[] content, OutputStream output) {
        int written = 0;
        int length = content == null ? 0 : content.length;
        int block = BUFFER_SIZE;
        try {
            while (written < length) {
                if ((written + block) > length) {
                    block = length - written;
                }
                output.write(content, written, block);
                written += block;
            }
        } catch (IOException e) {
            LOGGER.log(Level.CONFIG, e.getMessage(), e);
        } finally {
            try {
                output.close();

            } catch (Exception e) {
                LOGGER.log(Level.CONFIG, e.getMessage(), e);
            }
        }
    }
}
