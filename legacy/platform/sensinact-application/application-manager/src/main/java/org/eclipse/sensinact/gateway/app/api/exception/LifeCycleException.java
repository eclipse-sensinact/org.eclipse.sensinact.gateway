/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.api.exception;

/**
 * This exception is used when the desired state is not reached
 *
 * @author Rémi Druilhe
 */
public class LifeCycleException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LifeCycleException() {
        super();
    }

    public LifeCycleException(String message) {
        super(message);
    }
}