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

import org.eclipse.sensinact.gateway.core.message.AbstractMidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.SnaErrorMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaResponseMessage;
import org.eclipse.sensinact.gateway.core.message.SnaUpdateMessageImpl;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONArray;

public class RegisterAgentRequest extends NorthboundRequest {
    private NorthboundRecipient recipient;
    private JSONArray constraints;

    private String serviceProvider;
    private String service;
    private SnaFilter filter;

    /**
     * Constructor
     *
     * @param mediator
     * @param serviceProvider
     * @param service
     * @param recipient
     * @param constraints
     */
    public RegisterAgentRequest(NorthboundMediator mediator, String requestIdentifier, String serviceProvider, String service, NorthboundRecipient recipient, SnaFilter filter) {
        super(mediator, requestIdentifier, null);
        this.serviceProvider = serviceProvider;
        this.service = service;
        this.recipient = recipient;
        this.filter = filter;
        if (this.recipient == null) {
            throw new NullPointerException("Recipient missing");
        }
    }

    /**
     * @inheritDoc
     * @see ServiceProvidersRequest#getExecutionArguments()
     */
    @Override
    protected Argument[] getExecutionArguments() {
        AbstractMidAgentCallback callback = new AbstractMidAgentCallback() {
            @Override
            public void doHandle(SnaLifecycleMessageImpl message) {
                RegisterAgentRequest.this.recipient.doCallback(message);
            }

            @Override
            public void doHandle(SnaUpdateMessageImpl message) {
                RegisterAgentRequest.this.recipient.doCallback(message);
            }

            @Override
            public void doHandle(SnaErrorMessageImpl message) {
                RegisterAgentRequest.this.recipient.doCallback(message);
            }

            @Override
            public void doHandle(SnaResponseMessage<?, ?> message) {
                RegisterAgentRequest.this.recipient.doCallback(message);
            }

            @Override
            public void setIdentifier(String identifier) {
                super.setIdentifier(identifier);
                RegisterAgentRequest.this.recipient.setIdentifier(super.identifier);
            }
        };
        Argument[] superArguments = super.getExecutionArguments();
        int length = superArguments == null ? 0 : superArguments.length;
        Argument[] arguments = new Argument[length + 2];
        if (length > 0) {
            System.arraycopy(superArguments, 0, arguments, 0, length);
        }
        arguments[length] = new Argument(AbstractMidAgentCallback.class, callback);
        arguments[length + 1] = new Argument(SnaFilter.class, filter);
        return arguments;
    }

    /**
     * @inheritDoc
     * @see ResourceRequest#getMethod()
     */
    @Override
    protected String getMethod() {
        return "registerAgent";
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.common.primitive.Nameable#getName()
     */
    @Override
    public String getName() {
        return UriUtils.PATH_SEPARATOR;
    }
}
