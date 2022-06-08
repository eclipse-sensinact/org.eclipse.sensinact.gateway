/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.manager.application;

import java.io.StringReader;
import java.util.Set;

import org.eclipse.sensinact.gateway.app.manager.json.AppCondition;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.spi.JsonProvider;

/**
 * @author Rémi Druilhe
 */
public class ResourceSubscription {
    private final String resourceUri;
    private final Set<AppCondition> conditions;
    private String subscriptionId;

    public ResourceSubscription(String resourceUri, Set<AppCondition> conditions) {
        this.resourceUri = resourceUri;
        this.conditions = conditions;
    }
    
    public String getResourceUri() {
        return resourceUri;
    }

    public Set<AppCondition> getConditions() {
        return conditions;
    }

    String getSubscriptionId() {
        return subscriptionId;
    }

    void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

	public JsonArray getConditionsAsJSONArray() {
        JsonProvider provider = JsonProviderFactory.getProvider();
		JsonArrayBuilder constraints = provider.createArrayBuilder();
        if (this.conditions != null) {
            for (AppCondition condition : getConditions()) {
            	constraints.add(provider
            			.createReader(new StringReader(condition.getConstraint().getJSON()))
            			.readObject());
            }
        }
        return constraints.build();
	}
}
