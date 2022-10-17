/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.protocol.http.server;

import org.eclipse.sensinact.gateway.protocol.http.HeadersCollection;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractContent extends HeadersCollection implements Content {
    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//
    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//
    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//
    private byte[] content;

    /**
     *
     */
    public AbstractContent() {
    }

    /**
     * @param headers
     */
    public AbstractContent(Map<String, List<String>> headers) {
        super(headers);
    }

    /**
     * @inheritDoc
     * @see Content#getContent()
     */
    @Override
    public byte[] getContent() {
        int length = this.content == null ? 0 : this.content.length;
        byte[] copy = new byte[length];

        if (length > 0) {
            System.arraycopy(this.content, 0, copy, 0, length);
        }
        return copy;
    }

    /**
     * @inheritDoc
     * @see Content#setContent(byte[])
     */
    @Override
    public void setContent(byte[] content) {
        int length = content == null ? 0 : content.length;
        byte[] copy = new byte[length];

        if (length > 0) {
            System.arraycopy(content, 0, copy, 0, length);
        }
        this.content = copy;
    }
}
