/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.security.AccessNodeImpl;
import org.eclipse.sensinact.gateway.core.security.AccessProfileOption;
import org.eclipse.sensinact.gateway.core.security.MutableAccessNode;
import org.eclipse.sensinact.gateway.core.security.MutableAccessTree;
import org.eclipse.sensinact.gateway.core.security.SecuredAccess;
import org.eclipse.sensinact.gateway.security.signature.api.BundleValidation;
import org.eclipse.sensinact.gateway.util.ReflectUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows to build a {@link ModelInstance} in a simple way
 */
public class ModelInstanceBuilder<C extends ModelConfiguration,I extends ModelInstance<C>> {
	private static final Logger LOG=LoggerFactory.getLogger(ModelInstanceBuilder.class);

	protected Mediator mediator;

	/**
	 * Constructor
	 * 
	 * @param mediator the {@link Mediator} allowing the ModelInstanceBuilder to
	 * be instantiated to interact with the OSGi host environment
	 * 
	 */
	public ModelInstanceBuilder(Mediator mediator) {
		this.mediator = mediator;
	}

	/**
	 * Creates the {@link AccessNodeImpl} for the {@link SensiNactResourceModel} to
	 * be built, and add it to the {@link RootNode} passed as parameter
	 * 
	 * @param root
	 *            the {@link RootNode} to which attach the new created
	 *            {@link AccessNodeImpl}
	 */
	protected void buildAccessNode(final MutableAccessTree<? extends MutableAccessNode> accessTree, final String name) {
		AccessController.<Void>doPrivileged(new PrivilegedAction<Void>() {
			@Override
			public Void run() {
				final String identifier = ModelInstanceBuilder.this.mediator.callService(BundleValidation.class,
					new Executable<BundleValidation, String>() {
						@Override
						public String execute(BundleValidation service) throws Exception {
							return service.check(ModelInstanceBuilder.this.mediator.getContext().getBundle());
						}
					});
				if (identifier == null) {
					accessTree.add(UriUtils.getUri(new String[] { name })).withAccessProfile(
							AccessProfileOption.ALL_ANONYMOUS.getAccessProfile());
				} else {
					ModelInstanceBuilder.this.mediator.callService(SecuredAccess.class,
						new Executable<SecuredAccess, Void>() {
							@Override
							public Void execute(SecuredAccess service) throws Exception {
								service.buildAccessNodesHierarchy(identifier, name, accessTree);
									return null;
								}
						});
				}
				return null;
			}
		});
	}

	/**
	 * Creates and return a {@link SensiNactResourceModel} instance with the
	 * specified properties. Optional arguments apply to the
	 * {@link SensiNactResourceModelConfiguration} initialization
	 * 
	 * @return the new created {@link SensiNactResourceModel}
	 */
	public I build(String name, String profileId, C modelConfiguration) {
		I instance = null;

		if (modelConfiguration != null) {
			
			this.buildAccessNode(modelConfiguration.getAccessTree(), name);
			Class<I> ci =  modelConfiguration.<C,I>getModelInstanceType();

			if(ci != null) {
			    instance = (I) ReflectUtils.<ModelInstance,I>getInstance(
				    ModelInstance.class, ci, this.mediator, modelConfiguration, 
				        name, profileId);
			} else {
			    instance = (I) ReflectUtils.<ModelInstance>getInstance(
					    ModelInstance.class, new Object[] {this.mediator, modelConfiguration, 
					        name, profileId});
			}
			try {
				this.register(instance);

			} catch (ModelAlreadyRegisteredException e) {
				LOG.error("Model instance '{}' already exists", name);
				instance = null;
			}
		}
		return instance;
	}

	/**
	 * @param instance
	 */
	protected final void register(I instance) {
		if (instance == null) {
			return;
		}
		instance.register();
	}
}