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
import org.eclipse.sensinact.gateway.app.api.exception.FunctionNotFoundException;
import org.eclipse.sensinact.gateway.app.api.function.AbstractFunction;
import org.eclipse.sensinact.gateway.app.manager.application.Application;
import org.eclipse.sensinact.gateway.app.manager.application.ApplicationService;
import org.eclipse.sensinact.gateway.app.manager.application.ResourceSubscription;
import org.eclipse.sensinact.gateway.app.manager.component.AbstractDataProvider;
import org.eclipse.sensinact.gateway.app.manager.component.Component;
import org.eclipse.sensinact.gateway.app.manager.component.ComponentConstant;
import org.eclipse.sensinact.gateway.app.manager.component.DataProvider;
import org.eclipse.sensinact.gateway.app.manager.component.DataProviderItf;
import org.eclipse.sensinact.gateway.app.manager.component.DataProviderSubscription;
import org.eclipse.sensinact.gateway.app.manager.component.ResourceDataProvider;
import org.eclipse.sensinact.gateway.app.manager.component.property.RegisterPropertyBlock;
import org.eclipse.sensinact.gateway.app.manager.json.AppComponent;
import org.eclipse.sensinact.gateway.app.manager.json.AppContainer;
import org.eclipse.sensinact.gateway.app.manager.json.AppEvent;
import org.eclipse.sensinact.gateway.app.manager.json.AppJsonConstant;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.eclipse.sensinact.gateway.app.manager.osgi.PluginsProxy;
import org.eclipse.sensinact.gateway.app.manager.watchdog.AppExceptionWatchDog;
import org.eclipse.sensinact.gateway.core.InvalidResourceException;
import org.eclipse.sensinact.gateway.core.SensorDataResource;
import org.eclipse.sensinact.gateway.core.ServiceImpl;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class create an application, i.e., components and bindings, from the {@link AppContainer}.
 *
 * @author Rémi Druilhe
 */
public class ApplicationFactory {
	
	private static final Logger LOG = LoggerFactory.getLogger(ApplicationFactory.class);
    /**
     * Create the application, i.e., components and bindings, from the {@link AppContainer}.
     *
     * @param mediator  the mediator to access to the {@link org.osgi.framework.BundleContext}
     * @param container the model of the application
     * @param service   the {@link ApplicationService} used to register
     *                  new {@link Resource} from the output of the components
     * @return the built application
     * @throws ApplicationFactoryException when the factory failed to create the {@link Application}
     */
    public static Application createApplication(AppServiceMediator mediator, AppContainer container, ServiceImpl service) throws ApplicationFactoryException {
        final String applicationUri = AppJsonConstant.URI_SEPARATOR + container.getApplicationName();
        Map<String, Node> nodeMap = new HashMap<String, Node>();
        Map<String, AbstractDataProvider> allDataProvidersMap = new HashMap<String, AbstractDataProvider>();
        Map<ResourceDataProvider, Collection<ResourceSubscription>> resourceSubscriptions = new HashMap<ResourceDataProvider, Collection<ResourceSubscription>>();
        // Construct the different ComponentBuilder
        Map<String, ComponentBuilder> componentBuilderMap = new HashMap<String, ComponentBuilder>();
        List<ServiceRegistration<DataProviderItf>> dataProvidersRegistration = new ArrayList<ServiceRegistration<DataProviderItf>>();
        for (AppComponent component : container.getComponents()) {
            final String componentUri = applicationUri + AppJsonConstant.URI_SEPARATOR + component.getIdentifier();
            final String resultUri = componentUri + AppJsonConstant.URI_SEPARATOR + ComponentConstant.RESULT_DATA;
            // Construct the function of the component
            AbstractFunction<?> function;
            try {
                function = PluginsProxy.getFunction(mediator, component.getFunction());
            } catch (FunctionNotFoundException e) {
                ApplicationFactoryException exception = new ApplicationFactoryException("Unable to create application " + container.getApplicationName() + ". " + e.getMessage());
                if (LOG.isErrorEnabled()) {
                    LOG.error("Unable to create application " + container.getApplicationName() + ". " + e.getMessage(), exception);
                }
                throw exception;
            }
            // Getting the result's type of the function
            Class<?> currentClass = function.getClass();
            ParameterizedType superType = null;
            while (superType == null) {
                Type superClass = currentClass.getGenericSuperclass();
                if (superClass == null) {
                    break;
                }
                try {
                    superType = (ParameterizedType) superClass;
                } catch (ClassCastException e) {
                    currentClass = currentClass.getSuperclass();
                }
            }
            if (superType == null) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Instance creation error", new RuntimeException("Unable to instantiate: " + function.getClass()));
                }
                return null;
            }
            Type argumentType = superType.getActualTypeArguments()[0];
            if (ParameterizedType.class.isAssignableFrom(argumentType.getClass())) {
                argumentType = ((ParameterizedType) argumentType).getRawType();
            }
            nodeMap.put(resultUri, new Node(resultUri, function.isAsynchronous()));
            // Construct the DataProviders (i.e., the result of the functions)
            DataProvider resultData = new DataProvider(resultUri, (Class<?>) argumentType);
            Dictionary<String, String> resultProperties = new Hashtable<String, String>();
            resultProperties.put("application", container.getApplicationName());
            resultProperties.put("type", ((Class<?>) argumentType).getCanonicalName());
            resultProperties.put("uri", resultUri);
            dataProvidersRegistration.add(mediator.registerService(DataProviderItf.class, resultData, resultProperties));
            Map<String, DataProvider> componentDataProviderMap = new HashMap<String, DataProvider>();
            componentDataProviderMap.put(ComponentConstant.RESULT_DATA, resultData);
            allDataProvidersMap.put(resultUri, resultData);
            ComponentBuilder builder = new ComponentBuilder(mediator, component.getIdentifier(), function, component.getFunction().getRunParameters(), componentDataProviderMap);
            componentBuilderMap.put(componentUri, builder);
            // Construct the set of resource's subscriptions.
            for (AppEvent event : component.getEvents()) {
                if (event.getType().equals(AppEvent.EventType.RESOURCE)) {
                    String resourceUri = event.getUri();
                    ResourceDataProvider dataProvider = new ResourceDataProvider(resourceUri);
                    allDataProvidersMap.put(resourceUri, dataProvider);
                    Dictionary<String, String> dataProviderProperties = new Hashtable<String, String>();
                    dataProviderProperties.put("application", container.getApplicationName());
                    //dataProviderProperties.put("type", resourceType.getCanonicalName());
                    dataProviderProperties.put("uri", resourceUri);
                    dataProvidersRegistration.add(mediator.registerService(DataProviderItf.class, dataProvider, dataProviderProperties));
                    if (!resourceSubscriptions.containsKey(dataProvider)) {
                        Set<ResourceSubscription> resourceSubscriptionsSet = new HashSet<ResourceSubscription>();
                        resourceSubscriptionsSet.add(new ResourceSubscription(resourceUri, event.getConditions()));
                        resourceSubscriptions.put(dataProvider, resourceSubscriptionsSet);
                    } else {
                        // Test if the constraints are the same for the data provider
                        boolean constraintExist = false;
                        for (ResourceSubscription resourceSubscription : resourceSubscriptions.get(dataProvider)) {
                            /*System.out.println(event.getCondition());
                            System.out.println(resourceSubscription.getCondition());*/
                            if (event.getConditions().isEmpty() && resourceSubscription.getConditions().isEmpty()) {
                                constraintExist = true;
                                break;
                            } else if (event.getConditions().containsAll(resourceSubscription.getConditions()) && resourceSubscription.getConditions().containsAll(event.getConditions())) {
                                constraintExist = true;
                                break;
                            }
                        }
                        if (constraintExist) {
                            break;
                        }
                        resourceSubscriptions.get(dataProvider).add(new ResourceSubscription(resourceUri, event.getConditions()));
                    }
                    nodeMap.put(resourceUri, new Node(resourceUri, true));
                }
            }
        }
        // Construct the relations between the different Nodes, i.e., create the graph that link the components
        for (AppComponent component : container.getComponents()) {
            String componentUri = applicationUri + AppJsonConstant.URI_SEPARATOR + component.getIdentifier();
            String resultUri = componentUri + AppJsonConstant.URI_SEPARATOR + ComponentConstant.RESULT_DATA;
            for (AppEvent event : component.getEvents()) {
                nodeMap.get(resultUri).addNode(nodeMap.get(event.getUri()));
            }
        }
        // Construct the bindings between the components
        for (AppComponent component : container.getComponents()) {
            String componentUri = applicationUri + AppJsonConstant.URI_SEPARATOR + component.getIdentifier();
            String resultUri = componentUri + AppJsonConstant.URI_SEPARATOR + ComponentConstant.RESULT_DATA;
            //System.out.println("Component: " + componentUri);
            ComponentBuilder builder = componentBuilderMap.get(componentUri);
            // Bind the DataListeners of the components to the DataProviders
            for (AppEvent event : component.getEvents()) {
                String providerUri = event.getUri();
                List<List<String>> routes = nodeMap.get(resultUri).getRoutes();
                //System.out.println("Routes: " + routes);
                builder.addEvent(providerUri, new DataProviderSubscription(providerUri, event.getConditions(), routes));
                /*if (event.getVariable().getType().equals(AppJsonConstant.TYPE_RESOURCE)) {
                    System.out.println("Sizeee route: " + nodeMap.get(componentUri).getRoutes().size());
                    DataProviderSubscription subscription = new DataProviderSubscription(
                            (String) event.getVariable().getValue(),
                            event.getCondition(),
                            nodeMap.get(componentUri).getRoutes());
                        builder.addEvent((String) event.getVariable().getValue(), subscription);
                } else if (event.getVariable().getType().equals(AppJsonConstant.TYPE_VARIABLE)) {
                    System.out.println("Sizeee route: " + nodeMap.get(componentUri).getRoutes().size());
                    DataProviderSubscription subscription = new DataProviderSubscription(
                            (String) event.getVariable().getValue(),
                            event.getCondition(),
                            nodeMap.get(componentUri).getRoutes());
                    builder.addEvent((String) event.getVariable().getValue(), subscription);
                } else {
                    ApplicationFactoryException exception = new ApplicationFactoryException(
                            "Unable to create event listener: unable to find type: " + event.getVariable().getType());
                    if (LOG.isErrorEnabled()) {
                        LOG.error(exception.getMessage(), exception);
                    }
                    throw exception;
                }*/
            }
        }
        // Create the properties of a component
        for (AppComponent component : container.getComponents()) {
            String componentUri = AppJsonConstant.URI_SEPARATOR + container.getApplicationName() + AppJsonConstant.URI_SEPARATOR + component.getIdentifier();
            ComponentBuilder builder = componentBuilderMap.get(componentUri);
            // Construct the properties of the component
            if (component.getProperties().getRegister()) {
                try {
                    AbstractDataProvider dataProvider = allDataProvidersMap.get(componentUri + AppJsonConstant.URI_SEPARATOR + ComponentConstant.RESULT_DATA);
                    RegisterPropertyBlock propertyBlock = new RegisterPropertyBlock(service.addDataResource(SensorDataResource.class, componentUri, dataProvider.getDataType(), null), dataProvider);
                    builder.addProperty(RegisterPropertyBlock.PROPERTY, propertyBlock);
                } catch (InvalidResourceException e) {
                    e.printStackTrace();
                }
            }
        }
        // Finally build the components from the builders
        Map<String, Component> components = new HashMap<String, Component>();
        for (Map.Entry<String, ComponentBuilder> map : componentBuilderMap.entrySet()) {
            // Create the final component from the builder
            try {
                components.put(map.getKey(), map.getValue().build());
            } catch (ApplicationFactoryException exception) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Unable to create the component", exception);
                }
                throw exception;
            }
        }
        //TODO: provide the list of all the resources that will be used by the application (sensors and actuators)
        //TODO: in order to test their existence -> check with the new core
        // Create the Application
        return new Application(mediator, container, container.getApplicationName(), dataProvidersRegistration, resourceSubscriptions, components, new AppExceptionWatchDog(mediator, (ApplicationService) service));
    }

    /**
     * A Node is a temporary data structure to determine the routes leading to a component
     */
    private static class Node {
        private final String uri;
        private final boolean isAsynchronous;
        private final List<Node> providerNodes;

        /**
         * Constructor
         *
         * @param uri            the URI of the node
         * @param isAsynchronous the asynchronous value from the function hosted by the component
         */
        Node(final String uri, final boolean isAsynchronous) {
            this.uri = uri;
            this.isAsynchronous = isAsynchronous;
            this.providerNodes = new ArrayList<Node>();
        }

        /**
         * Add a new parent node to the Node
         *
         * @param providerNode the parent node
         */
        void addNode(Node providerNode) {
            this.providerNodes.add(providerNode);
        }

        /**
         * Get the routes from the already constructed list of parent nodes
         *
         * @return the routes (starting from an asynchronous node) to reach the node.
         */
        List<List<String>> getRoutes() {
            if (isAsynchronous || providerNodes.size() == 0) {
                return new ArrayList<List<String>>() {{
                    add(new ArrayList<String>() {{
                        add(uri);
                    }});
                }};
            }
            List<List<String>> routesList = new ArrayList<List<String>>();
            for (Node node : providerNodes) {
                if (node == null) continue;
                List<List<String>> routes = node.getRoutes();
                for (List<String> route : routes) {
                    route.add(uri);
                    routesList.add(new ArrayList<String>(route));
                }
            }
            return routesList;
        }
    }
}