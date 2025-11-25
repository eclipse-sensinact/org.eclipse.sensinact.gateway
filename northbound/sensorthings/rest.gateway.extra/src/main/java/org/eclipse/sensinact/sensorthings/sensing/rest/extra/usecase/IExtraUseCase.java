package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;

import jakarta.ws.rs.core.UriInfo;

public interface IExtraUseCase<D extends Id> {

    public record ExtraUseCaseResponse<D>(String id, D dto, boolean success, String message) {
        public ExtraUseCaseResponse(boolean success, String message) {
            this(null, null, success, message);
        }

        public ExtraUseCaseResponse(String id, D dto) {
            this(id, dto, true, null);
        }

        public ExtraUseCaseResponse(String id) {
            this(id, null, true, null);
        }
    }

    public Class<D> getType();

    public ExtraUseCaseResponse<D> create(SensiNactSession session, UriInfo urlInfo, D dto);

    public ExtraUseCaseResponse<D> update(SensiNactSession session, UriInfo urlInfo, String id, D dto);

    public ExtraUseCaseResponse<D> delete(SensiNactSession session, UriInfo urlInfo, String id);

    public ExtraUseCaseResponse<D> patch(SensiNactSession session, UriInfo urlInfo, String id, D dto);
}
