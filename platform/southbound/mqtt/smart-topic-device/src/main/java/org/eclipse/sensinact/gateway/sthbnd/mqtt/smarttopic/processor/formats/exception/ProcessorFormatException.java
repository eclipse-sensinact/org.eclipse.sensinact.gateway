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
package org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.formats.exception;

/**
 * This exception is raised whenever the format send to the processor to not correspont to the accepted respective processor syntax
 *
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class ProcessorFormatException extends org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.exception.ProcessorException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ProcessorFormatException(String message) {
        super(message);
    }

    public ProcessorFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProcessorFormatException(Throwable cause) {
        super(cause);
    }

    protected ProcessorFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
