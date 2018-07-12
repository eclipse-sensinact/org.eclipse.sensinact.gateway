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
package org.eclipse.sensinact.gateway.app.api.persistence.listener;

public interface ApplicationAvailabilityListener {
    void serviceOffline();

    void serviceOnline();

    void applicationFound(String applicationName, String content);

    void applicationChanged(String applicationName, String content);

    void applicationRemoved(String applicationName);
}
