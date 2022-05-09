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

import org.eclipse.sensinact.gateway.util.UriUtils;

public class ResourceRequest extends ResourcesRequest {
    private String resource;

    /**
     * @param responseFormat
     * @param serviceProvider
     * @param service
     */
    public ResourceRequest(String requestIdentifier, String serviceProvider, String service, String resource) {
        super(requestIdentifier, serviceProvider, service, null);
        this.resource = resource;
    }

    public String getName() {
        return resource;
    }

    @Override
    public String getPath() {
        return new StringBuilder().append(super.getPath()).append(UriUtils.PATH_SEPARATOR).append(this.getName()).toString();
    }

    @Override
    protected String getMethod() {
        return "resourceDescription";
    }

    @Override
    protected Argument[] getExecutionArguments() {
        Argument[] superArguments = super.getExecutionArguments();
        int length = superArguments == null ? 0 : superArguments.length;
        Argument[] arguments = new Argument[length + 1];
        if (length > 0) {
            System.arraycopy(superArguments, 0, arguments, 0, length);
        }
        arguments[length] = new Argument(String.class, resource);
        return arguments;
    }
}
