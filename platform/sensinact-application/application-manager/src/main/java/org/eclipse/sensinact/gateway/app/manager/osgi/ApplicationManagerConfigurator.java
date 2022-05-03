/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.manager.osgi;

import org.eclipse.sensinact.gateway.app.api.persistence.ApplicationPersistenceService;
import org.eclipse.sensinact.gateway.app.manager.internal.AppManagerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
@Component(immediate=true)
public class ApplicationManagerConfigurator  {
	
	private static Logger LOG = LoggerFactory.getLogger(ApplicationManagerConfigurator.class);
    private AppManagerFactory appManagerFactory;

	private AppServiceMediator mediator;

	@Reference
	private ApplicationPersistenceService applicationPersistenceService;
    
    @Activate
	public void activate(ComponentContext componentContext) throws Exception {
    	
    	this.mediator = new AppServiceMediator(componentContext.getBundleContext());

        this.appManagerFactory = new AppManagerFactory(mediator, applicationPersistenceService);
        applicationPersistenceService.registerServiceAvailabilityListener(appManagerFactory);
    }

    @Deactivate
    public void deactivate() throws Exception {

        this.appManagerFactory.deleteAppManager();
    }
}
