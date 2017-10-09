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
package org.eclipse.sensinact.gateway.security.signature.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import org.eclipse.sensinact.gateway.security.signature.internal.BundleValidationImpl;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.security.signature.api.BundleValidation;

public class BundleValidationActivator implements BundleActivator
{
	private ServiceRegistration<BundleValidation> serviceRegistration;

	public void start(final BundleContext context)
	{
		try
		{
			this.serviceRegistration = context.registerService(
			    BundleValidation.class, new BundleValidationImpl(
			    		new Mediator(context)), null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void stop(final BundleContext context)
	{
		try
		{
			this.serviceRegistration.unregister();
			this.serviceRegistration = null;
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
