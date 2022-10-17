/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.common.execution;

/**
 * Executor Service
 *
 * @param <P> the executor parameter type
 * @param <V> the executor returned type
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface Executable<P, V> {
    /**
     * Processes this Executor by parameterizing the
     * execution using the <code>&lt;P&gt;</code> typed
     * parameter and returns the invocation result
     * <code>&lt;V&gt;</code> typed object
     *
     * @param parameter the <code>&lt;P&gt;</code> typed parameter
     * @return <code>&lt;V&gt;</code> typed result object
     * of the execution
     * @throws Exception if an error is occurred during the invocation
     */
    V execute(P parameter) throws Exception;
}
