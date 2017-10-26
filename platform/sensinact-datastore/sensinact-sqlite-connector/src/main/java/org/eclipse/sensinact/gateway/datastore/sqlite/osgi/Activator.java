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

package org.eclipse.sensinact.gateway.datastore.sqlite.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import org.osgi.framework.ServiceRegistration;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreService;
import org.eclipse.sensinact.gateway.datastore.sqlite.internal.SQLiteDataStoreService;

/**
 * {@link BundleActivator} interface implementation
 */
public class Activator extends AbstractActivator<Mediator> 
{	
	private SQLiteDataStoreService dataBaseService = null;
	private ServiceRegistration dataServiceRegistration = null;

    /** 
     * @inheritDoc
     *
     * @see AbstractActivator#doStart()
     */
    @Override
    public void doStart() throws Exception
    {
    	try
    	{
	        dataBaseService = new SQLiteDataStoreService(mediator, 
	        (String) mediator.getProperty(
	        "org.eclipse.sensinact.gateway.security.database"));
	        
	        this.dataServiceRegistration = 
	                super.mediator.getContext().registerService(
	                DataStoreService.class.getCanonicalName(),
	                dataBaseService,null);
	        
    	} catch(Exception e)
    	{
    		e.printStackTrace();
    		throw e;
    	}
    }

    /** 
     * @inheritDoc
     *
     * @see AbstractActivator#doStop()
     */
    @Override
    public void doStop() throws Exception
    {
        if(this.dataServiceRegistration != null)
        {
            try
            {
                this.dataServiceRegistration.unregister();
                
            } catch(IllegalStateException e)
            {
                if(super.mediator.isErrorLoggable())
                {
                    super.mediator.error(e.getMessage(), e);
                }
            }
        }
        this.dataBaseService.stop();
        this.dataBaseService = null;
    }
    
	/**
	 * @inheritDoc
	 *
	 * @see AbstractActivator#
	 * doInstantiate(org.osgi.framework.BundleContext, int, java.io.FileOutputStream)
	 */
	public Mediator doInstantiate(BundleContext context)
	{
		return new Mediator(context);
	}
}
