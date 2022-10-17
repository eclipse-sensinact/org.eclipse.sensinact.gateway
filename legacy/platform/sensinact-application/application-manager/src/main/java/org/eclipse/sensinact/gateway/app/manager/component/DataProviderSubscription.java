/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.manager.component;

import org.eclipse.sensinact.gateway.app.manager.json.AppCondition;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Rémi Druilhe
 */
public class DataProviderSubscription {
    private final String dataProviderUri;
    private final Set<AppCondition> conditions;
    private final List<List<String>> routes;

    public DataProviderSubscription(String dataProviderUri, Set<AppCondition> conditions, List<List<String>> routes) {
        this.dataProviderUri = dataProviderUri;
        this.conditions = conditions;
        this.routes = routes;
    }

    public String getDataProviderUri() {
        return dataProviderUri;
    }

    public String getApplicationName() {
        return dataProviderUri.split("/")[1];
    }

    public String getComponentName() {
        return dataProviderUri.split("/")[2];
    }

    public String getOutputName() {
        return dataProviderUri.split("/")[3];
    }

    public Set<Constraint> getConstraints() {
        if (conditions.isEmpty()) {
            return null;
        }
        Set<Constraint> constraints = new HashSet<Constraint>();
        for (AppCondition condition : conditions) {
            constraints.add(condition.getConstraint());
        }
        return constraints;
    }

    public List<List<String>> getRoutes() {
        return routes;
    }
}
