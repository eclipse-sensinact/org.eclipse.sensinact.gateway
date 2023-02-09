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
package org.eclipse.sensinact.prototype.command;

import org.eclipse.sensinact.prototype.model.SensinactModelManager;
import org.eclipse.sensinact.prototype.twin.SensinactDigitalTwin;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * A twin command has no requirement to access or change the model and therefore
 * does not receive a {@link SensinactModelManager}
 *
 * @param <T>
 */
public abstract class AbstractTwinCommand<T> extends AbstractSensinactCommand<T> {

    @Override
    public final Promise<T> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr, PromiseFactory pf) {
        return call(twin, pf);
    }

    protected abstract Promise<T> call(SensinactDigitalTwin twin, PromiseFactory pf);

}
