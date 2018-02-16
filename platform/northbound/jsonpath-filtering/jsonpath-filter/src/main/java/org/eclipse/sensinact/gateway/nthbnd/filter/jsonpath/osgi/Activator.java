package org.eclipse.sensinact.gateway.nthbnd.filter.jsonpath.osgi;

import java.util.Hashtable;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.Filtering;
import org.eclipse.sensinact.gateway.nthbnd.filter.jsonpath.internal.JsonPathFiltering;
import org.osgi.framework.BundleContext;

/**
 */
public class Activator extends AbstractActivator<Mediator> {

    private static final String TYPE = "jsonpath";

    @Override
    public void doStart() throws Exception 
    {
        super.mediator.info("Registering JSONPath filter");
        super.mediator.register(new JsonPathFiltering(
        		super.mediator), Filtering.class, 
        		new Hashtable(){{put("type",TYPE);}});
    }

    @Override
    public void doStop() throws Exception 
    {
        super.mediator.info("Unregistering JSONPath filter");
    }

    @Override
    public Mediator doInstantiate(BundleContext context)
    {
        return new Mediator(context);
    }
}
