/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.app.manager.application.dependency;

import org.eclipse.sensinact.gateway.api.message.AbstractMessageAgentCallback;
import org.eclipse.sensinact.gateway.api.message.ErrorMessageImpl;
import org.eclipse.sensinact.gateway.api.message.ResponseMessage;
import org.eclipse.sensinact.gateway.api.message.UpdateMessageImpl;

/**
 * Class that avoid the DependencyManager to implement empty methods that may create some noisy to understand its real role
 */
public abstract class DependencyManagerAbstract extends AbstractMessageAgentCallback {
    private String id;

    public DependencyManagerAbstract(String id) {
        super();
        this.id = id;
    }

    @Override
    public void doHandle(ErrorMessageImpl message) {
        //Handle not used
    }

    @Override
    public void doHandle(ResponseMessage<?, ?> message) {
        //Handle not used
    }

    @Override
    public void doHandle(UpdateMessageImpl message) {
        //Handle not used
    }
}
