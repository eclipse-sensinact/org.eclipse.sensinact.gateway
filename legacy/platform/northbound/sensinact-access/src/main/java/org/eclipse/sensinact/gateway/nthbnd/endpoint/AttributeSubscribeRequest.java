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

import jakarta.json.JsonArray;

public class AttributeSubscribeRequest extends AttributeRequest {
	
    private NorthboundRecipient recipient;
    private JsonArray conditions;
	private String policy;
	private Argument[] extraArguments;

    /**
     * @param serviceProvider
     * @param service
     * @param resource
     * @param attribute
     * @param recipient
     */
    public AttributeSubscribeRequest(String requestIdentifier, 
    		String serviceProvider, String service, String resource, String attribute, 
    		NorthboundRecipient recipient, JsonArray conditions, String policy, 
    		Argument[] extraArguments) {
    	
        super(requestIdentifier, serviceProvider, service, resource, attribute);
        this.recipient = recipient;
        this.conditions = conditions;
        this.policy = policy;
        this.extraArguments = extraArguments;
        
        if (this.recipient == null) 
            throw new NullPointerException("Recipient missing");
    }

    @Override
    protected Argument[] getExecutionArguments() {
    	int offset = this.extraArguments == null?0:this.extraArguments.length;
        Argument[] superArguments = super.getExecutionArguments();
        int length = superArguments == null ? 0 : superArguments.length;
        Argument[] arguments = new Argument[length + offset + 3];
        if (length > 0) 
            System.arraycopy(superArguments, 0, arguments, 0, length);
        arguments[length] = new Argument(NorthboundRecipient.class, this.recipient);
        arguments[length + 1] = new Argument(JsonArray.class, this.conditions);
        arguments[length + 2] = new Argument(String.class, this.policy);
        if (offset > 0)
            System.arraycopy(this.extraArguments, 0, arguments, length+3, offset);
        return arguments;
    }

    @Override
    protected String getMethod() {
        return "subscribe";
    }
}
