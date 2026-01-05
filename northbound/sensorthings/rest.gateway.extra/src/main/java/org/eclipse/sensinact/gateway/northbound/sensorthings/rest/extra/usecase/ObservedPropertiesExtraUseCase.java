package org.eclipse.sensinact.gateway.northbound.sensorthings.rest.extra.usecase;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessResourceUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsApplicationSelect;

import jakarta.ws.rs.core.Response;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * object
 */
@Component
@JakartarsApplicationSelect(value = "sensorthings")
public class ObservedPropertiesExtraUseCase {

    @Reference
    DataUpdate dataUpdate;

    @Reference
    IAccessResourceUseCase resourceUseCase;

    public Response create(SensiNactSession session, ObservedProperty dto) {

        // call update
        dataUpdate.pushUpdate(dto);

        return Response.ok().build();
    }
}
