package org.eclipse.sensinact.sensorthings.sensing.rest.usecase.impl;

import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AccessProviderUseCaseProvider implements ContextResolver<IAccessProviderUseCase> {

    @Context
    Application application;

    @Override
    public IAccessProviderUseCase getContext(Class<?> type) {
        return (IAccessProviderUseCase) application.getProperties().get("access.provider.usecase");
    }

}
