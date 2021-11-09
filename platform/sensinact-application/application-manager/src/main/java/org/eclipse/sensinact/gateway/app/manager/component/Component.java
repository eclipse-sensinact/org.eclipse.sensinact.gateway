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
package org.eclipse.sensinact.gateway.app.manager.component;

import org.eclipse.sensinact.gateway.app.api.exception.ApplicationRuntimeException;
import org.eclipse.sensinact.gateway.app.api.exception.LifeCycleException;
import org.eclipse.sensinact.gateway.app.api.function.AbstractFunction;
import org.eclipse.sensinact.gateway.app.api.function.DataItf;
import org.eclipse.sensinact.gateway.app.api.function.FunctionUpdateListener;
import org.eclipse.sensinact.gateway.app.manager.component.data.ConstantData;
import org.eclipse.sensinact.gateway.app.manager.component.data.ResourceData;
import org.eclipse.sensinact.gateway.app.manager.component.property.AbstractPropertyBlock;
import org.eclipse.sensinact.gateway.app.manager.json.AppJsonConstant;
import org.eclipse.sensinact.gateway.app.manager.json.AppParameter;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The component is the core concept of the AppManager. A component is in charge of listening for {@link DataProviderItf}
 * from {@link Resource} or from others {@link Component}. A component is also in
 * charge of processing a data through an {@link AbstractFunction} provided by the plugins. And finally, the result
 * of this processing is sent to component listening for it.
 *
 * @author Rémi Druilhe
 */
public class Component implements DataListenerItf, FunctionUpdateListener {
    private final AppServiceMediator mediator;
    private final String identifier;
    private final Map<String, DataProviderSubscription> inputDataProviderMap;
    private final AbstractFunction<?> function;
    private final List<AppParameter> functionParameters;
    private final Map<String, AbstractPropertyBlock> propertyBlocks;
    private final Map<String, DataProvider> outputDataProviderMap;
    private Session session;
    private Event lastEvent;

    /**
     * Constructor of the Component object
     *
     * @param mediator              the mediator to the sNa core
     * @param identifier            the URI of the component
     * @param inputDataProviderMap  the data provider listeners
     * @param function              the {@link AbstractFunction} provided by the plugins
     * @param functionParameters    the parameters of the function
     * @param propertyBlocks        the properties of the component
     * @param outputDataProviderMap the {@link DataProvider} of a component
     */
    public Component(AppServiceMediator mediator, String identifier, Map<String, DataProviderSubscription> inputDataProviderMap, AbstractFunction<?> function, List<AppParameter> functionParameters, Map<String, AbstractPropertyBlock> propertyBlocks, Map<String, DataProvider> outputDataProviderMap) {
        this.mediator = mediator;
        this.identifier = identifier;
        this.inputDataProviderMap = inputDataProviderMap;
        this.functionParameters = functionParameters;
        this.function = function;
        this.propertyBlocks = propertyBlocks;
        this.outputDataProviderMap = outputDataProviderMap;
    }

    /**
     * Instantiate the component when the application is started
     *
     * @throws LifeCycleException when a problem occurs in the instantiation of the component
     */
    public void instantiate(Session session) throws LifeCycleException, ApplicationRuntimeException {
        /*System.out.println("Instantiating the component " + identifier);
        System.out.println("Number of listeners: " + inputDataProviderMap.size());*/
        if (session == null) {
            throw new ApplicationRuntimeException("User session does not exist");
        }
        this.session = session;
        // Register the listener to the DataProviders
        for (Map.Entry<String, DataProviderSubscription> map : inputDataProviderMap.entrySet()) {
            DataProviderSubscription subscription = map.getValue();
            try {
                String filter = "(&(objectClass=" + DataProviderItf.class.getName() + ")" + "(uri=" + subscription.getDataProviderUri() + "))";
                ServiceReference<?>[] serviceReferences = mediator.getServiceReferences(filter);
                if (serviceReferences.length == 0) {
                    throw new LifeCycleException("Unable to find " + subscription.getDataProviderUri() + ". " + "No service registered.");
                }
                for (ServiceReference<?> serviceReference : serviceReferences) {
                    DataProviderItf dataProviderItf = ((DataProviderItf) mediator.getService(serviceReference));
                    dataProviderItf.addListener(this, subscription.getConstraints());
                }
            } catch (Exception e) {
                throw new LifeCycleException("Unable to instantiate the component " + identifier + " > " + e.getMessage());
            }
        }
        // Instantiate the function and register the FunctionUpdateListener
        this.function.instantiate();
        this.function.setListener(this);
        // Instantiate the properties
        for (Map.Entry<String, AbstractPropertyBlock> map : propertyBlocks.entrySet()) {
            map.getValue().instantiate();
        }
    }

    /**
     * Uninstantiate the component when the application is stopped
     *
     * @throws LifeCycleException when a problem occurs in the uninstantiation of the component
     */
    public void uninstantiate() throws LifeCycleException {
        // Unregister the listener from the DataProviders
        for (Map.Entry<String, DataProviderSubscription> map : inputDataProviderMap.entrySet()) {
            DataProviderSubscription subscription = map.getValue();
            try {
                String filter = "(&(objectClass=" + DataProviderItf.class.getName() + ")" + "(uri=" + subscription.getDataProviderUri() + "))";
                ServiceReference<?>[] serviceReferences = mediator.getContext().getServiceReferences((String) null, filter);
                if (serviceReferences.length == 0) {
                    throw new LifeCycleException("Unable to find " + subscription.getDataProviderUri() + ". " + "No service registered.");
                }
                for (ServiceReference<?> serviceReference : serviceReferences) {
                    DataProviderItf dataProviderItf = ((DataProviderItf) mediator.getService(serviceReference));
                    dataProviderItf.removeListener(this);
                }
            } catch (Exception e) {
                throw new LifeCycleException("Unable to uninstantiate the component " + identifier + " > " + e.getMessage());
            }
        }
        // Uninstantiate the function and unregister the FunctionUpdateListener
        this.function.removeListener(this);
        this.function.uninstantiate();
        // Uninstantiate the properties
        for (Map.Entry<String, AbstractPropertyBlock> map : propertyBlocks.entrySet()) {
            map.getValue().uninstantiate();
        }
        this.session = null;
    }

    /**
     * Pre-processing of the function. This method is called when a new event occurs.
     *
     * @param event the event received by the component
     */
    public void eventNotification(Event event) {
        //System.out.println("> I am in " + identifier);
        //System.out.println("New component notification");
        List<String> eventRoute = event.getRoute();
        String firstProvider = eventRoute.get(0);
        String lastProvider = eventRoute.get(eventRoute.size() - 1);
        UUID eventUUID = event.getUuid();
        Map<String, UUID> lastUuidPerRoute = new HashMap<String, UUID>();
        lastUuidPerRoute.put(lastProvider, eventUUID);
        /*System.out.println("EventUUID: " + eventUUID);
        System.out.println("LastProvider: " + lastProvider);*/
        // Tests that the component is allowed to process the AbstractFunction: tests that, if there is multiple
        // listeners, the Data has been processed by all the others Components that have to process it.
        for (Map.Entry<String, DataProviderSubscription> map : inputDataProviderMap.entrySet()) {
            // Get an AbstractDataListener that is not the one from which the Event comes
            if (!map.getKey().equals(event.getData().getSourceUri())) {
                List<List<String>> listenerRoutes = map.getValue().getRoutes();
                for (List<String> listenerRoute : listenerRoutes) {
                    /*System.out.println("Route: " + listenerRoute);
                    System.out.println(" - First provider: " + listenerRoute.get(0));
                    System.out.println(" - Last provider: " + listenerRoute.get(listenerRoute.size() - 1));*/
                    // Test that the first provider of the event matches the first provider of an other route and
                    // the last provider of the event is different from the last provider of the other route.
                    // This means that the event can take two routes from a single data provider to reach the component.
                    if (listenerRoute.get(0).equals(firstProvider) && !listenerRoute.get(listenerRoute.size() - 1).equals(lastProvider)) {
                        // Test that the UUID from the two routes are the same (meaning that the processing on the
                        // others routes for the same resource event as been done)
                        if (lastUuidPerRoute.containsKey(map.getKey())) {
                            /*System.out.println("  - LastUUIDPerRoute: " + map.getKey() + " - " +
                                    lastUuidPerRoute.get(map.getKey()));*/
                            if (lastUuidPerRoute.get(map.getValue().getDataProviderUri()).compareTo(eventUUID) != 0) {
                                //System.out.println("   - Returning");
                                return;
                            }
                        }
                    }
                }
            }
        }
        lastEvent = event;
        // Retrieve the Data from the resources/variable/event/constant to create the
        // list of parameters of the AbstractFunction
        List<DataItf> dataList = new ArrayList<DataItf>();
        for (AppParameter parameter : functionParameters) {
            DataItf data = null;
            //TODO: test if the event data fit one of the parameters to avoid reading the data from the registry?
            if (parameter.getType().equals(AppJsonConstant.TYPE_RESOURCE)) {
                data = new ResourceData(session, (String) parameter.getValue());
            } else if (parameter.getType().equals(AppJsonConstant.TYPE_VARIABLE)) {
                String filter = "(&(objectClass=" + DataProviderItf.class.getName() + ")" + "(uri=" + parameter.getValue() + "))";
                ServiceReference<?>[] serviceReferences = mediator.getServiceReferences(filter);
                data = ((DataProviderItf) mediator.getService(serviceReferences[0])).getData();
            } else if (parameter.getType().equals(AppJsonConstant.TYPE_EVENT)) {
                if (event.getData().getSourceUri().matches((String) parameter.getValue())) {
                    data = event.getData();
                } else {
                	String filter = "(&(objectClass=" + DataProviderItf.class.getName() + ")" + "(uri=" + parameter.getValue() + "))";
	                ServiceReference<?>[] serviceReferences = mediator.getServiceReferences(filter);
	                data = ((DataProviderItf) mediator.getService(serviceReferences[0])).getData();
                }
            } else if (CastUtils.jsonTypeToJavaType(parameter.getType()) != null) {
                data = new ConstantData(parameter.getValue(), CastUtils.jsonTypeToJavaType(parameter.getType()));
            }
            /*if(data == null) {
                try {
                    throw new ApplicationRuntimeException("Parameter type " + parameter.getType() + " not found for " +
                            "parameter " + parameter.getValue());
                } catch (ApplicationRuntimeException e) {
                    e.printStackTrace();
                    return;
                }
            }*/
            dataList.add(data);
        }
        // Trigger the AbstractFunction with the given parameters
        try {
            function.process(dataList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to update the result of a processing. Because the {@link AbstractFunction} can be asynchronous,
     * i.e., it provides results even when there is no event to trigger the component, this method has to be called
     * directly from the {@link AbstractFunction}.
     *
     * @param result the result of the processing of the function
     */
    public void updatedResult(Object result) {
        //System.out.println("----> Updating result: " + result);
        UUID uuid = lastEvent.getUuid();
        List<String> sources = lastEvent.getRoute();
        if (function.isAsynchronous()) {
            uuid = UUID.randomUUID();
            sources = new ArrayList<String>();
        }
        try {
            outputDataProviderMap.get(ComponentConstant.RESULT_DATA).updateAndNotify(uuid, result, sources);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get a specific property from the component
     *
     * @param property the property name
     * @return the requested property
     */
    public AbstractPropertyBlock getProperty(String property) {
        return propertyBlocks.get(property);
    }

    public List<AppParameter> getFunctionParameters() {
        return functionParameters;
    }
}
