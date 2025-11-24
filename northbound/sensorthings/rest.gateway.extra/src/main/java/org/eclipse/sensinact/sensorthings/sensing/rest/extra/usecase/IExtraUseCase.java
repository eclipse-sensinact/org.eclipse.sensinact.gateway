package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;

public interface IExtraUseCase<D extends Id> {
    public boolean create(SensiNactSession session, D dto);

    public boolean update(SensiNactSession session, String id, D dto);

    public boolean delete(SensiNactSession session, String id);

    public boolean patch(SensiNactSession session, String id, D dto);

    public Class<D> getType();

}
