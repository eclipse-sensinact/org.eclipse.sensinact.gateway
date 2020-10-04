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
package org.eclipse.sensinact.gateway.app.api.exception;

/**
 * This exception is used when the desired function can not be found in the plugins
 *
 * @author Rémi Druilhe
 */
public class FunctionNotFoundException extends Exception {
    public FunctionNotFoundException() {
        super();
    }

    public FunctionNotFoundException(String message) {
        super(message);
    }
}
