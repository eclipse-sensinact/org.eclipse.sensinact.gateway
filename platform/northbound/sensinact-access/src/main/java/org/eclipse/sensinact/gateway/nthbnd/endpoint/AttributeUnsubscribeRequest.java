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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AttributeUnsubscribeRequest extends AttributeRequest {
	
    private String subscriptionId;
	private Argument[] extraArguments;

    /**
     * @param mediator
     * @param responseFormat
     * @param serviceProvider
     * @param service
     * @param resource
     */
    public AttributeUnsubscribeRequest(NorthboundMediator mediator, String requestIdentifier, String serviceProvider, String service, String resource, 
    		String attribute, String subscriptionId, Argument[] extraArguments) {
        super(mediator, requestIdentifier, serviceProvider, service, resource, attribute);

        this.subscriptionId = subscriptionId;
        this.extraArguments = extraArguments;
        if (this.subscriptionId == null) 
            throw new NullPointerException("Subscription identifier mising");
        
    }

    @Override
    protected Argument[] getExecutionArguments() {
    	int offset = this.extraArguments == null?0:this.extraArguments.length;
        Argument[] superArguments = super.getExecutionArguments();
        int length = superArguments == null ? 0 : superArguments.length;
        Argument[] arguments = new Argument[length + offset + 1];
        if (length > 0) 
            System.arraycopy(superArguments, 0, arguments, 0, length);
        arguments[length] = new Argument(String.class, this.subscriptionId);
        if (offset > 0)
            System.arraycopy(this.extraArguments, 0, arguments, length+1, offset);
        return arguments;
    }

    @Override
    protected String getMethod() {
        return "unsubscribe";
    }
}
