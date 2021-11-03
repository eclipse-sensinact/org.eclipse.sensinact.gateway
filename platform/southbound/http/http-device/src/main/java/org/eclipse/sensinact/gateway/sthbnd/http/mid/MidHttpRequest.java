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
package org.eclipse.sensinact.gateway.sthbnd.http.mid;

import java.io.IOException;

import org.eclipse.sensinact.gateway.protocol.http.client.AbstractRequest;
import org.eclipse.sensinact.gateway.protocol.http.client.mid.Reusable;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpConnectionConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponse;

/**
 *
 */
public abstract class MidHttpRequest<RESPONSE extends HttpResponse> extends AbstractRequest<RESPONSE> implements Reusable {

    /**
     * @param mediator
     * @param configuration
     */
    public MidHttpRequest(HttpConnectionConfiguration<RESPONSE, MidHttpRequest<RESPONSE>> configuration) {
        super(configuration);
    }

    @Override
    @SuppressWarnings("unchecked")
    public RESPONSE send() throws IOException {
        RESPONSE response = super.send();

        if (((MidHttpTask<RESPONSE, MidHttpRequest<RESPONSE>>) super.configuration).isDirect()) {
            ((MidHttpTask<RESPONSE, MidHttpRequest<RESPONSE>>) super.configuration).setResult(response.getContent());
        }
        return response;
    }
}
