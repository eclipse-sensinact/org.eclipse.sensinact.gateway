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

/**
 * This class represents the lifecycle of a component. The lifecycle works as follow:
 * <p>
 * +-> UNINSTANTIATED <-+
 * |                    |
 * |                    ?
 * |          +--?-- ABORTED TODO: next status should depends on the policy
 * v          |         ^
 * WAITING <-----+         |
 * ^          |         |
 * |          +--- BROADCASTING
 * |          |         ^
 * |          |         |
 * +---> PROCESSING ----+
 *
 * @author Remi Druilhe
 */
public enum ComponentStatus {
    UNINSTANTIATED("UNINSTANTIATED"), WAITING("WAITING"), PROCESSING("PROCRESSING"), BROADCASTING("BROADCASTING"), ABORTED("ABORTED");
    private String value;

    ComponentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
