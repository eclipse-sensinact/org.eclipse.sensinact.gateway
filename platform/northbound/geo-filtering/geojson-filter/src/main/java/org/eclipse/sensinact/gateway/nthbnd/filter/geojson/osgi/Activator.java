package org.eclipse.sensinact.gateway.nthbnd.filter.geojson.osgi;

import java.util.Hashtable;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.Filtering;
import org.eclipse.sensinact.gateway.nthbnd.filter.geojson.internal.GeoJSONFiltering;
import org.osgi.framework.BundleContext;

/**
 */
public class Activator extends AbstractActivator<Mediator> {

    private static final String TYPE = "geojson";

    @Override
    public void doStart() throws Exception 
    {
        super.mediator.info("Registering GeoJSON filter");
        super.mediator.register(new GeoJSONFiltering(
        		super.mediator), Filtering.class, 
        		new Hashtable(){{put("type",TYPE);}});
    }

    @Override
    public void doStop() throws Exception 
    {
        super.mediator.info("Unregistering GeoJSON filter");
    }

    @Override
    public Mediator doInstantiate(BundleContext context)
    {
        return new Mediator(context);
    }
}
