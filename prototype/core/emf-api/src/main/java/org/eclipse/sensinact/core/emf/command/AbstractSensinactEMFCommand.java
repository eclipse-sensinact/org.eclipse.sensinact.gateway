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
package org.eclipse.sensinact.core.emf.command;

import java.util.function.Supplier;

import org.eclipse.sensinact.core.emf.model.SensinactEMFModelManager;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFDigitalTwin;
import org.eclipse.sensinact.prototype.command.AbstractSensinactCommand;
import org.eclipse.sensinact.prototype.model.SensinactModelManager;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;
import org.eclipse.sensinact.prototype.twin.SensinactDigitalTwin;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public abstract class AbstractSensinactEMFCommand<T> extends AbstractSensinactCommand<T> {

    protected AbstractSensinactEMFCommand() {
        super();
    }

    protected AbstractSensinactEMFCommand(NotificationAccumulator accumulator) {
        super(accumulator);
    }

    protected abstract Promise<T> call(SensinactEMFDigitalTwin twin, SensinactEMFModelManager modelMgr,
            PromiseFactory promiseFactory);

    protected Promise<T> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr, PromiseFactory pf) {
        return call((SensinactEMFDigitalTwin) twin, (SensinactEMFModelManager) modelMgr, pf);
    }

    protected static <R> Promise<R> safeCall(Supplier<Promise<R>> supplier, PromiseFactory pf) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return pf.failed(e);
        }
    }
}
