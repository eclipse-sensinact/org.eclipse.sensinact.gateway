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
package org.eclipse.sensinact.gateway.core.message;

import org.eclipse.sensinact.gateway.common.execution.ErrorHandler;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;

/**
 * {@link SnaMessage} callback service
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface MidCallback extends Nameable {
    static final long ENDLESS = -1;

    enum Type {
        UNARY("unary"), BUFFERIZED("buffer"), SCHEDULED("scheduler"), BUFFERERIZED_AND_SCHEDULED("scheduled-buffer");

        private final String operator;

        Type(String operator) {
            this.operator = operator;
        }

        public static Type fromOperator(String operator) {
            Type values[] = Type.values();
            int index = 0;
            int length = values.length;
            for (; index < length; index++) {
                if (values[index].getOperator().equals(operator)) {
                    return values[index];
                }
            }
            return null;
        }

        private String getOperator() {
            return this.operator;
        }
    }

    /**
     * Returns this SnaCallback's error handler. The
     * {@link ErrorHandler} is in charge of
     * defining the error treatment policy for the
     * {@link SnaMessageListener} holding it
     *
     * @return this SnaCallback's error handler
     */
    ErrorHandler getCallbackErrorHandler();

    /**
     * Returns the {@link AccessMethodResponse.Status} of the last
     * callback
     *
     * @return the last callback {@link AccessMethodResponse.Status}
     */
    AccessMethodResponse.Status getStatus();

    /**
     * Returns this callback's timeout
     *
     * @return this callback's timeout
     */
    long getTimeout();

    /**
     * Returns the {@link MessageReisterer} associated to
     * this MidCallback
     *
     * @return the {@link MessageReisterer} of this SnaCallback
     */
    MessageRegisterer getMessageRegisterer();

    /**
     * Defines the String identifier of this MidCallback. if
     * the String identifier has already been defined, using
     * this method will produce no effect
     *
     * @param identifier the String identifier of this MidCallback.
     */
    void setIdentifier(String identifier);
}
