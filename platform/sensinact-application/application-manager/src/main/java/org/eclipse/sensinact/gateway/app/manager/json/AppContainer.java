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
package org.eclipse.sensinact.gateway.app.manager.json;

import org.eclipse.sensinact.gateway.app.api.exception.FunctionNotFoundException;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * This class wraps all the required information to create an application, i.e., the components, the options, ...
 *
 * @author Rémi Druilhe
 */
public class AppContainer implements JSONable {
	
	private static final Logger LOG = LoggerFactory.getLogger(AppContainer.class);
    private final String applicationName;
    private final AppInitialize initialize;
    private final List<AppComponent> components;
    private final AppFinalize finalize;

    /**
     * JSON constructor of an application
     *
     * @param mediator        the mediator
     * @param applicationName the name of the application
     * @param content         the application as a JSON object
     */
    public AppContainer(AppServiceMediator mediator, String applicationName, JSONObject content) {
        this.applicationName = applicationName;
        this.initialize = new AppInitialize(content.has(AppJsonConstant.INITIALIZE)
        		?content.optJSONObject(AppJsonConstant.INITIALIZE):new JSONObject());
        this.components = new ArrayList<AppComponent>();
        this.finalize = new AppFinalize(content.has(AppJsonConstant.FINALIZE)
        		?content.optJSONObject(AppJsonConstant.FINALIZE):new JSONObject());
        JSONArray componentArray = content.optJSONArray("application");
        for (int i = 0; i < componentArray.length(); i++) {
            AppComponent component;
            try {
                component = new AppComponent(mediator, componentArray.getJSONObject(i));
            } catch (FunctionNotFoundException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Unable to create the component", e);
                }
                return;
            }
            components.add(component);
        }
    }

    /**
     * Get the name of the application
     *
     * @return the name of the application
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * Get the initialize object from the container
     *
     * @return the initialize object
     */
    public AppInitialize getInitialize() {
        return initialize;
    }

    /**
     * Get the components list from the container
     *
     * @return the components list
     */
    public List<AppComponent> getComponents() {
        return components;
    }

    /**
     * Get the finalize object from the container
     *
     * @return the initialize object
     */
    public AppFinalize getFinalize() {
        return finalize;
    }

    /**
     * Extract all the resources from the AppContainer
     *
     * @return the collection of the URI of the resources
     */
    public Collection<String> getResourceUris() {
        Collection<String> resourceUris = new HashSet<String>();
        for (AppComponent component : this.getComponents()) {
            for (AppEvent event : component.getEvents()) {
                if (AppEvent.EventType.RESOURCE.equals(event.getType())) {
                    resourceUris.add(event.getUri());
                }
            }
            for (AppParameter parameter : component.getFunction().getRunParameters()) {
                if (AppJsonConstant.TYPE_RESOURCE.equals(parameter.getType())) {
                    resourceUris.add((String) parameter.getValue());
                }
            }
        }
        return resourceUris;
    }

    /**
     * @see JSONable#getJSON()
     */
    public String getJSON() {
        JSONObject application = new JSONObject();
        application.put(AppJsonConstant.INITIALIZE, initialize.getJSON());
        JSONArray componentArray = new JSONArray();
        for (AppComponent component : components) {
            componentArray.put(component.getJSON());
        }
        application.put(AppJsonConstant.APPLICATION, componentArray);
        application.put(AppJsonConstant.FINALIZE, finalize.getJSON());
        return application.toString();
    }
}
