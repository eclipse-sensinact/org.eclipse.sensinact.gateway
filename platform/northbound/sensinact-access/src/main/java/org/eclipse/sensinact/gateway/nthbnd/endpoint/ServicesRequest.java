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

import org.eclipse.sensinact.gateway.core.filtering.FilteringCollection;
import org.eclipse.sensinact.gateway.util.UriUtils;

public class ServicesRequest extends ServiceProviderRequest {
    public ServicesRequest(String requestIdentifier, String serviceProvider, FilteringCollection filteringCollection) {
        super(requestIdentifier, serviceProvider, filteringCollection);
    }

    /**
     * @inheritDoc
     * @see ServiceProviderRequest#getName()
     */
    public String getName() {
        return null;
    }

    /**
     * @inheritDoc
     * @see ServiceProviderRequest#getPath()
     */
    @Override
    public String getPath() {
        return new StringBuilder().append(super.getPath()).append(UriUtils.PATH_SEPARATOR).append("services").toString();
    }

    /**
     * @inheritDoc
     * @see ServiceProvidersRequest#getMethod()
     */
    @Override
    protected String getMethod() {
        return "servicesList";
    }

    /**
     * @inheritDoc
     * @see NorthboundRequest#getExecutionArguments()
     */
    @Override
    protected Argument[] getExecutionArguments() {
        if (this.getClass() == ServicesRequest.class && super.filteringCollection != null) {
            Argument[] superArguments = super.getExecutionArguments();
            int length = superArguments == null ? 0 : superArguments.length;
            Argument[] arguments = new Argument[length + 1];
            if (length > 0) 
                System.arraycopy(superArguments, 0, arguments, 0, length);
            arguments[length] = new Argument(FilteringCollection.class, super.filteringCollection);
            return arguments;
        }
        return super.getExecutionArguments();
    }
}
