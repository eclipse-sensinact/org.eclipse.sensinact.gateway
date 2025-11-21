package org.eclipse.sensinact.sensorthings.sensing.rest.usecase.impl;

import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsApplicationSelect;

import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
@Component(service = ContextResolver.class)
@JakartarsApplicationSelect(value = "sensorthings")
public class AccessProviderUseCaseProvider implements ContextResolver<IAccessProviderUseCase> {

    @Reference
    IAccessProviderUseCase providerUserCase;

    @Override
    public IAccessProviderUseCase getContext(Class<?> type) {
        return providerUserCase;
    }

}
