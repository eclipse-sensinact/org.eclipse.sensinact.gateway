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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.SensiNact;
import org.eclipse.sensinact.gateway.core.Service;
import org.eclipse.sensinact.gateway.core.api.Sensinact;
import org.eclipse.sensinact.gateway.core.security.SecuredAccessException;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.util.ReflectUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;
import org.osgi.service.condpermadmin.ConditionalPermissionAdmin;
import org.osgi.service.condpermadmin.ConditionalPermissionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionUpdate;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bundle Activator
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Activator extends AbstractActivator<Mediator> {

	private final Logger LOG= LoggerFactory.getLogger(Activator.class);

	private ServiceRegistration registration;
	private ServiceRegistration registrationConfiguration;

	@Override
	public void start(final BundleContext context) throws Exception {
		//super.start(context);
/*
		ServiceReference<ConditionalPermissionAdmin> sRef = context
				.getServiceReference(ConditionalPermissionAdmin.class);

		ConditionalPermissionAdmin cpa = null;

		if (sRef == null) {
			throw new BundleException("ConditionalPermissionAdmin services needed");
		}
		List<String> types = ReflectUtils.getAllStringTypes(context.getBundle());

		StringBuilder builder = new StringBuilder();

		for (int index = 0; index < types.size(); index++) {
			if (index > 0)
				builder.append("\\,");
			builder.append(types.get(index));
		}
		cpa = context.getService(sRef);

		ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();

		List piList = cpu.getConditionalPermissionInfos();

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

		ConditionalPermissionInfo cpiAllow = null;

		cpiAllow = cpa.newConditionalPermissionInfo(
			"ALLOW {[org.eclipse.sensinact.gateway.core.security.perm.CodeBaseCondition \"*\"](java.security.AllPermission \"\" \"\")} null");

		piList.add(cpiAllow);

		if (!cpu.commit()) {
			throw new ConcurrentModificationException("Permissions changed during update");
		}
		*/

/*
		registration = AccessController.doPrivileged(new PrivilegedAction<ServiceRegistration>() {
			@Override
			public ServiceRegistration run() {

				try {

					return mediator.getContext().registerService(new String[]{Core.class.getCanonicalName(), Sensinact.class.getName()},
							new SensiNact(null, mediator), null);
				} catch (SecuredAccessException e) {
					e.printStackTrace();
				} catch (BundleException e) {
					e.printStackTrace();
				} catch (DataStoreException e) {
					e.printStackTrace();
				}
				return null;
			}
		});
*/


		this.registrationConfiguration=context.registerService(ConfigurationListener.class.getCanonicalName(),new ConfigurationListener(){

			@Override
			public void configurationEvent(ConfigurationEvent event) {

				LOG.debug("Receiving Configuration notification for {}",event.getPid());

				//Waits for the sensinact configuration is available in config admin
				if(event.getPid().equals("sensinact")) {

					registration = AccessController.doPrivileged(new PrivilegedAction<ServiceRegistration>() {
						@Override
						public ServiceRegistration run() {
							try {

								ServiceReference configadminsr=context.getServiceReferences(ConfigurationAdmin.class.getCanonicalName(),null)[0];

								ConfigurationAdmin configurationAdmin=(ConfigurationAdmin)context.getService(configadminsr);
								Configuration confSensinact=configurationAdmin.getConfiguration("sensinact");

								final String namespaceLocal=confSensinact.getProperties().get("namespace").toString();

								LOG.info("SensiNact configuration is available, starting up SensiNact core with namespace {}..",namespaceLocal);

								Activator.this.mediator = Activator.this.initMediator(context);

								// Transfer sensinact config admin property into Mediator in order to be used by the other bundles
								Enumeration keysEnumeration=confSensinact.getProperties().keys();
								while(keysEnumeration.hasMoreElements()){
									String key=(String)keysEnumeration.nextElement();
									Activator.this.mediator.setProperty(key,confSensinact.getProperties().get(key).toString());
								}

								Activator.this.injectPropertyFields();
								return context.registerService(new String[]{Core.class.getCanonicalName(), Sensinact.class.getName()},
										new SensiNact(namespaceLocal,Activator.this.mediator), null);
							} catch (Exception e) {
								e.printStackTrace();
								Activator.this.mediator.error(e);
							}
							return null;
						}
					});

				}



			}
		},new Hashtable<String,String>());


	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator# doStart()
	 */
	@Override
	public void doStart() throws Exception {
		//Core cannot rely on this method, since it will only be called only by AbstractActivator when a SensiNact core itself is available in the registry
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
