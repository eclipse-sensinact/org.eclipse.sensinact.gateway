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
package org.eclipse.sensinact.gateway.nthbnd.endpoint;

public class AttributeGetRequest extends AttributeRequest {
    /**
     * @param mediator
     * @param serviceProvider
     * @param service
     * @param resource
     * @param attribute
     */
    public AttributeGetRequest(NorthboundMediator mediator, String requestIdentifier, String serviceProvider, String service, String resource, String attribute) {
        super(mediator, requestIdentifier, serviceProvider, service, resource, attribute);
    }

    /**
     * @inheritDoc
     * @see ResourceRequest#getMethod()
     */
    @Override
    protected String getMethod() {
        return "get";
    }
}
