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
 * This exception is used when a resource can not be read
 *
 * @author Rémi Druilhe
 */
public class NotAReadableResourceException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NotAReadableResourceException() {
        super();
    }

    public NotAReadableResourceException(String message) {
        super(message);
    }
}
