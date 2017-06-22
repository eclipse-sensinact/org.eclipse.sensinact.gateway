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
package org.eclipse.sensinact.gateway.core.osgi;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.eclipse.sensinact.gateway.core.security.SecuredAccess;
import org.eclipse.sensinact.gateway.core.security.SecuredAccessFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.condpermadmin.ConditionalPermissionAdmin;
import org.osgi.service.condpermadmin.ConditionalPermissionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionUpdate;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;

/**
 * Bundle Activator
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Activator extends AbstractActivator<Mediator>
{	
	private SecuredAccess securedAccess;
	private ServiceRegistration registration;

    /**
	 * @inheritDoc
	 *
	 * @see AbstractActivator#doStart()
	 */
	@Override
	public void doStart() throws Exception
	{    		
		ServiceLoader<SecuredAccessFactory> serviceLoader = ServiceLoader
		        .load(SecuredAccessFactory.class, mediator.getClassLoader());

		Iterator<SecuredAccessFactory> iterator = serviceLoader.iterator();

		if (iterator.hasNext())
		{
			SecuredAccessFactory factory = iterator.next();
			if (factory != null)
			{
				securedAccess = factory.newInstance(super.mediator);
			}
		}
		if (securedAccess == null)
		{
			throw new BundleException(
			        "A fragment providing SecuredAccess was excepted");
		}
		securedAccess.createAuthorizationService();

		ServiceReference<ConditionalPermissionAdmin> sRef = super.mediator.getContext(
				).getServiceReference(ConditionalPermissionAdmin.class);

		ConditionalPermissionAdmin cpa = null;

		if (sRef == null )
		{
			throw new BundleException(
			    "ConditionalPermissionAdmin services needed");
		}
		cpa = super.mediator.getContext().getService(sRef);

		ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();
		List piList = cpu.getConditionalPermissionInfos();
		ConditionalPermissionInfo cpiDeny = cpa.newConditionalPermissionInfo(String.format(
            "DENY {" + "[org.eclipse.sensinact.gateway.core.security.perm.SensiNactCoreCondition \"%s\" \"!\"]"
                    + "(org.osgi.framework.ServicePermission \"org.eclipse.sensinact.gateway.core.Core\" \"register\")"
                    + "(org.osgi.framework.ServicePermission \"SecuredAccess\" \"register\")"
                    + "(org.osgi.framework.ServicePermission \"SensiNactResourceModel\" \"register,get\")"
                    + "(org.osgi.framework.ServicePermission \"SensiNactResourceModelElement\" \"register,get\")"
                    + "(org.osgi.framework.ServicePermission \"SnaAgent\" \"register\")"
                    + "} null",
            mediator.getContext().getBundle().getLocation()));
		piList.add(cpiDeny);
		
		ConditionalPermissionInfo cpiAllow = cpa.newConditionalPermissionInfo(String.format(
			"ALLOW {"
			       + "[org.eclipse.sensinact.gateway.core.security.perm.SensiNactCoreCondition \"%s\" \"!\"]"
			       + "(java.security.AllPermission \"\" \"\")" 
			       + "} null",
		    mediator.getContext().getBundle().getLocation()));
		piList.add(cpiAllow);

		ConditionalPermissionInfo cpiAllowCurrent = cpa.newConditionalPermissionInfo(String.format(
			"ALLOW {"
		            + "[org.eclipse.sensinact.gateway.core.security.perm.SensiNactCoreCondition \"%s\"]"
		            + "(java.security.AllPermission \"\" \"\")"
		            + "} null",
		    mediator.getContext().getBundle().getLocation()));
		piList.add(cpiAllowCurrent );   	

		if (!cpu.commit())
		{
			throw new ConcurrentModificationException("Permissions changed during update");
		}
		registration = AccessController.doPrivileged(
		new PrivilegedAction<ServiceRegistration>() {
	        @Override
	        public ServiceRegistration run()
	        {
		        return mediator.getContext().registerService(
		                SecuredAccess.class.getCanonicalName(),
		                securedAccess, null);
	        }
        });
	}

	/**
	 * @inheritDoc
	 *
	 * @see AbstractActivator#
	 * doStop()
	 */
	@Override
	public void doStop() throws Exception
	{	
		if(this.registration != null)
		{
			try
			{
				SecuredAccess access = AccessController.doPrivileged(
				new PrivilegedAction<SecuredAccess>()
				{
			        @SuppressWarnings("unchecked")
					@Override
			        public SecuredAccess run()
			        {
				        return (SecuredAccess) mediator.getContext(
				        ).getService(Activator.this.registration.getReference());
			        }
		        });
				access.close();
				this.registration.unregister();
				
			} catch(IllegalStateException e)
			{
				if(super.mediator.isErrorLoggable())
				{
					super.mediator.error(e, e.getMessage());
				}
			} finally
			{
				this.registration = null;
			}
		}
	}

	/**
	 * @inheritDoc
	 *
	 * @see AbstractActivator#
	 * doInstantiate(org.osgi.framework.BundleContext)
	 */
	@Override
	public Mediator doInstantiate(BundleContext context) 
	{
		return new Mediator(context);
	}	
}
