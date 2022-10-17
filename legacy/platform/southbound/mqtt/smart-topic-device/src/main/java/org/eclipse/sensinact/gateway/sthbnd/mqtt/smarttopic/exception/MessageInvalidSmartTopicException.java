/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.exception;

public class MessageInvalidSmartTopicException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MessageInvalidSmartTopicException() {
        super();
    }

    public MessageInvalidSmartTopicException(String message) {
        super(message);
    }

    public MessageInvalidSmartTopicException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageInvalidSmartTopicException(Throwable cause) {
        super(cause);
    }

    protected MessageInvalidSmartTopicException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
