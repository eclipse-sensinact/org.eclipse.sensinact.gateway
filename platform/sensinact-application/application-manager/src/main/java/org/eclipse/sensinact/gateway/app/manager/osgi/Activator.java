/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */

package org.eclipse.sensinact.gateway.app.manager.osgi;

import org.eclipse.sensinact.gateway.app.api.persistence.listener.ApplicationAvailabilityListenerAbstract;
import org.eclipse.sensinact.gateway.app.manager.application.persistence.APSApplication;
import org.eclipse.sensinact.gateway.app.manager.internal.AppManagerFactory;
import org.eclipse.sensinact.gateway.common.annotation.Property;
import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


/**
 * @see AbstractActivator
 *
 * @author Remi Druilhe
 */
public class Activator extends AbstractActivator<AppServiceMediator> {

    private static Logger LOG= LoggerFactory.getLogger(Activator.class);

    private AppManagerFactory appManagerFactory;

    private Thread persistenceThread;

    @Property(defaultValue = "application")
    private String directory;

    @Property(validationRegex = Property.INTEGER)
    private Long updateFileCheck;

    @Property
    private Boolean applicationPersist;

    @Property(defaultValue = "json",validationRegex = Property.ALPHANUMERIC)
    private String applicationFileExtension;

    /**
     * @see AbstractActivator#doStart()
     */
    public void doStart() throws Exception {
        this.appManagerFactory = new AppManagerFactory(mediator);

        if(applicationPersist){
            File directoryToMonitor=new File(directory);
            APSApplication directoryMonitor=new APSApplication(directoryToMonitor,updateFileCheck, applicationFileExtension);
            persistenceThread=new Thread(directoryMonitor);
            directoryMonitor.registerServiceAvailabilityListener(new ApplicationAvailabilityListenerAbstract() {
                @Override
                public void applicationFound(String applicationName, String content) {
                    try {
                        appManagerFactory.getInstallResource().install(applicationName,new JSONObject(content));
                        //appManagerFactory.getInstallResource().getAccessMethod(AccessMethod.Type.valueOf(AccessMethod.ACT)).invoke(new Object[]{applicationName,new JSONObject(content)});
                        //appManagerFactory.getServiceProvider().getService(applicationName).getResource("START").getAccessMethod(AccessMethod.Type.valueOf(AccessMethod.ACT)).invoke(new Object[]{});
                    }catch(Exception e){
                        LOG.error("Failed to install application '{}'",applicationName,e);
                    }

                }

                @Override
                public void applicationRemoved(String applicationName) {
                    try {
                        //appManagerFactory.getServiceProvider().getService(applicationName).getResource("STOP").getAccessMethod(AccessMethod.Type.valueOf(AccessMethod.ACT)).invoke(new Object[]{});
                        //appManagerFactory.getUninstallResource().getAccessMethod(AccessMethod.Type.valueOf(AccessMethod.ACT)).invoke(new Object[]{applicationName});
                        appManagerFactory.getUninstallResource().uninstall(applicationName);
                    }catch(Exception e){
                        LOG.error("Failed to uninstall application",applicationName,e);
                    }

                }
            });
            persistenceThread.start();
        }

    }

    /**
     * @see AbstractActivator#doStop()
     */
    public void doStop() throws Exception {
        if(persistenceThread!=null) persistenceThread.interrupt();
        this.appManagerFactory.deleteAppManager();
    }

    /**
     * @see AbstractActivator#doInstantiate(BundleContext)
     */
    public AppServiceMediator doInstantiate(BundleContext context)
    {
        return new AppServiceMediator(context);
    }
}
