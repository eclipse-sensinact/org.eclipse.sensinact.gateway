/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.protocol.http.client;

import org.eclipse.sensinact.gateway.protocol.http.Headers;

/**
 * An Http Response service signature
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface Response extends Headers {
    /**
     * Returns the bytes array content of the HTTP response
     *
     * @return the bytes array content of the HTTP response
     */
    public byte[] getContent();

    /**
     * Returns the HTTP code of the HTTP response
     *
     * @return the HTTP response code
     */
    public int getStatusCode();

}
