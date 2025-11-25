package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessResourceUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.endpoint.dto.DatastreamExtra;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import jakarta.ws.rs.core.UriInfo;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * object
 */
@Component(service = IExtraUseCase.class)
public class DatastreamsExtraUseCase extends AbstractExtraUseCase<DatastreamExtra> {

    @Reference
    DataUpdate dataUpdate;

    @Reference
    IAccessResourceUseCase resourceUseCase;

    @Override
    public ExtraUseCaseResponse<DatastreamExtra> create(SensiNactSession session, UriInfo urlInfo,
            DatastreamExtra dto) {
        return new ExtraUseCaseResponse<DatastreamExtra>(false, "not implemented");

    }

    @Override
    public ExtraUseCaseResponse<DatastreamExtra> update(SensiNactSession session, UriInfo urlInfo, String id,
            DatastreamExtra dto) {
        return new ExtraUseCaseResponse<DatastreamExtra>(false, "not implemented");

    }

    @Override
    public ExtraUseCaseResponse<DatastreamExtra> delete(SensiNactSession session, UriInfo urlInfo, String id) {
        // TODO Auto-generated method stub
        return new ExtraUseCaseResponse<DatastreamExtra>(false, "not implemented");
    }

    @Override
    public ExtraUseCaseResponse<DatastreamExtra> patch(SensiNactSession session, UriInfo urlInfo, String id,
            DatastreamExtra dto) {
        // TODO Auto-generated method stub
        return new ExtraUseCaseResponse<DatastreamExtra>(false, "not implemented");
    }
}
