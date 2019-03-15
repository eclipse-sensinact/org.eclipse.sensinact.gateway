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
import java.util.List;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.SensiNact;
import org.eclipse.sensinact.gateway.core.api.Sensinact;
import org.eclipse.sensinact.gateway.util.ReflectUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.condpermadmin.ConditionalPermissionAdmin;
import org.osgi.service.condpermadmin.ConditionalPermissionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionUpdate;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Bundle Activator
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Activator extends AbstractActivator<Mediator> {

	private ServiceRegistration registration;

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator# doStart()
	 */
	@Override
	public void doStart() throws Exception {
		ServiceReference<ConditionalPermissionAdmin> sRef = super.mediator.getContext()
				.getServiceReference(ConditionalPermissionAdmin.class);

		ConditionalPermissionAdmin cpa = null;

		if (sRef == null) {
			throw new BundleException("ConditionalPermissionAdmin services needed");
		}
		List<String> types = ReflectUtils.getAllStringTypes(mediator.getContext().getBundle());

		StringBuilder builder = new StringBuilder();

		for (int index = 0; index < types.size(); index++) {
			if (index > 0)
				builder.append("\\,");
			builder.append(types.get(index));
		}
		cpa = super.mediator.getContext().getService(sRef);

		ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();
		List piList = cpu.getConditionalPermissionInfos();
/*
		ConditionalPermissionInfo cpiDeny = cpa.newConditionalPermissionInfo(
			String.format("DENY { [org.eclipse.sensinact.gateway.core.security.perm.StrictCodeBaseCondition \"%s\" \"!\"]"
				//+ " (org.osgi.framework.ServicePermission \"org.eclipse.sensinact.gateway.core.Core\" \"register\")"
				+ "(org.osgi.framework.ServicePermission \"org.eclipse.sensinact.gateway.core.SensiNactResourceModel\" \"register,get\")"
				+ "(org.osgi.framework.ServicePermission \"org.eclipse.sensinact.gateway.core.SensiNactResourceModelElement\" \"register,get\")"
				+ "(org.osgi.framework.ServicePermission \"org.eclipse.sensinact.gateway.core.message.LocalAgent\" \"register,get\")"
				+ "(org.osgi.framework.ServicePermission \"org.eclipse.sensinact.gateway.core.message.RemoteAgent\" \"register,get\")"
				//+ "(org.osgi.framework.ServicePermission \"org.eclipse.sensinact.gateway.core.api.SensinactCoreBaseIface\" \"register,get\")"
				//	+ "(org.osgi.framework.ServicePermission \"org.eclipse.sensinact.gateway.core.SensinactCoreBase\" \"register,get\")"
				//+ "(org.osgi.framework.ServicePermission \"org.eclipse.sensinact.gateway.core.api.Sensinact\" \"register,get\")"
				+ "(org.osgi.framework.ServicePermission \"org.eclipse.sensinact.gateway.core.remote.RemoteCore\" \"register,get\")"
				+ "(org.osgi.framework.ServicePermission \"org.eclipse.sensinact.gateway.core.security.SecuredAccess\" \"register,get\")"
				+ "(org.osgi.framework.ServicePermission \"org.eclipse.sensinact.gateway.core.security.UserManager\" \"register,get\")"
				+ "(org.osgi.framework.ServicePermission \"org.eclipse.sensinact.gateway.core.security.SecurityDataStoreService\" \"register,get\")"
				+ "} null", builder.toString()));
		piList.add(cpiDeny);
*/
/*
		ConditionalPermissionInfo cpiAllow = null;

		cpiAllow = cpa.newConditionalPermissionInfo(
			"ALLOW {[org.eclipse.sensinact.gateway.core.security.perm.CodeBaseCondition \"*\"](java.security.AllPermission \"\" \"\")} null");
		
		piList.add(cpiAllow);
*/
		if (!cpu.commit()) {
			throw new ConcurrentModificationException("Permissions changed during update");
		}

		registration = AccessController.doPrivileged(new PrivilegedAction<ServiceRegistration>() {
			@Override
			public ServiceRegistration run() {
				try {
					return mediator.getContext().registerService(new String[]{Core.class.getCanonicalName(),Sensinact.class.getName()},
							new SensiNact(mediator), null);
				} catch (Exception e) {
					e.printStackTrace();
					mediator.error(e);
				}
				return null;
			}
		});



	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator# doStop()
	 */
	@Override
	public void doStop() throws Exception {
		if (this.registration != null) {
			try {
				Core core = AccessController.doPrivileged(new PrivilegedAction<Core>() {
					@SuppressWarnings("unchecked")
					@Override
					public Core run() {
						return (Core) mediator.getContext().getService(Activator.this.registration.getReference());
					}
				});
				core.close();
				this.registration.unregister();

			} catch (IllegalStateException e) {
				super.mediator.error(e, e.getMessage());

			} finally {
				this.registration = null;
			}
		}
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator#
	 *      doInstantiate(org.osgi.framework.BundleContext)
	 */
	@Override
	public Mediator doInstantiate(BundleContext context) {
		return new Mediator(context);
	}
}
