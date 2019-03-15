package org.eclipse.sensinact.gateway.core.osgi;

import org.eclipse.sensinact.gateway.core.api.Sensinact;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class Tracker implements ServiceTrackerCustomizer<Sensinact,Sensinact>{

    private final BundleContext context;

    public Tracker(BundleContext context){
        this.context=context;
    }

    @Override
    public Sensinact addingService(ServiceReference<Sensinact> serviceReference) {
        System.out.println("adding service --->"+serviceReference);

        return context.getService(serviceReference);
    }

    @Override
    public void modifiedService(ServiceReference<Sensinact> serviceReference, Sensinact o) {

    }

    @Override
    public void removedService(ServiceReference<Sensinact> serviceReference, Sensinact o) {
        System.out.println("removing service --->"+serviceReference);
    }
}
