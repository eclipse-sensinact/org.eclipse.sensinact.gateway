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

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

/**
 * OutputStream wrapper
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class SocketOutputStreamWriter {
    private OutputStream output;

    /**
     * Constructor
     */
    public SocketOutputStreamWriter(OutputStream output) {
        this.output = output;
    }

    /**
     * @param response
     * @throws IOException
     */
    protected void write(JSONObject response) throws IOException {
        int block = SocketEndpoint.BUFFER_SIZE;
        byte[] data = response.toString().getBytes();
        int written = 0;
        int length = data == null ? 0 : data.length;

        while (written < length) {
            if ((written + block) > length) {
                block = length - written;
            }
            output.write(data, written, block);
            written += block;
        }
        output.write(new byte[]{'\0'});
        output.flush();
    }
}
