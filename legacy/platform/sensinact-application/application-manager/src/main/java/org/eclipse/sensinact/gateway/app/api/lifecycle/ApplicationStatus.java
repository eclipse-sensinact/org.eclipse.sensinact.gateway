/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.api.lifecycle;

import org.eclipse.sensinact.gateway.app.manager.AppConstant;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;

/**
 * This class represents the lifecycle of the application. The lifecycle works as follow:
 * <p>
 * ------> INSTALLED ------uninstall-----> UNINSTALLED
 * |           |                               ^
 * |           |<------------------------      |
 * |         start                      |      |
 * |           |                        |      |
 * stop          v                        |      |
 * |       RESOLVING -------            |   uninstall
 * |           |           |            |      |
 * |           |           |            |      |
 * |           v           v            |      |
 * -------- ACTIVE -----exception----> UNRESOLVED
 *
 * @author Remi Druilhe
 */
public enum ApplicationStatus implements JSONable {
    INSTALLED("INSTALLED"), RESOLVING("RESOLVING"), UNRESOLVED("UNRESOLVED"), ACTIVE("ACTIVE"), UNINSTALLED("UNINSTALLED");
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
