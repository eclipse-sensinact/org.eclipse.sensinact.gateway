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
