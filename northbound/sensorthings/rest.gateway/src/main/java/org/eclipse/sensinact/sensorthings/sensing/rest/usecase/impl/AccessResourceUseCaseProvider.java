package org.eclipse.sensinact.sensorthings.sensing.rest.usecase.impl;

import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessResourceUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
@Component(service = ContextResolver.class, immediate = true)
public class AccessResourceUseCaseProvider implements ContextResolver<IAccessResourceUseCase> {

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC)
    IAccessResourceUseCase resourceUseCase;

    @Override
    public IAccessResourceUseCase getContext(Class<?> type) {
        return resourceUseCase;
    }

}
