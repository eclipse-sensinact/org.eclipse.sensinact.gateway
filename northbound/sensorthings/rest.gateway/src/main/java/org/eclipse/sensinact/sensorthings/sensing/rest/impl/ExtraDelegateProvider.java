/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.sensorthings.sensing.rest.impl;

import org.eclipse.sensinact.sensorthings.sensing.rest.IExtraDelegate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsApplicationSelect;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.ContextResolver;

/**
 * Provides a suitable ObjectMapper for JSON serialization
 */
@JakartarsApplicationSelect("(osgi.jakartars.name=sensorthings)")
@Component(service = ContextResolver.class, immediate = true, property = { "osgi.jakartars.resource=true" })
public class ExtraDelegateProvider implements ContextResolver<IExtraDelegate> {

    @Context
    Application application;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    private volatile IExtraDelegate extraDelegate;

    protected void bindExtraDelegate(IExtraDelegate delegate) {
        this.extraDelegate = delegate;
    }

    @Override
    public IExtraDelegate getContext(Class<?> type) {
        return (IExtraDelegate) extraDelegate;
    }

    protected void unbindExtraDelegate(IExtraDelegate delegate) {
        if (this.extraDelegate == delegate) {
            this.extraDelegate = null;
        }
    }
}
