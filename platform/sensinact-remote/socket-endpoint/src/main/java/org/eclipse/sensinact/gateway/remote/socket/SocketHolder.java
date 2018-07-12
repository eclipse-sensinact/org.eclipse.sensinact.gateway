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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;

/**
 * Holds a Socket and its asociated resources
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class SocketHolder {
    Socket socket;
    SocketInputStreamReader reader;
    SocketOutputStreamWriter writer;
    private Mediator mediator;

    /**
     * @param mediator
     * @param socket
     * @throws IOException
     */
    public SocketHolder(Mediator mediator, Socket socket) throws IOException {
        this.mediator = mediator;
        this.socket = socket;
        this.reader = new SocketInputStreamReader(socket.getInputStream());
        this.writer = new SocketOutputStreamWriter(socket.getOutputStream());
    }

    public boolean checkSocketStatus() {
        if (socket == null || !socket.isConnected()) {
            return false;
        }
        return (!socket.isClosed() && !socket.isInputShutdown() && !socket.isOutputShutdown());
    }

    public JSONObject read() throws JSONException, IOException {
        return this.reader.read();
    }

    public void write(JSONObject object) throws IOException {
        this.writer.write(object);
    }

    public void close() {
        if (socket != null && socket.isConnected()) {
            try {
                socket.close();

            } catch (IOException e) {
                mediator.error(e);
            }
            socket = null;
        }
    }
}
