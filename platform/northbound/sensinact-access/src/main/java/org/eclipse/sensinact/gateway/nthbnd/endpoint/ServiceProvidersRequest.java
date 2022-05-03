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

public class ServiceProvidersRequest extends NorthboundRequest {
    public ServiceProvidersRequest(String requestIdentifier, FilteringCollection filteringCollection) {
        super(requestIdentifier, filteringCollection);
    }
    
    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getPath() {
        return new StringBuilder().append(super.getPath()).append(UriUtils.PATH_SEPARATOR).append("providers").toString();
    }

    @Override
    protected Argument[] getExecutionArguments() {
        Argument[] superArguments = super.getExecutionArguments();
        int length = superArguments == null ? 0 : superArguments.length;
        if (this.getClass() == ServiceProvidersRequest.class && super.filteringCollection != null) {
            Argument[] arguments = new Argument[length + 1];
            if (length > 0) 
                System.arraycopy(superArguments, 0, arguments, 0, length);            
            arguments[length] = new Argument(FilteringCollection.class, super.filteringCollection);
            return arguments;
        }
        return superArguments;
    }

    @Override
    protected String getMethod() {
        return "serviceProvidersList";
    }

}
