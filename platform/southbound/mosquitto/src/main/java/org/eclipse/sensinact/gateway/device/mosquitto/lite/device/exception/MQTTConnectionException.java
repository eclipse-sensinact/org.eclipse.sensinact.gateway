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
package org.eclipse.sensinact.gateway.device.mosquitto.lite.device.exception;

/**
 * Exception raised when the link between the platform and the broker are lost
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class MQTTConnectionException extends Exception{

    public MQTTConnectionException(String message) {
        super(message);
    }

    public MQTTConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public MQTTConnectionException(Throwable cause) {
        super(cause);
    }

}
