package org.eclipse.sensinact.sensorthings.sensing.rest;

import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.core.UriInfo;

public interface IExtraDelegate {

    public <D extends Id, S> S create(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, D dto,
            String parentId);

    public <D extends Id, S> S create(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, D dto);

    public <D extends Id, S> S delete(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String id,
            Class<D> clazz);

    public <D extends Id, S> S update(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String id, D dto);

    public <D extends Id, S> S update(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String id, D dto,
            String parentId);
}
