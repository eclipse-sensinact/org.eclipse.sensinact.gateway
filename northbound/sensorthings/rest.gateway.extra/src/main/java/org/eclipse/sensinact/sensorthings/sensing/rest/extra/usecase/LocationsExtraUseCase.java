package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessResourceUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import jakarta.ws.rs.core.UriInfo;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * object
 */
@Component(service = IExtraUseCase.class)
public class LocationsExtraUseCase extends AbstractExtraUseCase<Location> {

    @Reference
    DataUpdate dataUpdate;

    @Reference
    IAccessResourceUseCase resourceUseCase;

    @Override
    public ExtraUseCaseResponse<Location> create(SensiNactSession session, UriInfo urlInfo, Location dto) {
        return new ExtraUseCaseResponse<Location>(false, "not implemented");

    }

    @Override
    public ExtraUseCaseResponse<Location> update(SensiNactSession session, UriInfo urlInfo, String id, Location dto) {
        return new ExtraUseCaseResponse<Location>(false, "not implemented");

    }

    @Override
    public ExtraUseCaseResponse<Location> delete(SensiNactSession session, UriInfo urlInfo, String id) {
        // TODO Auto-generated method stub
        return new ExtraUseCaseResponse<Location>(false, "not implemented");
    }

    @Override
    public ExtraUseCaseResponse<Location> patch(SensiNactSession session, UriInfo urlInfo, String id, Location dto) {
        // TODO Auto-generated method stub
        return new ExtraUseCaseResponse<Location>(false, "not implemented");
    }
}
