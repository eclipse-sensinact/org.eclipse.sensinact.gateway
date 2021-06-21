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

public class AttributeGetRequest extends AttributeRequest {
	
    private Argument[] extraArguments;

	/**
     * @param mediator
     * @param serviceProvider
     * @param service
     * @param resource
     * @param attribute
     */
    public AttributeGetRequest(NorthboundMediator mediator, String requestIdentifier, String serviceProvider, String service, String resource, 
    		String attribute, Argument[] extraArguments) {
        super(mediator, requestIdentifier, serviceProvider, service, resource, attribute);
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
