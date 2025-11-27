package org.eclipse.sensinact.sensorthings.sensing.rest;

import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

public interface IExtraDelegate {

    public <D extends Id> Response create(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, D dto,
            Class<D> clazz);

    public <D extends Id> Response delete(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String id,
            Class<D> clazz);

    public <D extends Id> Response update(SensiNactSession session, ObjectMapper mapper, UriInfo uriInfo, String id,
            D dto, Class<D> clazz);
}
