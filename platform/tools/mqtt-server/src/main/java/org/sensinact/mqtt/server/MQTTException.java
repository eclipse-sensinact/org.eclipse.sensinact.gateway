/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.sensinact.mqtt.server;

public class MQTTException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MQTTException() {
        super();
    }

    public MQTTException(String message) {
        super(message);
    }

    public MQTTException(String message, Throwable cause) {
        super(message, cause);
    }

    public MQTTException(Throwable cause) {
        super(cause);
    }

    protected MQTTException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
