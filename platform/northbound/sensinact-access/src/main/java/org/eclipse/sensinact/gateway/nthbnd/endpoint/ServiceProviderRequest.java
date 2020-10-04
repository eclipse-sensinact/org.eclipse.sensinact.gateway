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

import org.eclipse.sensinact.gateway.core.FilteringCollection;
import org.eclipse.sensinact.gateway.util.UriUtils;

public class ServiceProviderRequest extends ServiceProvidersRequest {
    private String serviceProvider;

    public ServiceProviderRequest(NorthboundMediator mediator, String requestIdentifier, String serviceProvider, FilteringCollection filteringCollection) {
        super(mediator, requestIdentifier, filteringCollection);
        this.serviceProvider = serviceProvider;
//      if (this.serviceProvider == null) {
//      throw new NullPointerException("ServiceProvider missing");
//  }
    }

    /**
     * @inheritDoc
     * @see ServiceProvidersRequest#
     * getName()
     */
    public String getName() {
        return serviceProvider;
    }

    /**
     * @inheritDoc
     * @see ServiceProvidersRequest#
     * getPath()
     */
    @Override
    public String getPath() {
        return new StringBuilder().append(super.getPath()).append(UriUtils.PATH_SEPARATOR).append(this.getName()).toString();
    }

    /**
     * @inheritDoc
     * @see ServiceProvidersRequest#
     * getMethod()
     */
    @Override
    protected String getMethod() {
        return "serviceProviderDescription";
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
        arguments[length] = new Argument(String.class, serviceProvider);
        return arguments;
    }
}
