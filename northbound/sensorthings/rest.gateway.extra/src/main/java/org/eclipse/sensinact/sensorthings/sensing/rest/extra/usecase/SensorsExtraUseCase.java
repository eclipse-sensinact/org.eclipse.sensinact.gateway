package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessResourceUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import jakarta.ws.rs.core.UriInfo;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * object
 */
@Component(service = IExtraUseCase.class)
public class SensorsExtraUseCase extends AbstractExtraUseCase<Sensor> {

    @Reference
    DataUpdate dataUpdate;

    @Reference
    IAccessResourceUseCase resourceUseCase;

    @Override
    public ExtraUseCaseResponse<Sensor> create(SensiNactSession session, UriInfo urlInfo, Sensor dto) {
        return new ExtraUseCaseResponse<Sensor>(false, "not implemented");

    }

    @Override
    public ExtraUseCaseResponse<Sensor> update(SensiNactSession session, UriInfo urlInfo, String id, Sensor dto) {
        return new ExtraUseCaseResponse<Sensor>(false, "not implemented");

    }

    @Override
    public ExtraUseCaseResponse<Sensor> delete(SensiNactSession session, UriInfo urlInfo, String id) {
        // TODO Auto-generated method stub
        return new ExtraUseCaseResponse<Sensor>(false, "not implemented");
    }

    @Override
    public ExtraUseCaseResponse<Sensor> patch(SensiNactSession session, UriInfo urlInfo, String id, Sensor dto) {
        // TODO Auto-generated method stub
        return new ExtraUseCaseResponse<Sensor>(false, "not implemented");
    }
}
