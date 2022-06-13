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
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.spi.JsonProvider;

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
    public AppComponent(AppServiceMediator mediator, JsonObject component) throws FunctionNotFoundException {
        this.identifier = component.getString(AppJsonConstant.APP_IDENTIFIER);
        this.events = new ArrayList<AppEvent>();
        JsonArray eventArray = component.getJsonArray(AppJsonConstant.APP_EVENTS);
        for (int i = 0; i < eventArray.size(); i++) {
            events.add(new AppEvent(mediator, eventArray.getJsonObject(i)));
        }
        this.function = new AppFunction(component.getJsonObject(AppJsonConstant.APP_FUNCTION));
        ComponentProperties.Builder propertiesBuilder = new ComponentProperties.Builder();
        if (component.containsKey(AppJsonConstant.APP_PROPERTIES)) {
            JsonObject propertiesJson = component.getJsonObject(AppJsonConstant.APP_PROPERTIES);
            if (propertiesJson.containsKey(AppJsonConstant.APP_PROPERTIES_REGISTER)) {
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
        JsonProvider provider = JsonProviderFactory.getProvider();
		JsonObjectBuilder component = provider.createObjectBuilder();
        component.add(AppJsonConstant.APP_IDENTIFIER, identifier);
        JsonArrayBuilder eventArray = provider.createArrayBuilder();
        for (AppEvent event : events) {
            eventArray.add(event.getJSON());
        }
        component.add(AppJsonConstant.APP_EVENTS, eventArray);
        component.add(AppJsonConstant.APP_FUNCTION, function.getJSON());
        component.add(AppJsonConstant.APP_PROPERTIES, properties.getJSON());
        return component.build().toString();
    }
}
