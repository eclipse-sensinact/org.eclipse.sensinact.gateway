package org.eclipse.sensinact.sensorthings.sensing.rest.usecase.impl;

import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessResourceUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsApplicationSelect;

import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
@Component(service = ContextResolver.class)
@JakartarsApplicationSelect(value = "sensorthings")
public class AccessResourceUseCaseProvider implements ContextResolver<IAccessResourceUseCase> {

    @Reference
    IAccessResourceUseCase resourceUseCase;

    @Override
    public IAccessResourceUseCase getContext(Class<?> type) {
        return resourceUseCase;
    }

}
