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
package org.sensinact.mqtt.server;

public class MQTTException extends Exception {
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
