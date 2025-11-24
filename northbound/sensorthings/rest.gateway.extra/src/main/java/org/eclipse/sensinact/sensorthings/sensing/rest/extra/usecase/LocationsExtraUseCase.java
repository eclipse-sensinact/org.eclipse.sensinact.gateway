package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsApplicationSelect;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * object
 */
@Component(service = IExtraUseCase.class, property = {
        "model.class=org.eclipse.sensinact.sensorthings.sensing.dto.Location" })
@JakartarsApplicationSelect(value = "sensorthings")
public class LocationsExtraUseCase extends AbstractExtraUseCase<Location> {

    @Reference
    DataUpdate dataUpdate;

    @Reference
    IAccessProviderUseCase providerUseCase;

    public boolean create(SensiNactSession session, Location dto) {
        String providerId = dto.thingsLink;
        // check if provider exists
        ProviderSnapshot providerSnapshot = providerUseCase.read(session, providerId);
        // call create
        if (providerSnapshot == null) {
            return false;
        }
        dataUpdate.pushUpdate(dto);

        return false;

    }

    @Override
    public boolean update(SensiNactSession session, String id, Location dto) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean delete(SensiNactSession session, String id) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean patch(SensiNactSession session, String id, Location dto) {
        // TODO Auto-generated method stub
        return false;
    }

}
