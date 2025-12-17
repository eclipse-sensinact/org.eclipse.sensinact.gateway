/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.sensorthings.sensing.rest.usecase.impl;

import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessServiceUseCase;
import org.osgi.service.component.annotations.Component;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.ContextResolver;

@Component(service = ContextResolver.class, immediate = true)
public class AccessServiceUseCaseProvider implements ContextResolver<IAccessServiceUseCase> {

    @Context
    Application application;

    @Override
    public IAccessServiceUseCase getContext(Class<?> type) {
        return (IAccessServiceUseCase) application.getProperties().get("access.service.usecase");
    }

}
