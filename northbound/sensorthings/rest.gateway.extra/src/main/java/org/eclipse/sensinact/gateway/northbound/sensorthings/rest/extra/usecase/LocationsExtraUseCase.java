package org.eclipse.sensinact.gateway.northbound.sensorthings.rest.extra.usecase;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsApplicationSelect;
import org.eclipse.emf.ecore.EClass;

import jakarta.ws.rs.core.Response;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * object
 */
@Component
@JakartarsApplicationSelect(value = "sensorthings")
public class LocationsExtraUseCase {

    @Reference
    DataUpdate dataUpdate;

    @Reference
    IAccessProviderUseCase providerUseCase;

    public Response create(SensiNactSession session, Location dto) {
        try {
            String providerId = dto.thingsLink;
            // check if provider exists
            providerUseCase.execute(session, providerId);
            // call create
            
            dataUpdate.pushUpdate(dto);

            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(500).build(); // TODO
        }
    }
    
    
    protected record LocationUpdate()
    
}
