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
package org.eclipse.sensinact.sensorthings.sensing.rest;

import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.core.UriInfo;

public interface IExtraDelegate {
    /**
     * create sensorthing model
     *
     * @param <D>
     * @param <S>
     * @param session
     * @param mapper
     * @param uriInfo
     * @param method
     * @param dto
     * @param parentId
     * @return
     */
    public <D extends Id, S> S create(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String method,
            D dto, String parentId);

    /**
     * create sensorthing model
     *
     * @param <D>
     * @param <S>
     * @param session
     * @param mapper
     * @param uriInfo
     * @param method
     * @param dto
     * @return
     */
    public <D extends Id, S> S create(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String method,
            D dto);

    /**
     * delete sensorthing model
     *
     * @param <D>
     * @param <S>
     * @param session
     * @param mapper
     * @param uriInfo
     * @param method
     * @param id
     * @param clazz
     * @return
     */
    public <D extends Id, S> S delete(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String id,
            Class<D> clazz);

    /**
     * delete the ref between 2 entities
     *
     * @param <D>
     * @param <S>
     * @param session
     * @param mapper
     * @param uriInfo
     * @param id
     * @param refId
     * @param clazz
     * @return
     */
    public <S> S deleteRef(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String id, String parentId,
            Class<? extends Id> clazzUseCase, Class<? extends Id> clazzRef);

    /**
     * delete the ref between 2 entities in parameter
     *
     * @param <S>
     * @param session
     * @param mapper
     * @param uriInfo
     * @param parentId
     * @param clazzUseCase
     * @param clazzRef
     * @return
     */
    public <S> S deleteRef(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String parentId,
            Class<? extends Id> clazzUseCase, Class<? extends Id> clazzRef);

    /**
     * update sensorthing model
     *
     * @param <D>
     * @param <S>
     * @param session
     * @param mapper
     * @param uriInfo
     * @param method
     * @param id
     * @param dto
     * @return
     */
    public <D extends Id, S> S update(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String method,
            String id, D dto);

    /**
     * update sensorthing model
     *
     * @param <D>
     * @param <S>
     * @param session
     * @param mapper
     * @param uriInfo
     * @param method
     * @param id
     * @param dto
     * @param parentId
     * @return
     */
    public <D extends Id, S> S update(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String method,
            String id, D dto, String parentId);

    /**
     * update reference between 2 sensorthing model
     *
     * @param <S>
     * @param session
     * @param mapper
     * @param uriInfo
     * @param method
     * @param dto
     * @param parentId
     * @param clazzUseCase
     * @param clazzRef
     * @return
     */
    public <S> S updateRef(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String method, RefId dto,
            String parentId, Class<? extends Id> clazzUseCase, Class<? extends Id> clazzRef);

    /**
     * create ref between 2 sensorthing model
     *
     * @param <S>
     * @param session
     * @param mapper
     * @param uriInfo
     * @param method
     * @param dto
     * @param parentId
     * @param clazzUseCase
     * @param clazzRef
     * @return
     */
    public <S> S createRef(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String method, RefId dto,
            String parentId, Class<? extends Id> clazzUseCase, Class<? extends Id> clazzRef);
}
