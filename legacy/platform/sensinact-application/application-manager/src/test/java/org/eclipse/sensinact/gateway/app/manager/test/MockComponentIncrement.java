/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.manager.test;

import org.eclipse.sensinact.gateway.app.api.function.AbstractFunction;
import org.eclipse.sensinact.gateway.app.api.function.DataItf;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.eclipse.sensinact.gateway.util.CastUtils;

import java.util.List;

class MockComponentIncrement extends AbstractFunction<Integer> {
    private final AppServiceMediator mediator;

    MockComponentIncrement(AppServiceMediator mediator) {
        this.mediator = mediator;
    }

    public void process(List<DataItf> variables) {
        super.update((Integer) CastUtils.cast(variables.get(0).getType(), variables.get(0).getValue()) + 1);
    }
}
