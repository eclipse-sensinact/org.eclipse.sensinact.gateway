package org.eclipse.sensinact.gateway.nthbnd.filter.jsonpath.osgi;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.Filtering;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;
import org.eclipse.sensinact.gateway.nthbnd.filter.jsonpath.internal.JsonPathFiltering;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 */
public class Activator extends AbstractActivator<NorthboundMediator> {

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
    public NorthboundMediator doInstantiate(BundleContext context)
    {
        return new NorthboundMediator(context);
    }
}
