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
package org.eclipse.sensinact.gateway.app.manager.application.dependency;

import org.eclipse.sensinact.gateway.core.message.AbstractMidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.SnaErrorMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaResponseMessage;
import org.eclipse.sensinact.gateway.core.message.SnaUpdateMessageImpl;

/**
 * Class that avoid the DependencyManager to implement empty methods that may create some noisy to understand its real role
 */
public abstract class DependencyManagerAbstract extends AbstractMidAgentCallback {
    private String id;

    public DependencyManagerAbstract(String id) {
        super();
        this.id = id;
    }

    @Override
    public void doHandle(SnaErrorMessageImpl message) {
        //Handle not used
    }

    @Override
    public void doHandle(SnaResponseMessage<?, ?> message) {
        //Handle not used
    }

    @Override
    public void doHandle(SnaUpdateMessageImpl message) {
        //Handle not used
    }
}
