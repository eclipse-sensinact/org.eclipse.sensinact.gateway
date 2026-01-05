package org.eclipse.sensinact.sensorthings.sensing.rest.usecase.impl;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessServiceUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapper;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

@Component(service = IAccessServiceUseCase.class, immediate = true)
public class AccessServiceUseCase implements IAccessServiceUseCase {

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    IAccessProviderUseCase accessProviderUserCase;

    @Override
    public ServiceSnapshot read(SensiNactSession session, String id) {
        String providerId = DtoMapper.extractFirstIdSegment(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(session, providerId);

        String service = DtoMapper.extractFirstIdSegment(id.substring(providerId.length() + 1));

        ServiceSnapshot serviceSnapshot = providerSnapshot.getService(service);

        return serviceSnapshot;
    }

    private ProviderSnapshot validateAndGetProvider(SensiNactSession session, String providerId) {
        return accessProviderUserCase.read(session, providerId);
    }

}
