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

public abstract class AttributeRequest extends ResourceRequest {
    private String attribute;

    /**
     * @param mediator
     * @param serviceProvider
     * @param service
     * @param resource
     * @param attribute
     */
    public AttributeRequest(NorthboundMediator mediator, String requestIdentifier, String serviceProvider, String service, String resource, String attribute) {
        super(mediator, requestIdentifier, serviceProvider, service, resource);

        this.attribute = attribute;
//      if (this.attribute == null) {
//      throw new NullPointerException("Attribute missing");
//  }
    }

    /**
     * @inheritDoc
     * @see ResourcesRequest#getName()
     */
    public String getName() {
        return attribute;
    }

    /**
     * @inheritDoc
     * @see ResourceRequest#getMethod()
     */
    @Override
    protected String getMethod() {
        return null;
    }

    /**
     * @inheritDoc
     * @see NorthboundRequest#getExecutionArguments()
     */
    @Override
    protected Argument[] getExecutionArguments() {
        Argument[] superArguments = super.getExecutionArguments();
        int length = superArguments == null ? 0 : superArguments.length;
        Argument[] arguments = new Argument[length + 1];
        if (length > 0) {
            System.arraycopy(superArguments, 0, arguments, 0, length);
        }
        arguments[length] = new Argument(String.class, attribute);
        return arguments;
    }
}
