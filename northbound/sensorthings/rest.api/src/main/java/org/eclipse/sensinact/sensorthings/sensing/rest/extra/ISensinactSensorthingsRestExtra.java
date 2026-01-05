package org.eclipse.sensinact.sensorthings.sensing.rest.extra;

import java.util.Set;

public interface ISensinactSensorthingsRestExtra {

    public Set<Class<?>> getExtraClasses();

    public Set<Object> getExtraSingletons();

}
