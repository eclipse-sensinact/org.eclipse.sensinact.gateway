package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import org.eclipse.sensinact.core.snapshot.Snapshot;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.core.UriInfo;

public interface IExtraUseCase<M extends Id> {

    public record ExtraUseCaseResponse<Snapshot>(String id, Snapshot snapshot, boolean success, String message) {
        public ExtraUseCaseResponse(boolean success, String message) {
            this(null, null, success, message);
        }

        public ExtraUseCaseResponse(String id, Snapshot model) {
            this(id, model, true, null);
        }

        public ExtraUseCaseResponse(String id) {
            this(id, null, true, null);
        }
    }

    public record ExtraUseCaseRequest<M>(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String id,
            M model) {
        public ExtraUseCaseRequest(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String id) {
            this(session, mapper, uriInfo, id, null);
        }

        public ExtraUseCaseRequest(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, M model) {
            this(session, mapper, uriInfo, null, model);
        }
    }

    public ExtraUseCaseResponse<Snapshot> create(ExtraUseCaseRequest<M> request);

    public ExtraUseCaseResponse<Snapshot> delete(ExtraUseCaseRequest<M> request);

    public Class<M> getType();

    public ExtraUseCaseResponse<Snapshot> patch(ExtraUseCaseRequest<M> request);

    public ExtraUseCaseResponse<Snapshot> update(ExtraUseCaseRequest<M> request);
}
