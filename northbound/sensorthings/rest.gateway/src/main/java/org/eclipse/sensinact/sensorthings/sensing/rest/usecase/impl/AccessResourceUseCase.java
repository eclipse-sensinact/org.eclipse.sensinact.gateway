package org.eclipse.sensinact.sensorthings.sensing.rest.usecase.impl;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessResourceUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapper;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

@Component(service = IAccessResourceUseCase.class, immediate = true)
public class AccessResourceUseCase implements IAccessResourceUseCase {

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    IAccessProviderUseCase accessProviderUserCase;

    @Override
    public ResourceSnapshot read(SensiNactSession session, String id) {
        String providerId = DtoMapper.extractFirstIdSegment(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(session, providerId);

        String service = DtoMapper.extractFirstIdSegment(id.substring(providerId.length() + 1));
        String resource = DtoMapper.extractFirstIdSegment(id.substring(providerId.length() + service.length() + 2));

        ResourceSnapshot resourceSnapshot = providerSnapshot.getResource(service, resource);

        return resourceSnapshot;
    }

    private ProviderSnapshot validateAndGetProvider(SensiNactSession session, String providerId) {
        return accessProviderUserCase.read(session, providerId);
    }

}
