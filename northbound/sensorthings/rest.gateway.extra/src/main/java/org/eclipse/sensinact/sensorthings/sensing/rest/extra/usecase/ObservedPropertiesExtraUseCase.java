package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessResourceUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import jakarta.ws.rs.core.UriInfo;

/**
 * UseCase that manage the create, update, delete use case for
 * ObservedPropertything object
 */
@Component(service = IExtraUseCase.class)
public class ObservedPropertiesExtraUseCase extends AbstractExtraUseCase<ObservedProperty> {

    @Reference
    DataUpdate dataUpdate;

    @Reference
    IAccessResourceUseCase resourceUseCase;

    @Override
    public ExtraUseCaseResponse<ObservedProperty> create(SensiNactSession session, UriInfo urlInfo,
            ObservedProperty dto) {
        return new ExtraUseCaseResponse<ObservedProperty>(false, "not implemented");

    }

    @Override
    public ExtraUseCaseResponse<ObservedProperty> update(SensiNactSession session, UriInfo urlInfo, String id,
            ObservedProperty dto) {
        return new ExtraUseCaseResponse<ObservedProperty>(false, "not implemented");

    }

    @Override
    public ExtraUseCaseResponse<ObservedProperty> delete(SensiNactSession session, UriInfo urlInfo, String id) {
        // TODO Auto-generated method stub
        return new ExtraUseCaseResponse<ObservedProperty>(false, "not implemented");
    }

    @Override
    public ExtraUseCaseResponse<ObservedProperty> patch(SensiNactSession session, UriInfo urlInfo, String id,
            ObservedProperty dto) {
        // TODO Auto-generated method stub
        return new ExtraUseCaseResponse<ObservedProperty>(false, "not implemented");
    }
}
