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

package org.eclipse.sensinact.gateway.app.manager.json;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * This class represents a subscription to a variable.
 * The subscription is realised when the application is started.
 *
 * @author Remi Druilhe
 */
public class AppEvent implements JSONable {

    public enum EventType {
        RESOURCE,
        VARIABLE
    }

    protected final String uri;
    protected final EventType type;
    protected final Set<AppCondition> conditions;

    /**
     * JSON constructor of the event subscription. It includes the variable to listen to
     * and the conditions/parameters of the subscription
     * @param mediator the mediator
     * @param json the json value of the subscription.
     */
    public AppEvent(Mediator mediator, JSONObject json) {
        this.uri = json.getString(AppJsonConstant.VALUE);

        if(AppJsonConstant.TYPE_RESOURCE.equals(json.getString(AppJsonConstant.TYPE))) {
            this.type = EventType.RESOURCE;
        } else {
            this.type = EventType.VARIABLE;
        }

        this.conditions = new HashSet<AppCondition>();

        if(json.has(AppJsonConstant.APP_EVENTS_CONDITIONS)) {
            JSONArray conditionsArray = json.getJSONArray(AppJsonConstant.APP_EVENTS_CONDITIONS);

            for(int i = 0; i < conditionsArray.length(); i++) {
                this.conditions.add(new AppCondition(mediator, conditionsArray.getJSONObject(i)));
            }
        }
    }

    /**
     * Get the uri of the provider of the events
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * Get the type of the provider of the events
     * @return the type of the provider
     */
    public EventType getType() {
        return type;
    }

    /**
     * Get the condition
     * @return the condition
     */
    public Set<AppCondition> getConditions() {
        return conditions;
    }

    /**
     * @see JSONable#getJSON()
     */
    public String getJSON() {
        JSONObject json = new JSONObject()
                .put(AppJsonConstant.VALUE, this.uri)
                .put(AppJsonConstant.TYPE, this.type);

        if(!conditions.isEmpty()) {
            JSONArray conditionsArray = new JSONArray();

            for (AppCondition condition : conditions) {
                conditionsArray.put(condition.getJSON());
            }

            json.put(AppJsonConstant.APP_EVENTS_CONDITIONS, conditionsArray);
        }

        return json.toString();
    }
}
