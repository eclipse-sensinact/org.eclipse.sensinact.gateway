package org.eclipse.sensinact.sensorthings.sensing.rest.access;

import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.northbound.session.SensiNactSession;

public interface IAccessServiceUseCase {

    public ServiceSnapshot read(SensiNactSession session, String id);
}
