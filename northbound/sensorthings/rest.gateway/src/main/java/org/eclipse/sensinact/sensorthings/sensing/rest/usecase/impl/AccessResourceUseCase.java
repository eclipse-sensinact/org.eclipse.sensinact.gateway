package org.eclipse.sensinact.sensorthings.sensing.rest.usecase.impl;

import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapper.extractFirstIdSegment;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessResourceUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import jakarta.ws.rs.NotFoundException;

@Component(service = IAccessResourceUseCase.class)
public class AccessResourceUseCase implements IAccessResourceUseCase {

    @Reference
    IAccessProviderUseCase accessProviderUserCase;

    private ProviderSnapshot validateAndGetProvider(SensiNactSession session, String providerId) {
        return accessProviderUserCase.execute(session, providerId);
    }

    @Override
    public ResourceSnapshot execute(SensiNactSession session, String id) {
        String provider = extractFirstIdSegment(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(session, provider);

        String service = extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));

        ResourceSnapshot resourceSnapshot = providerSnapshot.getResource(service, resource);

        if (resourceSnapshot == null) {
            throw new NotFoundException();
        }
        return resourceSnapshot;
    }

    @Override
    public boolean exists(SensiNactSession session, String id) {
        // TODO Auto-generated method stub
        return false;
    }

}
