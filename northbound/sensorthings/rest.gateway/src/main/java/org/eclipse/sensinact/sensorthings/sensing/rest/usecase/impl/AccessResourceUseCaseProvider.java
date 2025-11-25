package org.eclipse.sensinact.sensorthings.sensing.rest.usecase.impl;

import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessResourceUseCase;
import org.osgi.service.component.annotations.Component;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.ContextResolver;

@Component(service = ContextResolver.class, immediate = true)
public class AccessResourceUseCaseProvider implements ContextResolver<IAccessResourceUseCase> {

    @Context
    Application application;

    @Override
    public IAccessResourceUseCase getContext(Class<?> type) {
        return (IAccessResourceUseCase) application.getProperties().get("access.resource.usecase");
    }

}
