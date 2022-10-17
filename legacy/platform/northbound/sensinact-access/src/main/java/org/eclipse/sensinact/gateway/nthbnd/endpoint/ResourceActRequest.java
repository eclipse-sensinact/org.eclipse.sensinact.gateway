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

public class ResourceActRequest extends ResourceRequest {
    private Object[] arguments;

    /**
     * @param responseFormat
     * @param serviceProvider
     * @param service
     * @param resource
     * @param requestIdentifier
     * @param arguments
     */
    public ResourceActRequest(String requestIdentifier, String serviceProvider, String service, String resource,
    		Object[] arguments) {
        super(requestIdentifier, serviceProvider, service, resource);
        this.arguments = arguments;
    }

    /**
     * @inheritDoc
     * @see ServiceProvidersRequest#
     * getExecutionArguments()
     */
    @Override
    protected Argument[] getExecutionArguments() {
        Argument[] superArguments = super.getExecutionArguments();
        int length = superArguments == null ? 0 : superArguments.length;
        Argument[] arguments = new Argument[length + 1];
        if (length > 0) {
            System.arraycopy(superArguments, 0, arguments, 0, length);
        }
        arguments[length] = new Argument(Object[].class, this.arguments);
        return arguments;
    }

    /**
     * @inheritDoc
     * @see ResourcesRequest#
     * getMethod()
     */
    @Override
    protected String getMethod() {
        return "act";
    }

}
