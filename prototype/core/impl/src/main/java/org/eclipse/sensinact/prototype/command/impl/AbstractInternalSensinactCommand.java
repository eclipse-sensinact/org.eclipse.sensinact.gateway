/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.prototype.command.impl;

import org.eclipse.sensinact.prototype.command.AbstractSensinactCommand;
import org.eclipse.sensinact.prototype.command.SensinactModel;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * This command is used internally in the core of the gateway to provide access
 * to internal methods of the model
 */
public abstract class AbstractInternalSensinactCommand<T> extends AbstractSensinactCommand<T> {

    @Override
    protected final Promise<T> call(SensinactModel model, PromiseFactory promiseFactory) {
        return call((SensinactModelImpl) model, promiseFactory);
    }

    protected abstract Promise<T> call(SensinactModelImpl modelImpl, PromiseFactory promiseFactory);
}
