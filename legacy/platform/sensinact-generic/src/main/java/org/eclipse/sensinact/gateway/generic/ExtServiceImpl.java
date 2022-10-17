/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic;

import java.util.List;

import org.eclipse.sensinact.gateway.core.ActionResource;
import org.eclipse.sensinact.gateway.core.InvalidResourceException;
import org.eclipse.sensinact.gateway.core.InvalidServiceException;
import org.eclipse.sensinact.gateway.core.ResourceBuilder;
import org.eclipse.sensinact.gateway.core.ResourceImpl;
import org.eclipse.sensinact.gateway.core.ResourceProxy;
import org.eclipse.sensinact.gateway.core.ServiceImpl;
import org.eclipse.sensinact.gateway.core.ServiceProviderImpl;
import org.eclipse.sensinact.gateway.core.ServiceProxy;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethodExecutor.ExecutionPolicy;
import org.eclipse.sensinact.gateway.core.method.Signature;
import org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTrigger;
import org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTriggerFactory;
import org.eclipse.sensinact.gateway.generic.parser.ReferenceDefinition;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.json.JsonObject;

/**
 * Extended abstract {@link ServiceImpl} implementation
 * to reify a Service in the gateway
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ExtServiceImpl extends ServiceImpl {
	
	private static final Logger LOG = LoggerFactory.getLogger(ExtServiceImpl.class);
    protected final String defaultResourceConfig;

    /**
     * Constructor
     *
     * @param mediator
     * @param uri
     * @param defaultResourceConfig
     * @throws InvalidServiceException
     */
    public ExtServiceImpl(ExtModelInstance<?> snaModelInstance, String name, ServiceProviderImpl serviceProvider) throws InvalidServiceException {
        this(snaModelInstance, name, serviceProvider, null);
    }

    /**
     * Constructor
     *
     * @param mediator
     * @param uri
     * @param defaultResourceConfig
     * @throws InvalidServiceException
     */
    public ExtServiceImpl(ExtModelInstance<?> snaModelInstance, String name, ServiceProviderImpl serviceProvider, String defaultResourceConfig) throws InvalidServiceException {
        super(snaModelInstance, name, serviceProvider);
        this.defaultResourceConfig = defaultResourceConfig;
    }

    /**
     * Creates and returns a new {@link ResourceImpl} and
     * updates existing {@link ServiceProxy}s its {@link
     * ResourceProxy}
     *
     * @param builder the {@link ResourceBuilder} containing the
     *                definition of the resource to create
     * @return a new created {@link Resource} instance
     * @throws InvalidResourceException if an error occurred while instantiating the new
     *                                  {@link Resource}
     */
    public ResourceImpl addResource(ResourceBuilder builder) throws InvalidResourceException {
        ExtResourceImpl resource = (ExtResourceImpl) super.addResource(builder);
        if (resource != null) {
            ExtResourceConfig resourceConfig = (ExtResourceConfig) builder.getResourceConfig();
            resource.buildMethod(resourceConfig, this);
        }
        return resource;
    }

    /**
     * Creates and registers the {@link AccessMethodTrigger}(s) described
     * by the set of {@link ReferenceDefinition}s passed as parameter, and
     * to associate to the {@link AccessMethod}'s  {@link Signature} also passed
     * as parameter
     *
     * @param resourceName the name of the {@link ActionResource} whose invocation triggers the
     *                     {@link AccessMethodTrigger}(s) to create
     * @param signature    the {@link Signature} to which to link the {@link AccessMethodTrigger}(s)
     *                     to instantiate
     * @param references   the list of {@link ReferenceDefinition} describing the
     *                     {@link AccessMethodTrigger}(s) to create
     */
    protected void buildTriggers(String resourceName, Signature signature, List<ReferenceDefinition> references) {
        if (signature.getName().intern() != AccessMethod.ACT && signature.getName().intern() != AccessMethod.SET) {
            LOG.debug("Trigger allowed for ACT and SET methods only");
            return;
        }
        int index = 0;
        int length = references == null ?0:references.size();
        AccessMethodTriggerFactory.Loader loader = AccessMethodTriggerFactory.LOADER.get();
        try {
            for (; index < length; index++) {
                JsonObject referenceJson =  JsonProviderFactory.readObject(references.get(index).getJSON());
                JsonObject triggerJson = referenceJson.getJsonObject(AccessMethodTrigger.TRIGGER_KEY);

                AccessMethodTriggerFactory factory = loader.load(super.modelInstance.mediator(), 
                		triggerJson.getString(AccessMethodTrigger.TRIGGER_TYPE_KEY));
                
                super.addTrigger(resourceName, referenceJson.getString("reference"), signature, 
                	factory.newInstance(super.modelInstance.mediator(), triggerJson), 
                	ExecutionPolicy.AFTER);
            }
        } catch (Exception e) {
        	e.printStackTrace();
            LOG.error(e.getMessage(), e);

        } finally {
            AccessMethodTriggerFactory.LOADER.remove();
        }
    }
}
