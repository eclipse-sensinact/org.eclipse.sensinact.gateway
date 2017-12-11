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
package org.eclipse.sensinact.gateway.app.manager.watchdog;

import org.eclipse.sensinact.gateway.app.api.lifecycle.ApplicationStatus;
import org.eclipse.sensinact.gateway.app.manager.AppConstant;
import org.eclipse.sensinact.gateway.app.manager.application.ApplicationService;
import org.eclipse.sensinact.gateway.app.manager.json.AppSnaMessage;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.Attribute;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.message.*;
import org.eclipse.sensinact.gateway.core.security.SecuredAccess;
import org.eclipse.sensinact.gateway.core.security.Session;
import org.eclipse.sensinact.gateway.util.UriUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class triggers an alert that stops the application when a resource disappears
 * during the runtime of the application
 *
 * @see AbstractAppWatchDog
 *
 * @author Remi Druilhe
 */
public class AppResourceLifecycleWatchDog extends AbstractAppWatchDog {

    private Collection<String> resourceUris;
    private List<String> registrations;

    /**
     * Constructor
     * @param mediator the mediator
     * @param service the application to monitor
     * @param resourceUris the URI of the resources to monitor
     */
    public AppResourceLifecycleWatchDog(AppServiceMediator mediator, ApplicationService service,
                                        Collection<String> resourceUris) {
        super(mediator, service);

        this.resourceUris = resourceUris;
        this.registrations = new ArrayList<String>();
    }

    private Resource getResource(Session session, String uri)
    {
    	String[] uriElements = UriUtils.getUriElements(uri);
    	if(uriElements.length != 3)
    	{
    		return null;
    	}
    	return session.resource(uriElements[0],uriElements[1],uriElements[2]);
    }
    
    /**
     * @see AbstractAppWatchDog#start(Session)
     */
    public SnaErrorMessage start(Session session)
    {
        // Testing that all resources exist at start
        for(String resourceUri : resourceUris) 
        {
            try
            {
                getResource(session, resourceUri).getName();
                
            } catch (NullPointerException e)
            {
                return new AppSnaMessage(this.mediator, "/AppManager", 
                	SnaErrorMessage.Error.SYSTEM_ERROR, "Resource " + 
                    resourceUri + " does not exist or you are not allowed to access it.");
            }
        }
        mediator.callService(Core.class, 
    	new Executable<Core,Void>()
		{
			@Override
			public Void execute(Core service) 
					throws Exception 
			{
		        for(String resourceUri : resourceUris)
		        {
		            final SnaFilter filter = new SnaFilter(mediator, resourceUri);
		            filter.addHandledType(SnaMessage.Type.LIFECYCLE);
    
		            AppResourceLifecycleWatchDog.this.registrations.add(
		            service.registerAgent(mediator, new AppResourceLifeCycleSnaAgent(
		            	mediator, AppResourceLifecycleWatchDog.this), filter));
		        } 
		        return null;
			}    		
		});
        return null;
    }

    /**
     * @see AbstractAppWatchDog#stop(Session)
     */
    public SnaErrorMessage stop(Session session)
    {
        mediator.callService(Core.class, 
    	new Executable<Core,Void>()
		{
			@Override
			public Void execute(Core service) 
					throws Exception 
			{
		        for(String registration : 
		        	AppResourceLifecycleWatchDog.this.registrations)
		        {
		            service.unregisterAgent(registration);
		        } 
		        return null;
			}    		
		});
        return null;
    }

    /**
     * @see AbstractMidAgentCallback
     */
    private class AppResourceLifeCycleSnaAgent extends AbstractMidAgentCallback {

        private AbstractAppWatchDog watchDog;

        AppResourceLifeCycleSnaAgent(Mediator mediator, AbstractAppWatchDog watchDog)
        {
            super();
            this.watchDog = watchDog;
        }

        /**
         * @see AbstractMidAgentCallback#doHandle(SnaLifecycleMessageImpl)
         */
        public void doHandle(SnaLifecycleMessageImpl message) {
            try {
                Attribute attribute = service.getResource(AppConstant.STATUS).getAttribute(DataResource.VALUE);

                ApplicationStatus status = (ApplicationStatus) attribute.getValue();

                switch(message.getType()) {
                    case RESOURCE_APPEARING:
                        // TODO: there is a problem of session when a resource appears because the session of the user
                        // TODO: does not exist anymore (need a brainstorming with Christophe about it)
                        /*if ((Boolean) service.getResource(AppConstant.AUTORESTART).getAttribute(DataResource.VALUE).getValue()
                                && ApplicationStatus.UNRESOLVED.equals(status)) {

                            service.getResource(AppConstant.START).getAccessMethod(AccessMethod.Type.ACT)
                                    .invoke(null, AccessLevelOption.AUTHENTICATED.getAccessLevel().getLevel());
                        }*/
                        break;
                    case RESOURCE_DISAPPEARING:
                        switch(status) {
                            case ACTIVE:
                            case INSTALLED:
                            case RESOLVING:
                                watchDog.alert("Resource " + message.getPath() + " disappeared");

                                break;
                            case UNINSTALLED:
                            case UNRESOLVED:
                            default:
                                break;
                        }
                    case PROVIDER_APPEARING:
                    case PROVIDER_DISAPPEARING:
                    case SERVICE_APPEARING:
                    case SERVICE_DISAPPEARING:
                    default:
                        break;
                }
            } catch(Exception e)
            {
               mediator.error(e);
            }
        }

        /**
         * Not used
         * @see AbstractMidAgentCallback#doHandle(SnaUpdateMessageImpl)
         */
        public void doHandle(SnaUpdateMessageImpl event) {}

        /**
         * Not used
         * @see AbstractMidAgentCallback#doHandle(SnaErrorMessageImpl)
         */
        public void doHandle(SnaErrorMessageImpl event) {}

        /**
         * Not used
         * @see AbstractMidAgentCallback#doHandle(SnaResponseMessage)
         */
        public void doHandle(SnaResponseMessage event) {}

        /**
         * @inheritDoc
         *
         * @see AbstractMidAgentCallback#stop()
         */
        public void stop() {}
    }
}
