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
package org.eclipse.sensinact.gateway.app.api.function;

import org.eclipse.sensinact.gateway.app.api.exception.LifeCycleException;

import java.util.List;

public abstract class AbstractFunction<T> {
    private FunctionUpdateListener updateListener;

    /**
     * This method is called when the application is about to be started.
     * This method should be override when the function needs to perform some operations before any processing.
     *
     * @throws LifeCycleException when a problem occurs in the instantiation of the function
     */
    public void instantiate() throws LifeCycleException {
    }

    /**
     * This method is called when the application is about to be stopped.
     * This method should be override when the function needs to be perform some operations before its stop.
     *
     * @throws LifeCycleException when a problem occurs in the uninstantiation of the function
     */
    public void uninstantiate() throws LifeCycleException {
    }

    /**
     * Register a {@link FunctionUpdateListener} associated to the function
     *
     * @param listener the {@link FunctionUpdateListener}
     */
    public void setListener(FunctionUpdateListener listener) {
        //TODO: should we test also that updateListener is null ?
        if (listener != null) {
            this.updateListener = listener;
        }
    }

    /**
     * Remove the {@link FunctionUpdateListener} associated to the function
     *
     * @param listener the {@link FunctionUpdateListener}
     */
    public void removeListener(FunctionUpdateListener listener) {
        if (this.updateListener == listener) {
            this.updateListener = null;
        }
    }

    /**
     * Trigger the processing of the function.
     *
     * @param dataList the parameters of the function
     * @throws Exception
     */
    public abstract void process(List<DataItf> dataList) throws Exception;

    /**
     * Notify the listener about a new result from the function.
     *
     * @param result the result of the function
     */
    protected void update(T result) {
        updateListener.updatedResult(result);
    }

    /**
     * Specify whether the function can produce results even when no events has triggered
     * the processing of the function.
     * Default is false.
     */
    public boolean isAsynchronous() {
        return false;
    }
}
