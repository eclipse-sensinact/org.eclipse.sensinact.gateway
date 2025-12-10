package org.eclipse.sensinact.sensorthings.sensing.rest.usecase.impl;

import java.util.Optional;

import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.extended.ModelToDTO;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = IAccessProviderUseCase.class, immediate = true)
public class AccessProviderUseCase implements IAccessProviderUseCase {

    @Reference
    GatewayThread thread;

    @Override
    public ProviderSnapshot read(SensiNactSession session, String providerId) {

        Optional<ProviderSnapshot> providerSnapshot = ModelToDTO.getProviderSnapshot(session, providerId);
        if (providerSnapshot.isEmpty()) {
            return null;
        }
        return providerSnapshot.get();
    }

}
