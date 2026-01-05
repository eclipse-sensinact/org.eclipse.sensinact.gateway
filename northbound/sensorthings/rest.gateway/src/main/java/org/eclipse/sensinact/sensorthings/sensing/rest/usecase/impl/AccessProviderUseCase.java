package org.eclipse.sensinact.sensorthings.sensing.rest.usecase.impl;

import java.util.EnumSet;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin.SnapshotOption;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.osgi.service.component.annotations.Component;

@Component(service = IAccessProviderUseCase.class, immediate = true)
public class AccessProviderUseCase implements IAccessProviderUseCase {

    private Optional<ProviderSnapshot> getProviderSnapshot(SensiNactSession session, String id) {
        return Optional.ofNullable(session.providerSnapshot(id, EnumSet.noneOf(SnapshotOption.class)));
    }

    @Override
    public ProviderSnapshot read(SensiNactSession session, String providerId) {

        Optional<ProviderSnapshot> providerSnapshot = getProviderSnapshot(session, providerId);
        if (providerSnapshot.isEmpty()) {
            return null;
        }
        return providerSnapshot.get();
    }

}
