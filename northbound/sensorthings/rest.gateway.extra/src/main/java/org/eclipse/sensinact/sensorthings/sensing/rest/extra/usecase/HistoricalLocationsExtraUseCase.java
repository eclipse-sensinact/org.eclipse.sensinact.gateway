package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessResourceUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import jakarta.ws.rs.core.UriInfo;

/**
 * UseCase that manage the create, update, delete use case for
 * HistoricalLocationthing object
 */
@Component(service = IExtraUseCase.class)
public class HistoricalLocationsExtraUseCase extends AbstractExtraUseCase<HistoricalLocation> {

    @Reference
    DataUpdate dataUpdate;

    @Reference
    IAccessResourceUseCase resourceUseCase;

    @Override
    public ExtraUseCaseResponse<HistoricalLocation> create(SensiNactSession session, UriInfo urlInfo,
            HistoricalLocation dto) {
        return new ExtraUseCaseResponse<HistoricalLocation>(false, "not implemented");

    }

    @Override
    public ExtraUseCaseResponse<HistoricalLocation> update(SensiNactSession session, UriInfo urlInfo, String id,
            HistoricalLocation dto) {
        return new ExtraUseCaseResponse<HistoricalLocation>(false, "not implemented");

    }

    @Override
    public ExtraUseCaseResponse<HistoricalLocation> delete(SensiNactSession session, UriInfo urlInfo, String id) {
        // TODO Auto-generated method stub
        return new ExtraUseCaseResponse<HistoricalLocation>(false, "not implemented");
    }

    @Override
    public ExtraUseCaseResponse<HistoricalLocation> patch(SensiNactSession session, UriInfo urlInfo, String id,
            HistoricalLocation dto) {
        // TODO Auto-generated method stub
        return new ExtraUseCaseResponse<HistoricalLocation>(false, "not implemented");
    }
}
