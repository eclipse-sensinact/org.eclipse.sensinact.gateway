/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.endpoint;

public class AttributeUnsubscribeRequest extends AttributeRequest {
	
    private String subscriptionId;
	private Argument[] extraArguments;

    /**
     * @param responseFormat
     * @param serviceProvider
     * @param service
     * @param resource
     */
    public AttributeUnsubscribeRequest(String requestIdentifier, String serviceProvider, String service, String resource, 
    		String attribute, String subscriptionId, Argument[] extraArguments) {
        super(requestIdentifier, serviceProvider, service, resource, attribute);

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
