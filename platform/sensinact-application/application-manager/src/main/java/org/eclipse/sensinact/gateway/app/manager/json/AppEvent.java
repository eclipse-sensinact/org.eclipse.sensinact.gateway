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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.spi.JsonProvider;

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
        RESOURCE, VARIABLE
    }

    protected final String uri;
    protected final EventType type;
    protected final Set<AppCondition> conditions;

    /**
     * JSON constructor of the event subscription. It includes the variable to listen to
     * and the conditions/parameters of the subscription
     *
     * @param mediator the mediator
     * @param json     the json value of the subscription.
     */
    public AppEvent(Mediator mediator, JsonObject json) {
        this.uri = json.getString(AppJsonConstant.VALUE);
        if (AppJsonConstant.TYPE_RESOURCE.equals(json.getString(AppJsonConstant.TYPE))) {
            this.type = EventType.RESOURCE;
        } else {
            this.type = EventType.VARIABLE;
        }
        this.conditions = new HashSet<AppCondition>();
        if (json.containsKey(AppJsonConstant.APP_EVENTS_CONDITIONS)) {
            JsonArray conditionsArray = json.getJsonArray(AppJsonConstant.APP_EVENTS_CONDITIONS);
            for (int i = 0; i < conditionsArray.size(); i++) {
                this.conditions.add(new AppCondition(mediator, conditionsArray.getJsonObject(i)));
            }
        }
    }

    /**
     * Get the uri of the provider of the events
     *
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * Get the type of the provider of the events
     *
     * @return the type of the provider
     */
    public EventType getType() {
        return type;
    }

    /**
     * Get the condition
     *
     * @return the condition
     */
    public Set<AppCondition> getConditions() {
        return conditions;
    }

    /**
     * @see JSONable#getJSON()
     */
    public String getJSON() {
    	JsonProvider jp = JsonProviderFactory.getProvider();
        JsonObjectBuilder json = jp.createObjectBuilder();
        json.add(AppJsonConstant.VALUE, this.uri)
        	.add(AppJsonConstant.TYPE, this.type.name());
        if (!conditions.isEmpty()) {
            JsonArrayBuilder conditionsArray = jp.createArrayBuilder();
            for (AppCondition condition : conditions) {
                conditionsArray.add(JsonProviderFactory.readObject(jp, condition.getJSON()));
            }
            json.add(AppJsonConstant.APP_EVENTS_CONDITIONS, conditionsArray);
        }
        return json.toString();
    }
}
