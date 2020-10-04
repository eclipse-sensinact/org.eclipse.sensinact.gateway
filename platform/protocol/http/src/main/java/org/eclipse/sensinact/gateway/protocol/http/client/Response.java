/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
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
