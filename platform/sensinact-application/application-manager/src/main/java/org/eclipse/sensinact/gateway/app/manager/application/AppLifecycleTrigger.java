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

package org.eclipse.sensinact.gateway.app.manager.application;

import org.eclipse.sensinact.gateway.app.api.exception.LifeCycleException;
import org.eclipse.sensinact.gateway.app.api.lifecycle.ApplicationStatus;
import org.eclipse.sensinact.gateway.app.manager.AppConstant;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder;
import org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTrigger;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;

/**
 * @see AccessMethodTrigger
 *
 * @author Remi Druilhe
 */
public class AppLifecycleTrigger implements AccessMethodTrigger<AccessMethodResponseBuilder> 
{

    private static final String APP_LIFECYCLE_TRIGGER = "AppLifecycleTrigger";

    private final ApplicationService service;

    public AppLifecycleTrigger(ApplicationService service) 
    {
        this.service = service;
    }

    /**
     * @see AccessMethodTrigger#getName()
     */
    public String getName() 
    {
        return APP_LIFECYCLE_TRIGGER;
    }

    /**
     * @see AccessMethodTrigger#getParameters()
     */
    public Parameters getParameters() 
    {
        return Parameters.INTERMEDIATE;
    }

    /**
     * @see Executable#execute(java.lang.Object)
     */
    public Object execute(AccessMethodResponseBuilder snaResult) throws Exception
    {
        String uri = snaResult.getPath();

        ApplicationStatus currentStatus = (ApplicationStatus) service.getResource(
        	AppConstant.STATUS).getAttribute(DataResource.VALUE).getValue();

        if (uri.endsWith(AppConstant.START)) 
        {
            if (!snaResult.hasError()) 
            {
                if (ApplicationStatus.INSTALLED.equals(currentStatus) 
                		|| ApplicationStatus.UNRESOLVED.equals(currentStatus)) 
                {
                    currentStatus = ApplicationStatus.RESOLVING;
                    
                } else if (currentStatus.equals(ApplicationStatus.RESOLVING))
                {
                    currentStatus = ApplicationStatus.ACTIVE;
                }
            } else {
                return ApplicationStatus.UNRESOLVED;
            }
        } else if (uri.endsWith(AppConstant.UNINSTALL)) {
            if (ApplicationStatus.INSTALLED.equals(currentStatus) 
            		|| ApplicationStatus.UNRESOLVED.equals(currentStatus))
            {
                currentStatus = ApplicationStatus.UNINSTALLED;
                
            } else
            {
                snaResult.registerException(new LifeCycleException(
                		"Unable to UNINSTALL an application " +
                        "which is not in an INSTALLED or UNRESOLVED state"));
            }
        } else if (uri.endsWith(AppConstant.STOP)) {
            if (ApplicationStatus.ACTIVE.equals(currentStatus)) {
                currentStatus = ApplicationStatus.INSTALLED;
            } else {
                snaResult.registerException(new LifeCycleException(
                		"Unable to STOP an application " +
                        "which is not in an ACTIVE state"));
            }
        } else if (uri.endsWith(AppConstant.EXCEPTION)) {
            if (ApplicationStatus.ACTIVE.equals(currentStatus)) {
                currentStatus = ApplicationStatus.UNRESOLVED;
            } else {
                snaResult.registerException(new LifeCycleException(
                		"This should never happened"));
            }
        }

        return currentStatus;
    }

    /**
     * @see JSONable#getJSON()
     */
    public String getJSON() {
        return null;
    }

	/**
	 * @inheritDoc
	 *
	 * @see AccessMethodTrigger#passOn()
	 */
    public boolean passOn() {
	    return true;
    }
}
