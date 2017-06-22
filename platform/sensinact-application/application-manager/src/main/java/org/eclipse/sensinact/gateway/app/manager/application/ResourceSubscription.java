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
package org.eclipse.sensinact.gateway.app.manager.application;

import org.eclipse.sensinact.gateway.app.manager.json.AppCondition;

import java.util.Set;

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
}
