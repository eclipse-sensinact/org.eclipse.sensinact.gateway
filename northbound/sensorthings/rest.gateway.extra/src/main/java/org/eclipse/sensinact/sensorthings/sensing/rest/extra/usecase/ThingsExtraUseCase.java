package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
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
public class ThingsExtraUseCase {

    @Reference
    DataUpdate dataUpdate;

    @Reference
    IAccessProviderUseCase providerUseCase;

    public Response create(SensiNactSession session, Thing dto) {
        try {

            providerUseCase.execute(session, (String) dto.id);

            // call create
            dataUpdate.pushUpdate(null); // TODO

            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(500).build(); // TODO
        }
    }

}
