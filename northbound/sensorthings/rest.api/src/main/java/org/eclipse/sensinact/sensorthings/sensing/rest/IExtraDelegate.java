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

    public <D extends Id, S> S create(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String method,
            D dto, String parentId);

    public <D extends Id, S> S create(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String method,
            D dto);

    public <D extends Id, S> S delete(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String method,
            String id, Class<D> clazz);

    public <D extends Id, S> S update(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String method,
            String id, D dto);

    public <D extends Id, S> S update(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String method,
            String id, D dto, String parentId);

    public <S> S updateRef(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String method, RefId dto,
            String parentId, Class<? extends Id> clazzUseCase, Class<? extends Id> clazzRef);

    public <S> S createRef(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String method, RefId dto,
            String parentId, Class<? extends Id> clazzUseCase, Class<? extends Id> clazzRef);
}
