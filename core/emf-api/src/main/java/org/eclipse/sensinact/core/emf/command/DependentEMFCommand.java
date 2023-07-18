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

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.DependentCommand;
import org.eclipse.sensinact.core.emf.model.SensinactEMFModelManager;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFDigitalTwin;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * A {@link DependentEMFCommand} provides a simple way to write a command which
 * depends upon the result of a previous parent command. This command will
 * correctly delay execution of the dependent command until the parent command
 * has completed. It will do this without blocking the Gateway Thread.
 *
 * Notifications will not be delivered until the child command has completed.
 *
 * @param <P>
 * @param <T>
 */
public abstract class DependentEMFCommand<P, T> extends DependentCommand<P, T> {

    public DependentEMFCommand(AbstractSensinactCommand<P> parent) {
        super(parent);
    }

    protected Promise<T> call(Promise<P> parentResult, SensinactDigitalTwin twin, SensinactModelManager modelMgr,
            PromiseFactory pf) {
        return call(parentResult, (SensinactEMFDigitalTwin) twin, (SensinactEMFModelManager) modelMgr, pf);
    }

    protected abstract Promise<T> call(Promise<P> parentResult, SensinactEMFDigitalTwin twin,
            SensinactEMFModelManager modelMgr, PromiseFactory pf);

}
