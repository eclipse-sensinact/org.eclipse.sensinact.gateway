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
package org.eclipse.sensinact.gateway.app.api.exception;

/**
 * This exception is used when the desired state is not reached
 *
 * @author Rémi Druilhe
 */
public class LifeCycleException extends Exception {
    public LifeCycleException() {
        super();
    }

    public LifeCycleException(String message) {
        super(message);
    }
}
