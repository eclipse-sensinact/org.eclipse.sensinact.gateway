package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsApplicationSelect;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * object
 */
@Component(service = IExtraUseCase.class, property = { "model.class=Thing" })
@JakartarsApplicationSelect(value = "sensorthings")
public class ThingsExtraUseCase implements IExtraUseCase<Thing> {

    @Reference
    DataUpdate dataUpdate;

    @Reference
    IAccessProviderUseCase providerUseCase;

    public boolean create(SensiNactSession session, Thing dto) {

        ProviderSnapshot providerSnapshot = providerUseCase.read(session, (String) dto.id);
        if (providerSnapshot != null) {
            return false;
        }
        // call create
        dataUpdate.pushUpdate(dto);
        return true;

    }

    public boolean update(SensiNactSession session, String id, Thing dto) {
        ProviderSnapshot providerSnapshot = providerUseCase.read(session, id);
        if (providerSnapshot == null) {
            return false;
        }
        dto.id = providerSnapshot.getName();
        dataUpdate.pushUpdate(dto);
        return true;

    }

    @Override
    public boolean delete(SensiNactSession session, String id) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean patch(SensiNactSession session, String id, Thing dto) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Class<Thing> getType() {
        // TODO Auto-generated method stub
        return null;
    }
}
