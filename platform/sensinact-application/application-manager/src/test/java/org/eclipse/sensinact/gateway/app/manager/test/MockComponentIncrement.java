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
        super.update((Integer) CastUtils.cast(mediator.getClassLoader(), variables.get(0).getType(), variables.get(0).getValue()) + 1);
    }
}
