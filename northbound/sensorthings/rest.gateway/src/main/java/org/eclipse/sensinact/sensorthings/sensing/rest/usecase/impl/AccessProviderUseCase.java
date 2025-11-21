package org.eclipse.sensinact.sensorthings.sensing.rest.usecase.impl;

import java.util.EnumSet;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin.SnapshotOption;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapper;
import org.osgi.service.component.annotations.Component;

import jakarta.ws.rs.NotFoundException;

@Component(service = IAccessProviderUseCase.class)
public class AccessProviderUseCase implements IAccessProviderUseCase {

    private Optional<ProviderSnapshot> getProviderSnapshot(SensiNactSession session, String id) {
        return Optional.ofNullable(session.providerSnapshot(id, EnumSet.noneOf(SnapshotOption.class)));
    }

    @Override
    public ProviderSnapshot execute(SensiNactSession session, String providerId) {
        DtoMapper.validatedProviderId(providerId);

        Optional<ProviderSnapshot> providerSnapshot = getProviderSnapshot(session, providerId);

        if (providerSnapshot.isEmpty()) {
            throw new NotFoundException("Unknown provider");
        }
        return providerSnapshot.get();
    }

}
