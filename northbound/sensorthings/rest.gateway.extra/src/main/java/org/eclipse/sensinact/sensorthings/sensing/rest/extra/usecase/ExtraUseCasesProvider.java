package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.ExtraUseCasesProvider.ExtraRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsApplicationSelect;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.ContextResolver;

@JakartarsApplicationSelect("(osgi.jakartars.name=sensorthings)")
@Component(service = ContextResolver.class, immediate = true, property = { "osgi.jakartars.resource=true" })
public class ExtraUseCasesProvider implements ContextResolver<ExtraRegistry> {

    public class ExtraRegistry {
        private final Map<String, IExtraUseCase<?>> map = new HashMap<>();

        public Map<String, IExtraUseCase<?>> getMap() {
            return map;
        }
    }

    @Context
    Application application;

    private ExtraRegistry useCases = new ExtraRegistry();

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, bind = "bindExtraUseCase", unbind = "unbindExtraUseCase")
    public void bindExtraUseCase(IExtraUseCase useCase) {
        useCases.map.put(useCase.getType().getName(), useCase);
    }

    public void unbindExtraUseCase(IExtraUseCase<?> useCase) {
        useCases.map.remove(useCase.getType().getName());
    }

    @Override
    public ExtraRegistry getContext(Class<?> type) {
        return useCases;
    }

}
