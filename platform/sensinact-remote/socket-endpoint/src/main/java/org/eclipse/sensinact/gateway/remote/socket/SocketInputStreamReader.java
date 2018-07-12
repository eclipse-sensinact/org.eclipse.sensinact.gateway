/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.remote.socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * InputStream wrapper
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class SocketInputStreamReader {
    private InputStream input;

    /**
     * Constructor
     */
    public SocketInputStreamReader(InputStream input) {
        this.input = input;
    }

    /**
     * @return
     * @throws IOException
     */
    protected JSONObject read() throws IOException, JSONException {
        JSONObject object = null;
        int read = 0;
        int length = 0;
        byte[] content = new byte[length];
        byte[] buffer = new byte[SocketEndpoint.BUFFER_SIZE];

        boolean eof = false;
        while ((read = input.read(buffer)) > -1) {
            eof = (buffer[read - 1] == '\0');
            byte[] newContent = new byte[length + read];
            if (length > 0) {
                System.arraycopy(content, 0, newContent, 0, length);
            }
            System.arraycopy(buffer, 0, newContent, length, eof ? read - 1 : read);
            content = newContent;
            newContent = null;
            length += (eof ? read - 1 : read);
            if (eof) {
                break;
            }
        }
        String strContent = new String(content);
        if (strContent != null && strContent.length() > 0) {
            object = new JSONObject(strContent);
        }
        return object;
    }
}
