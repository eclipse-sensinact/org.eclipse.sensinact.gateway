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
package org.eclipse.sensinact.gateway.app.api.persistence;

import org.eclipse.sensinact.gateway.app.api.persistence.dao.Application;
import org.eclipse.sensinact.gateway.app.api.persistence.exception.ApplicationPersistenceException;
import org.eclipse.sensinact.gateway.app.api.persistence.listener.ApplicationAvailabilityListener;
import org.json.JSONObject;

import java.util.Collection;

public interface ApplicationPersistenceService extends Runnable {
    void persist(Application application) throws ApplicationPersistenceException;

    void delete(String applicationName) throws ApplicationPersistenceException;

    JSONObject fetch(String applicationName) throws ApplicationPersistenceException;

    Collection<Application> list();

    void registerServiceAvailabilityListener(ApplicationAvailabilityListener listener);

    void unregisterServiceAvailabilityListener(ApplicationAvailabilityListener listener);
}
