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
package org.eclipse.sensinact.gateway.app.manager.factory;

import org.eclipse.sensinact.gateway.app.api.exception.ApplicationFactoryException;
import org.eclipse.sensinact.gateway.app.api.function.AbstractFunction;
import org.eclipse.sensinact.gateway.app.manager.component.Component;
import org.eclipse.sensinact.gateway.app.manager.component.DataProvider;
import org.eclipse.sensinact.gateway.app.manager.component.DataProviderSubscription;
import org.eclipse.sensinact.gateway.app.manager.component.property.AbstractPropertyBlock;
import org.eclipse.sensinact.gateway.app.manager.json.AppParameter;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class enables to create a {@link Component} in an asynchronous way.
 *
 * @author Rémi Druilhe
 */
class ComponentBuilder {
    private final AppServiceMediator mediator;
    private final String identifier;
    private final AbstractFunction function;
    private final Map<String, DataProvider> dataProviderMap;
    private Map<String, DataProviderSubscription> abstractDataListenerMap;
    private List<AppParameter> parameters;
    private Map<String, AbstractPropertyBlock> propertyBlocks;

    /**
     * Constructor of the builder
     *
     * @param mediator        the mediator
     * @param identifier      the identifier of the component
     * @param function        the function hosted by the component
     * @param parameters      the parameters of the function
     * @param dataProviderMap the data providers
     */
    ComponentBuilder(AppServiceMediator mediator, String identifier, AbstractFunction function, List<AppParameter> parameters, Map<String, DataProvider> dataProviderMap) {
        this.mediator = mediator;
        this.identifier = identifier;
        this.function = function;
        this.parameters = parameters;
        this.dataProviderMap = dataProviderMap;
        this.abstractDataListenerMap = new HashMap<String, DataProviderSubscription>();
        this.propertyBlocks = new HashMap<String, AbstractPropertyBlock>();
    }

    /**
     * Add an event source to the component
     *
     * @param source       the URI of the {@link DataProviderItf}
     * @param subscription the {@link DataProviderSubscription} detailing the subscription
     * @return the {@link ComponentBuilder}
     */
    ComponentBuilder addEvent(String source, DataProviderSubscription subscription) {
        this.abstractDataListenerMap.put(source, subscription);
        return this;
    }

    /**
     * Add an {@link AbstractPropertyBlock} to the component
     *
     * @param property      the property name
     * @param propertyBlock the property block
     * @return the {@link ComponentBuilder}
     */
    ComponentBuilder addProperty(String property, AbstractPropertyBlock propertyBlock) {
        this.propertyBlocks.put(property, propertyBlock);
        return this;
    }

    /**
     * Build the component from the information previously provided.
     * An exception is thrown if something is missing to create the component.
     *
     * @return the built component
     * @throws ApplicationFactoryException when the build of the component failed
     */
    public Component build() throws ApplicationFactoryException {
        if (function == null) {
            throw new ApplicationFactoryException("Function is not defined");
        }
        if (abstractDataListenerMap.isEmpty()) {
            throw new ApplicationFactoryException("No listeners set in the component");
        }
        return new Component(mediator, identifier, abstractDataListenerMap, function, parameters, propertyBlocks, dataProviderMap);
    }
}
