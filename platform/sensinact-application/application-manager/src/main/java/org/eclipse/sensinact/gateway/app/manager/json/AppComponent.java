/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.manager.json;

import org.eclipse.sensinact.gateway.app.api.exception.FunctionNotFoundException;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a component
 *
 * @author Rémi Druilhe
 */
public class AppComponent implements JSONable {
    private final String identifier;
    private final List<AppEvent> events;
    private final AppFunction function;
    private final ComponentProperties properties;

    /**
     * Java constructor of a component
     *
     * @param identifier the identifier of the component
     * @param events     the events that trigger the function of the component
     * @param function   the function to getFunctionBlock
     * @param properties the properties of the component
     */
    public AppComponent(String identifier, List<AppEvent> events, AppFunction function, ComponentProperties properties) throws FunctionNotFoundException {
        this.identifier = identifier;
        this.events = events;
        this.function = function;
        this.properties = properties;
    }

    /**
     * JSON constructor for a component
     *
     * @param mediator  the mediator
     * @param component the JSON component
     */
    public AppComponent(AppServiceMediator mediator, JSONObject component) throws FunctionNotFoundException {
        this.identifier = component.getString(AppJsonConstant.APP_IDENTIFIER);
        this.events = new ArrayList<AppEvent>();
        JSONArray eventArray = component.getJSONArray(AppJsonConstant.APP_EVENTS);
        for (int i = 0; i < eventArray.length(); i++) {
            events.add(new AppEvent(mediator, eventArray.getJSONObject(i)));
        }
        this.function = new AppFunction(component.getJSONObject(AppJsonConstant.APP_FUNCTION));
        ComponentProperties.Builder propertiesBuilder = new ComponentProperties.Builder();
        if (component.has(AppJsonConstant.APP_PROPERTIES)) {
            JSONObject propertiesJson = component.getJSONObject(AppJsonConstant.APP_PROPERTIES);
            if (propertiesJson.has(AppJsonConstant.APP_PROPERTIES_REGISTER)) {
                propertiesBuilder.register(propertiesJson.getBoolean(AppJsonConstant.APP_PROPERTIES_REGISTER));
            }
        }
        this.properties = propertiesBuilder.build();
    }

    /**
     * Get the events that trigger the component
     *
     * @return the events
     */
    public List<AppEvent> getEvents() {
        return events;
    }

    /**
     * Get the function as a {@link String}
     *
     * @return the function
     */
    public AppFunction getFunction() {
        return function;
    }

    /**
     * Get the properties of the component
     *
     * @return the properties of the component
     */
    public ComponentProperties getProperties() {
        return properties;
    }

    /**
     * Get the identifier of the component
     *
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @see JSONable#getJSON()
     */
    public String getJSON() {
        JSONObject component = new JSONObject();
        component.put(AppJsonConstant.APP_IDENTIFIER, identifier);
        JSONArray eventArray = new JSONArray();
        for (AppEvent event : events) {
            eventArray.put(event.getJSON());
        }
        component.put(AppJsonConstant.APP_EVENTS, eventArray);
        component.put(AppJsonConstant.APP_FUNCTION, function.getJSON());
        component.put(AppJsonConstant.APP_PROPERTIES, properties.getJSON());
        return component.toString();
    }
}
