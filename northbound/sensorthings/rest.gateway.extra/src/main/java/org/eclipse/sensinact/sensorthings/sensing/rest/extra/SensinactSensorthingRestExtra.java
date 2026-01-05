package org.eclipse.sensinact.sensorthings.sensing.rest.extra;

import java.util.Set;

import org.eclipse.sensinact.sensorthings.sensing.rest.extra.ISensinactSensorthingsRestExtra;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsApplicationSelect;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;

@Component(service = ISensinactSensorthingsRestExtra.class)
@JakartarsApplicationSelect(value = "sensorthings")
@JakartarsExtension
public class SensinactSensorthingRestExtra implements ISensinactSensorthingsRestExtra {

    @Override
    public Set<Class<?>> getExtraClasses() {
        return Set.of();
    }

}
