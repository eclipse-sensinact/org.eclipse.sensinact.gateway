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
import java.net.ConnectException;

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
        int pos = 0;
        int length = 0;
        byte[] content = new byte[length];
        byte[] buffer = new byte[SocketEndpoint.BUFFER_SIZE];

        boolean eof = false;
        while (true) {
        	read = input.read();
        	if(read == -1) {
        		throw new ConnectException();
        	}
            eof = ( read == '\0') ;
            buffer[pos] = (byte) read;
            pos+=(eof)?0:1;
            if(eof || pos == SocketEndpoint.BUFFER_SIZE) {            
	            byte[] newContent = new byte[length + pos];
	            if (length > 0) {
	                System.arraycopy(content, 0, newContent, 0, length);
	            }
	            System.arraycopy(buffer, 0, newContent, length, pos);
	            content = newContent;	 
	            newContent = null;           
	            if (eof) {
	                break;
	            }
	            length += pos;
	            pos = 0;
            }
        }
        String strContent = new String(content);
        if (strContent != null && strContent.length() > 0) {
            object = new JSONObject(strContent);
        }
        return object;
    }
}
