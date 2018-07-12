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
