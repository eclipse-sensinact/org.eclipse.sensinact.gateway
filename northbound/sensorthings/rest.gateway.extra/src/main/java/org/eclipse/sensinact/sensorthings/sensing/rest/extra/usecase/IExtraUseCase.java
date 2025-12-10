package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import org.eclipse.sensinact.core.snapshot.Snapshot;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.core.UriInfo;

public interface IExtraUseCase<M extends Id, S extends Snapshot> {

    public record ExtraUseCaseResponse<S>(String id, S snapshot, boolean success, Throwable e, String message) {
        public ExtraUseCaseResponse(boolean success, Throwable e, String message) {
            this(null, null, success, e, message);
        }

        public ExtraUseCaseResponse(boolean success, String message) {
            this(null, null, success, null, message);
        }

        public ExtraUseCaseResponse(String id, S model) {
            this(id, model, true, null, null);
        }

        public ExtraUseCaseResponse(String id) {
            this(id, null, true, null, null);
        }
    }

    public record ExtraUseCaseRequest<M>(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String id,
            M model, String parentId) {
        public ExtraUseCaseRequest(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String id) {
            this(session, mapper, uriInfo, id, null, null);
        }

        public ExtraUseCaseRequest(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, M model,
                String parentId) {
            this(session, mapper, uriInfo, null, model, parentId);
        }
    }

    public ExtraUseCaseResponse<S> create(ExtraUseCaseRequest<M> request);

    public ExtraUseCaseResponse<S> delete(ExtraUseCaseRequest<M> request);

    public Class<M> getType();

    public ExtraUseCaseResponse<S> patch(ExtraUseCaseRequest<M> request);

    public ExtraUseCaseResponse<S> update(ExtraUseCaseRequest<M> request);
}
