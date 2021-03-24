/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.app.manager.osgi;

import java.io.File;
import java.util.Map;

import org.eclipse.sensinact.gateway.app.api.persistence.ApplicationPersistenceService;
import org.eclipse.sensinact.gateway.app.manager.application.persistence.SNAPersistApplicationFileSystem;
import org.eclipse.sensinact.gateway.app.manager.application.persistence.SNAPersistApplicationInMemory;
import org.eclipse.sensinact.gateway.app.manager.internal.AppManagerFactory;
import org.eclipse.sensinact.gateway.common.annotation.Property;
import org.eclipse.sensinact.gateway.common.interpolator.Interpolator;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
@Component(immediate=true)
public class ApplicationManagerConfigurator  {
	
    private static Logger LOG = LoggerFactory.getLogger(ApplicationManagerConfigurator.class);
    
    private AppManagerFactory appManagerFactory;
    private Thread persistenceThread;
    
    @Property(defaultValue = "application")
    private String directory;
    
    @Property(validationRegex = Property.INTEGER, mandatory=false, defaultValue="0")
    private Long updateFileCheck;
    
    @Property(mandatory = false, defaultValue="false")
    private Boolean applicationPersist;
    
    @Property(defaultValue = "json", validationRegex = Property.ALPHANUMERIC)
    private String applicationFileExtension;

	private AppServiceMediator mediator;

    protected void injectPropertyFields() throws Exception {
        this.mediator.debug("Starting introspection in bundle %s", mediator.getContext().getBundle().getSymbolicName());
        Interpolator interpolator = new Interpolator(this.mediator);
        interpolator.getInstance(this);
        for(Map.Entry<String,String> entry:interpolator.getPropertiesInjected().entrySet()){
            if(!this.mediator.getProperties().containsKey(entry.getKey()))
                mediator.setProperty(entry.getKey(),entry.getValue());
        }
    }
    
    @Activate
	public void activate(ComponentContext componentContext) throws Exception {
    	
    	this.mediator = new AppServiceMediator(componentContext.getBundleContext());
        injectPropertyFields();
        
        ApplicationPersistenceService directoryMonitor = null;
        if (applicationPersist) {
            LOG.info("Filesystem Persistence mechanism is ON");
            directoryMonitor = new SNAPersistApplicationFileSystem(new File(directory), updateFileCheck, applicationFileExtension);
            ;
        } else {
            LOG.info("Filesystem Persistence mechanism is OFF");
            directoryMonitor = new SNAPersistApplicationInMemory();
        }
        this.appManagerFactory = new AppManagerFactory(mediator, directoryMonitor);
        directoryMonitor.registerServiceAvailabilityListener(appManagerFactory);
        persistenceThread = new Thread(directoryMonitor);
        persistenceThread.setDaemon(true);
        persistenceThread.setPriority(Thread.MIN_PRIORITY);
        persistenceThread.start();
    }

    @Deactivate
    public void deactivate() throws Exception {
        if (persistenceThread != null) persistenceThread.interrupt();
        this.appManagerFactory.deleteAppManager();
    }
}
