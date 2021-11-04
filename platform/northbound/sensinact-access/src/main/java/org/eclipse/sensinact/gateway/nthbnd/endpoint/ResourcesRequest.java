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

import org.eclipse.sensinact.gateway.core.filtering.FilteringCollection;
import org.eclipse.sensinact.gateway.util.UriUtils;

public class ResourcesRequest extends ServiceRequest {
    /**
     * @param responseFormat
     * @param serviceProvider
     * @param service
     * @param filteringCollection
     */
    public ResourcesRequest(String requestIdentifier, String serviceProvider, String service, FilteringCollection filteringCollection) {
        super(requestIdentifier, serviceProvider, service, filteringCollection);
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getPath() {
        return new StringBuilder().append(super.getPath()).append(UriUtils.PATH_SEPARATOR).append("resources").toString();
    }

    @Override
    protected String getMethod() {
        return "resourcesList";
    }

    @Override
    protected Argument[] getExecutionArguments() {
        if (this.getClass() == ResourcesRequest.class && super.filteringCollection != null) {
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
