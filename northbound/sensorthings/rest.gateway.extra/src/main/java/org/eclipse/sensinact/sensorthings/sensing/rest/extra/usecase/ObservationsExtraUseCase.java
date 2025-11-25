package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessResourceUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import jakarta.ws.rs.core.UriInfo;

/**
 * UseCase that manage the create, update, delete use case for Observationthing
 * object
 */
@Component(service = IExtraUseCase.class)
public class ObservationsExtraUseCase extends AbstractExtraUseCase<Observation> {

    @Reference
    DataUpdate dataUpdate;

    @Reference
    IAccessResourceUseCase resourceUseCase;

    @Override
    public ExtraUseCaseResponse<Observation> create(SensiNactSession session, UriInfo urlInfo, Observation dto) {
        return new ExtraUseCaseResponse<Observation>(false, "not implemented");

    }

    @Override
    public ExtraUseCaseResponse<Observation> update(SensiNactSession session, UriInfo urlInfo, String id,
            Observation dto) {
        return new ExtraUseCaseResponse<Observation>(false, "not implemented");

    }

    @Override
    public ExtraUseCaseResponse<Observation> delete(SensiNactSession session, UriInfo urlInfo, String id) {
        // TODO Auto-generated method stub
        return new ExtraUseCaseResponse<Observation>(false, "not implemented");
    }

    @Override
    public ExtraUseCaseResponse<Observation> patch(SensiNactSession session, UriInfo urlInfo, String id,
            Observation dto) {
        // TODO Auto-generated method stub
        return new ExtraUseCaseResponse<Observation>(false, "not implemented");
    }
}
