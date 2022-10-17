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

import org.eclipse.sensinact.gateway.app.api.function.DataItf;
import org.eclipse.sensinact.gateway.app.manager.component.data.Data;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author Rémi Druilhe
 */
public abstract class AbstractDataProvider implements DataProviderItf {
    private final String uri;
    private final Map<DataListenerItf, Set<Constraint>> listeners;
    private Data data;

    AbstractDataProvider(String uri) {
        this.uri = uri;
        this.listeners = new HashMap<DataListenerItf, Set<Constraint>>();
        this.data = null;
    }

    /**
     * Get the URI of this data provider
     *
     * @return the URI
     */
    public String getUri() {
        return uri;
    }

    /**
     * Get the {@link Data} of this block
     *
     * @return the {@link Data}
     */
    public DataItf getData() {
        return data;
    }

    public abstract Class<?> getDataType();

    /**
     * Add a new listener to the notifier list
     *
     * @param listener    the listener to notify
     * @param constraints the possible constraints of the subscription
     */
    public void addListener(DataListenerItf listener, Set<Constraint> constraints) {
        listeners.put(listener, constraints);
    }

    /**
     * Remove a listener from the notifier list
     *
     * @param listener the listener to notify
     */
    public void removeListener(DataListenerItf listener) {
        listeners.remove(listener);
    }

    /**
     * Update the value of the {@link DataProvider} and notify the {@link DataListenerItf} of this modification
     *
     * @param eventUuid the uuid of the event
     * @param value     the value to set in the {@link DataProvider}
     */
    public void updateAndNotify(UUID eventUuid, Object value, List<String> route) {
        this.data = new Data(new UUID(System.currentTimeMillis(), System.currentTimeMillis()), uri, getDataType(), value, System.currentTimeMillis());

        route.add(uri);
        Event event = new Event(eventUuid, data, route);
        // Notify the listeners
        for (Map.Entry<DataListenerItf, Set<Constraint>> listener : listeners.entrySet()) {
            boolean comply = true;
            if (listener.getValue() != null) {
                for (Constraint constraint : listener.getValue()) {
                    if (!constraint.complies(value)) {
                        comply = false;
                        break;
                    }
                }
            }
            if (comply) {
                listener.getKey().eventNotification(event);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractDataProvider that = (AbstractDataProvider) o;
        return uri != null ? uri.equals(that.uri) : that.uri == null;
    }

    @Override
    public int hashCode() {
        return uri != null ? uri.hashCode() : 0;
    }
}
