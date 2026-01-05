package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
@Component(service = ContextResolver.class)
public class ExtraUseCasesProvider implements ContextResolver<Map<String, IExtraUseCase<?>>> {

    private Map<String, IExtraUseCase<?>> mapExtraUseCase = new HashMap<String, IExtraUseCase<?>>();

    @Reference(service = IExtraUseCase.class, cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    void bindMyService(IExtraUseCase<?> useCase) {
        mapExtraUseCase.put(useCase.getType().toString(), useCase);
    }

    void unbindMyService(IExtraUseCase<?> useCase) {
        mapExtraUseCase.remove(useCase.getType().toString());
    }

    @Override
    public Map<String, IExtraUseCase<?>> getContext(Class<?> type) {
        return mapExtraUseCase;
    }

}
