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

public class AttributeGetRequest extends AttributeRequest {
	
    private Argument[] extraArguments;

	/**
     * @param mediator
     * @param serviceProvider
     * @param service
     * @param resource
     * @param attribute
     */
    public AttributeGetRequest(String requestIdentifier, String serviceProvider, String service, String resource, 
    		String attribute, Argument[] extraArguments) {
        super(requestIdentifier, serviceProvider, service, resource, attribute);
        this.extraArguments = extraArguments;
    }

    @Override
    protected String getMethod() {
        return "get";
    }
    
    @Override
    protected Argument[] getExecutionArguments() {
    	int offset = this.extraArguments == null?0:this.extraArguments.length;
    	if(offset == 0)
    		return super.getExecutionArguments();    	
        Argument[] superArguments = super.getExecutionArguments();
        int length = superArguments == null ? 0 : superArguments.length;
        Argument[] arguments = new Argument[length + offset];
        if (length > 0) 
            System.arraycopy(superArguments, 0, arguments, 0, length);
        System.arraycopy(this.extraArguments, 0, arguments, length, offset);
        return arguments;
    }
}
