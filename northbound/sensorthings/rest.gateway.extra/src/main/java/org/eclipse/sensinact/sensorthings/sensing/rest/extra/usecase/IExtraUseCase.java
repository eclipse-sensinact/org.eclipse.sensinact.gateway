/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.util.UUID;

import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.DtoToModelMapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.core.UriInfo;

public interface IExtraUseCase<M extends Id, S> {

    /**
     * generic class for use case response
     *
     * @param <S>
     */
    public record ExtraUseCaseResponse<S>(String id, S snapshot, boolean success, RuntimeException e, String message) {
        public ExtraUseCaseResponse(boolean success, RuntimeException e, String message) {
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

    /**
     * generic class for use case request
     *
     * @param <M>
     */
    public record ExtraUseCaseRequest<M extends Id>(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo,
            String method, String id, M model, String parentId, Class<? extends Id> clazzModel,
            Class<? extends Id> clazzRef) {

        public ExtraUseCaseRequest(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String method,
                String id) {
            this(session, mapper, uriInfo, method, id, null, null, null, null);
        }

        public ExtraUseCaseRequest(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String method,
                M model, String parentId) {
            this(session, mapper, uriInfo, method, null, model, parentId, null, null);
        }

        public ExtraUseCaseRequest(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String method,
                M model, String parentId, Class<? extends Id> clazzModel, Class<? extends Id> clazzRef) {

            this(session, mapper, uriInfo, method, null, model, parentId, clazzModel, clazzRef);
        }

        public ExtraUseCaseRequest(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String method,
                String id, M model, String parentId) {

            this(session, mapper, uriInfo, method, id, model, parentId, null, null);
        }

        public ExtraUseCaseRequest(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String method,
                M model) {
            this(session, mapper, uriInfo, method, null, model, null, null, null);
        }

        public ExtraUseCaseRequest {
            if (id == null) {
                id = DtoToModelMapper.sanitizeId(model.id() != null ? model.id() : UUID.randomUUID().toString());
            }
        }

    }

    /**
     * create an EMF model (provider/service/resouces) for the corresponding DTO
     *
     * @param request
     * @return
     */
    public ExtraUseCaseResponse<S> create(ExtraUseCaseRequest<M> request);

    /**
     * delete the EMF model (provider/service/resouces) for the corresponding DTO
     *
     * @param request
     * @return
     */
    public ExtraUseCaseResponse<S> delete(ExtraUseCaseRequest<M> request);

    public Class<M> getType();

    /**
     * update(put) the EMF model (provider/service/resouces) for the corresponding
     * DTO
     *
     * @param request
     * @return
     */
    public ExtraUseCaseResponse<S> update(ExtraUseCaseRequest<M> request);
}
