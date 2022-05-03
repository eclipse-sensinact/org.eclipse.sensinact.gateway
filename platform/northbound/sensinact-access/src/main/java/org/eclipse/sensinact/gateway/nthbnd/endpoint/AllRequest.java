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

public class AllRequest extends NorthboundRequest {
    /**
     * @param requestIdentifier
     * @param filteringCollection
     */
    public AllRequest(String requestIdentifier, FilteringCollection filteringCollection) {
        super(requestIdentifier, filteringCollection);
    }

    @Override
    public String getName() {
        return "all";
    }

    @Override
    protected String getMethod() {
        return this.getName();
    }

    @Override
    protected Argument[] getExecutionArguments() {
        Argument[] superArguments = super.getExecutionArguments();
        int length = superArguments == null ? 0 : superArguments.length;
        Argument[] arguments = new Argument[length + 2];
        if (length > 0) {
            System.arraycopy(superArguments, 0, arguments, 0, length);
        }
        arguments[length] = new Argument(String.class, null);
        arguments[length + 1] = new Argument(FilteringCollection.class, super.filteringCollection);
        return arguments;
    }
}
