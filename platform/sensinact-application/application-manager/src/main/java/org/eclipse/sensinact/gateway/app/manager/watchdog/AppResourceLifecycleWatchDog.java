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
package org.eclipse.sensinact.gateway.app.manager.watchdog;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.message.AbstractMidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.MidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.SnaErrorMessage;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class triggers an alert that stops the application when a resource disappears
 * during the runtime of the application
 *
 * @author Remi Druilhe
 * @see AbstractAppWatchDog
 */
public class AppResourceLifecycleWatchDog extends AbstractAppWatchDog {
	
	private static final Logger LOG = LoggerFactory.getLogger(AppResourceLifecycleWatchDog.class);
    private Collection<String> resourceUris;
    private Map<String,MidAgentCallback> registrations;

    /**
     * Constructor
     *
     * @param mediator     the mediator
     * @param service      the application to monitor
     * @param resourceUris the URI of the resources to monitor
     */
    public AppResourceLifecycleWatchDog(AppServiceMediator mediator, ApplicationService service, Collection<String> resourceUris) {
        super(mediator, service);
        this.resourceUris = resourceUris;
        this.registrations = new HashMap<String,MidAgentCallback>();
    }

    private Resource getResource(Session session, String uri) {
        String[] uriElements = UriUtils.getUriElements(uri);
        if (uriElements.length != 3) {
            return null;
        }
        return session.resource(uriElements[0], uriElements[1], uriElements[2]);
    }

    /**
     * @see AbstractAppWatchDog#start(Session)
     */
    public SnaErrorMessage start(Session session) {
        // Testing that all resources exist at start
        for (String resourceUri : resourceUris) {        	
            try {
            	 String[] uriElements = UriUtils.getUriElements(resourceUri);
            	 if(uriElements[0].indexOf('(')>-1)
            		 continue;
                 if (uriElements.length != 3 || session.getResource(uriElements[0], 
                		 uriElements[1], uriElements[2]).getStatusCode()!=200) {
                     throw new NullPointerException();
                 }
            } catch (NullPointerException e) {
                return new AppSnaMessage(this.mediator, "/AppManager", SnaErrorMessage.Error.SYSTEM_ERROR, "Resource " + resourceUri + " does not exist or you are not allowed to access it.");
            }
        }
        mediator.callService(Core.class, new Executable<Core, Void>() {
            @Override
            public Void execute(Core service) throws Exception {
                for (String resourceUri : resourceUris) {
               	 	String[] uriElements = UriUtils.getUriElements(resourceUri);
               	 	if(uriElements[0].indexOf('(')>-1)
               		 continue;
                    final SnaFilter filter = new SnaFilter(mediator, resourceUri);
                    filter.addHandledType(SnaMessage.Type.LIFECYCLE);
                    AppResourceLifeCycleSnaAgent callback = new AppResourceLifeCycleSnaAgent(mediator, AppResourceLifecycleWatchDog.this);
                    AppResourceLifecycleWatchDog.this.registrations.put(service.registerAgent(mediator,callback , filter), callback);
                }
                return null;
            }
        });
        return null;
    }

    /**
     * @see AbstractAppWatchDog#stop(Session)
     */
    public SnaErrorMessage stop(Session session) {
    	Iterator<MidAgentCallback> iterator = this.registrations.values().iterator();
    	while(iterator.hasNext()) {
    		iterator.next().stop();
    	}
    	return null;
    }

    /**
     * @see AbstractMidAgentCallback
     */
    private class AppResourceLifeCycleSnaAgent extends AbstractMidAgentCallback {
        private AbstractAppWatchDog watchDog;

        AppResourceLifeCycleSnaAgent(Mediator mediator, AbstractAppWatchDog watchDog) {
            super();
            this.watchDog = watchDog;
        }

        /**
         * @see AbstractMidAgentCallback#doHandle(SnaLifecycleMessageImpl)
         */
        @Override
        public void doHandle(SnaLifecycleMessageImpl message) {
            try {
                Attribute attribute = service.getResource(AppConstant.STATUS).getAttribute(DataResource.VALUE);
                ApplicationStatus status = (ApplicationStatus) attribute.getValue();
                switch (message.getType()) {
                    case RESOURCE_APPEARING:
                        // TODO: there is a problem of session when a resource appears because the session of the user
                        // TODO: does not exist anymore (need a brainstorming with Christophe about it)
                        /*
                        if ((Boolean) service.getResource(AppConstant.AUTORESTART).getAttribute(DataResource.VALUE).getValue()
                                && ApplicationStatus.UNRESOLVED.equals(status)) {
                            service.getResource(AppConstant.START).getAccessMethod(AccessMethod.Type.valueOf(AccessMethod.ACT))
                                    .invoke(null);//null, AccessLevelOption.AUTHENTICATED.getAccessLevel().getLevel()
                        }
                        */
                        break;
                    case RESOURCE_DISAPPEARING:
                        switch (status) {
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
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }
}
