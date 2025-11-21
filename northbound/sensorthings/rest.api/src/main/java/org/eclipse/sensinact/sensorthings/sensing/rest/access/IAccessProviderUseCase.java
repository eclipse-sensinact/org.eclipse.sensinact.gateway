package org.eclipse.sensinact.sensorthings.sensing.rest.access;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.northbound.session.SensiNactSession;

public interface IAccessProviderUseCase {

    public ProviderSnapshot execute(SensiNactSession sessin, String providerId);
}
