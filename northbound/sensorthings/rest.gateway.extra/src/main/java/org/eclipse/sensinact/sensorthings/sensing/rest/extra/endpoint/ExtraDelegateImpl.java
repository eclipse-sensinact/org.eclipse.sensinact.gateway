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
package org.eclipse.sensinact.sensorthings.sensing.rest.extra.endpoint;

import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;
import org.eclipse.sensinact.sensorthings.sensing.rest.IExtraDelegate;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.IExtraUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.IExtraUseCase.ExtraUseCaseRequest;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.IExtraUseCase.ExtraUseCaseResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Providers;

/**
 * service that allow to aggregate use case and delegate http operations for
 * extra (post,put,delete) for endpoint
 */

public class ExtraDelegateImpl implements IExtraDelegate {

    /**
     * Used to access the various Extra Use Case services through a
     * {@link ContextResolver}
     */
    private final Providers providers;

    public ExtraDelegateImpl(Providers providers) {
        this.providers = providers;
    }

    @SuppressWarnings("unchecked")
    public <D extends Id, S> S create(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String method,
            D dto, String parentId) {
        IExtraUseCase<D, S> useCase = (IExtraUseCase<D, S>) getExtraUseCase(dto.getClass());

        ExtraUseCaseRequest<D> request = new ExtraUseCaseRequest<D>(session, mapper, uriInfo, method, dto, parentId);
        ExtraUseCaseResponse<S> result = useCase.create(request);
        if (!result.success()) {
            throw new UnsupportedOperationException(result.message(), result.e());
        }
        return result.snapshot();

    }

    public <D extends Id, S> S create(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String method,
            D dto) {
        return create(session, mapper, uriInfo, method, dto, null);
    }

    @SuppressWarnings("unchecked")
    public <D extends Id, S> S delete(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String method,
            String id, Class<D> clazz) {
        IExtraUseCase<D, S> useCase = (IExtraUseCase<D, S>) getExtraUseCase(clazz);
        ExtraUseCaseRequest<D> request = new ExtraUseCaseRequest<D>(session, mapper, uriInfo, method, id);
        ExtraUseCaseResponse<S> result = useCase.create(request);
        if (!result.success()) {
            throw result.e();
        }
        return result.snapshot();
    }

    @SuppressWarnings("unchecked")
    protected <D extends Id, S> IExtraUseCase<D, S> getExtraUseCase(Class<D> clazz) {
        return providers.getContextResolver(IExtraUseCase.class, MediaType.WILDCARD_TYPE).getContext(clazz);
    }

    @SuppressWarnings("unchecked")
    public <D extends Id, S> S update(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String method,
            String id, D dto, String parentId) {
        IExtraUseCase<D, S> useCase = (IExtraUseCase<D, S>) getExtraUseCase(dto.getClass());
        ExtraUseCaseRequest<D> request = new ExtraUseCaseRequest<D>(session, mapper, uriInfo, method, id, dto,
                parentId);
        ExtraUseCaseResponse<S> result = useCase.update(request);
        if (!result.success()) {
            throw new UnsupportedOperationException(result.message(), result.e());
        }
        return result.snapshot();
    }

    @Override
    public <D extends Id, S> S update(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String method,
            String id, D dto) {
        return update(session, mapper, uriInfo, method, id, dto, null);
    }

    @SuppressWarnings("unchecked")

    @Override
    public <S> S updateRef(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String method, RefId dto,
            String parentId, Class<? extends Id> clazzModel, Class<? extends Id> clazzRef) {
        IExtraUseCase<RefId, S> useCase = (IExtraUseCase<RefId, S>) getExtraUseCase(dto.getClass());
        ExtraUseCaseRequest<RefId> request = new ExtraUseCaseRequest<RefId>(session, mapper, uriInfo, method, dto,
                parentId, clazzModel, clazzRef);
        ExtraUseCaseResponse<S> result = useCase.update(request);
        if (!result.success()) {
            throw new UnsupportedOperationException(result.message(), result.e());
        }
        return result.snapshot();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S> S createRef(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String method, RefId dto,
            String parentId, Class<? extends Id> clazzModel, Class<? extends Id> clazzRef) {
        IExtraUseCase<RefId, S> useCase = (IExtraUseCase<RefId, S>) getExtraUseCase(dto.getClass());
        ExtraUseCaseRequest<RefId> request = new ExtraUseCaseRequest<RefId>(session, mapper, uriInfo, method, dto,
                parentId, clazzModel, clazzRef);
        ExtraUseCaseResponse<S> result = useCase.create(request);
        if (!result.success()) {
            throw new UnsupportedOperationException(result.message(), result.e());
        }
        return result.snapshot();
    }

}
