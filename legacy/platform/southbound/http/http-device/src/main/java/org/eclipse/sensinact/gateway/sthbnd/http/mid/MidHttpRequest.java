/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
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
