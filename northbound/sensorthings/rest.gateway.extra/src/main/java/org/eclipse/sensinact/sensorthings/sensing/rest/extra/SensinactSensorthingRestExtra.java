package org.eclipse.sensinact.sensorthings.sensing.rest.extra;

import java.util.Set;

import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.endpoint.AbstractEndpoint;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.ExtraUseCasesProvider;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.LocationsExtraUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.ObservedPropertiesExtraUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.ThingsExtraUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsApplicationSelect;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;

@Component(service = ISensinactSensorthingsRestExtra.class)
@JakartarsApplicationSelect(value = "sensorthings")
@JakartarsExtension
public class SensinactSensorthingRestExtra implements ISensinactSensorthingsRestExtra {

    @Override
    public Set<Class<?>> getExtraClasses() {
        return Set.of(ExtraUseCasesProvider.class, LocationsExtraUseCase.class, ThingsExtraUseCase.class,
                ObservedPropertiesExtraUseCase.class
        //
        );
    }

    @Override
    public Set<Object> getExtraSingletons() {
        return Set.of(new AbstractEndpoint<Thing>(), new AbstractEndpoint<Location>(), new AbstractEndpoint<Sensor>(),
                new AbstractEndpoint<Observation>(), new AbstractEndpoint<ObservedProperty>(),
                new AbstractEndpoint<Datastream>()

        );
    }

}
