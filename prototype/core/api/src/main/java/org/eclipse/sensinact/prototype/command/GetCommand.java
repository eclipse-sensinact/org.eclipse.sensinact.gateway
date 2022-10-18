/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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

import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public class GetCommand<T> extends AbstractSensinactCommand<T> {

    @Override
    public Promise<T> call(SensinactModel model, PromiseFactory pf) {
        // TODO Auto-generated method stub
        return null;
    }

}
