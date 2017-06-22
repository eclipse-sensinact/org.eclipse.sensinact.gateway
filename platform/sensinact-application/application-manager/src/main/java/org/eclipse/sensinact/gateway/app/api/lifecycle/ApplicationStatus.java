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

package org.eclipse.sensinact.gateway.app.api.lifecycle;

import org.eclipse.sensinact.gateway.app.manager.AppConstant;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;

/**
 * This class represents the lifecycle of the application. The lifecycle works as follow:
 *
 *   ------> INSTALLED ------uninstall-----> UNINSTALLED
 *   |           |                               ^
 *   |           |<------------------------      |
 *   |         start                      |      |
 *   |           |                        |      |
 * stop          v                        |      |
 *   |       RESOLVING -------            |   uninstall
 *   |           |           |            |      |
 *   |           |           |            |      |
 *   |           v           v            |      |
 *   -------- ACTIVE -----exception----> UNRESOLVED
 *
 * @author Remi Druilhe
 */
public enum ApplicationStatus implements JSONable {

    INSTALLED("INSTALLED"),
    RESOLVING("RESOLVING"),
    UNRESOLVED("UNRESOLVED"),
    ACTIVE("ACTIVE"),
    UNINSTALLED("UNINSTALLED");

    private String value;

    ApplicationStatus(String value) {
        this.value = value;
    }

    public static ApplicationStatus getNextStatus(ApplicationStatus currentStatus, String action, boolean hasErrors) {
        if (AppConstant.START.equals(action)) {
            if (!hasErrors) {
                if (ApplicationStatus.INSTALLED.equals(currentStatus) || ApplicationStatus.UNRESOLVED.equals(currentStatus)) {
                    return ApplicationStatus.RESOLVING;
                } else if (currentStatus.equals(ApplicationStatus.RESOLVING)) {
                    return ApplicationStatus.ACTIVE;
                }
            } else {
                return ApplicationStatus.UNRESOLVED;
            }
        } else if (AppConstant.UNINSTALL.equals(action)) {
            if (ApplicationStatus.INSTALLED.equals(currentStatus) || ApplicationStatus.UNRESOLVED.equals(currentStatus)) {
                return ApplicationStatus.UNINSTALLED;
            }
        } else if (AppConstant.STOP.equals(action)) {
            if (ApplicationStatus.ACTIVE.equals(currentStatus)) {
                return ApplicationStatus.INSTALLED;
            }
        } else if (AppConstant.EXCEPTION.equals(action)) {
            if (ApplicationStatus.ACTIVE.equals(currentStatus)) {
                return ApplicationStatus.UNRESOLVED;
            }
        }

        return currentStatus;
    }

    /**
     * @see JSONable#getJSON()
     */
    public String getJSON() {
        return this.value;
    }
}
