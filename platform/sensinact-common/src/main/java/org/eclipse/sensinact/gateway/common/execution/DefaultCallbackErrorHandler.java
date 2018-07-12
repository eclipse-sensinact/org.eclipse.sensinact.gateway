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
package org.eclipse.sensinact.gateway.common.execution;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Default {@link ErrorHandler} implementation
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class DefaultCallbackErrorHandler implements ErrorHandler {
    private final static byte DEFAULT_ERROR_POLICY = ErrorHandler.ErrorHandlerPolicy.valueOf(new ErrorHandler.ErrorHandlerPolicy[]{ErrorHandler.ErrorHandlerPolicy.REMOVE});

    private JSONArray errors;
    private int exceptions = 0;

    /**
     * @InheritedDoc
     * @see ErrorHandler#register(java.lang.Exception)
     */
    @Override
    public void register(Exception exception) {
        if (exception == null) {
            return;
        }
        this.exceptions++;
        JSONObject exceptionObject = new JSONObject();
        exceptionObject.put("message", exception.getMessage());

        StringBuilder buffer = new StringBuilder();
        if (exception != null) {
            StackTraceElement[] trace = exception.getStackTrace();

            int index = 0;
            int length = trace.length;

            for (; index < length; index++) {
                buffer.append(trace[index].toString());
                buffer.append("\n");
            }
        }
        exceptionObject.put("trace", buffer.toString());

        if (this.errors == null) {
            this.errors = new JSONArray();
        }
        errors.put(exceptionObject);
    }

    /**
     * @InheritedDoc
     * @see ErrorHandler#hasError()
     */
    @Override
    public boolean hasError() {
        return this.exceptions > 0;
    }

    /**
     * @InheritedDoc
     * @see ErrorHandler#getPolicy()
     */
    @Override
    public byte getPolicy() {
        return DefaultCallbackErrorHandler.DEFAULT_ERROR_POLICY;

    }

    /**
     * @InheritedDoc
     * @see ErrorHandler#getStackTrace()
     */
    @Override
    public JSONArray getStackTrace() {
        return this.errors;
    }

    /**
     * @InheritedDoc
     * @see ErrorHandler#getAlternative()
     */
    @Override
    public <E extends Executable<?, ?>> List<E> getAlternative() {
        return null;
    }
}
