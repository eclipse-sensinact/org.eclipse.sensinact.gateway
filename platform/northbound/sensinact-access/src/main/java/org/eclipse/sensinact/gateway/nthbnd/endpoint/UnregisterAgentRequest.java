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
package org.eclipse.sensinact.gateway.nthbnd.endpoint;

import org.eclipse.sensinact.gateway.util.UriUtils;

public class UnregisterAgentRequest extends NorthboundRequest {
    private String agentId;

    /**
     * Constructor
     *
     * @param mediator
     * @param agentId
     */
    public UnregisterAgentRequest(NorthboundMediator mediator, String requestIdentifier, String agentId) {
        super(mediator, requestIdentifier, null);
        this.agentId = agentId;
    }

    /**
     * @inheritDoc
     * @see ServiceProvidersRequest#getExecutionArguments()
     */
    @Override
    protected Argument[] getExecutionArguments() {
        Argument[] superArguments = super.getExecutionArguments();
        int length = superArguments == null ? 0 : superArguments.length;
        Argument[] arguments = new Argument[length + 1];
        if (length > 0) {
            System.arraycopy(superArguments, 0, arguments, 0, length);
        }
        arguments[length] = new Argument(String.class, this.agentId);
        return arguments;
    }

    /**
     * @inheritDoc
     * @see ResourceRequest#getMethod()
     */
    @Override
    protected String getMethod() {
        return "unregisterAgent";
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.common.primitive.Nameable#getName()
     */
    @Override
    public String getName() {
        return UriUtils.ROOT;
    }
}
