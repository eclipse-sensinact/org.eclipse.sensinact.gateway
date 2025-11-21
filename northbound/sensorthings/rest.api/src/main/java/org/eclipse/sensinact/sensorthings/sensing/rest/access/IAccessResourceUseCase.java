package org.eclipse.sensinact.sensorthings.sensing.rest.access;

import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.northbound.session.SensiNactSession;

public interface IAccessResourceUseCase {

    public ResourceSnapshot execute(SensiNactSession session, String id);
}
