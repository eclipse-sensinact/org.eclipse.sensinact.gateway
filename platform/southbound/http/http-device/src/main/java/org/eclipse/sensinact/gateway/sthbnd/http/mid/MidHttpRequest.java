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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.protocol.http.client.AbstractRequest;
import org.eclipse.sensinact.gateway.protocol.http.client.mid.Reusable;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpConnectionConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponse;

import java.io.IOException;

/**
 *
 */
public abstract class MidHttpRequest<RESPONSE extends HttpResponse> extends AbstractRequest<RESPONSE> implements Reusable {
    protected Mediator mediator;

    /**
     * @param mediator
     * @param configuration
     */
    public MidHttpRequest(Mediator mediator, HttpConnectionConfiguration<RESPONSE, MidHttpRequest<RESPONSE>> configuration) {
        super(configuration);
        this.mediator = mediator;
    }

    /**
     * @inheritDoc
     * @see HttpTask#send()
     */
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
