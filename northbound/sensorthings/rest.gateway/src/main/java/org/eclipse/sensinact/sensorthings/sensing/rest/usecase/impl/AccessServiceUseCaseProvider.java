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
