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
package org.eclipse.sensinact.gateway.app.manager.component.property;

import org.eclipse.sensinact.gateway.api.core.DataResource;
import org.eclipse.sensinact.gateway.app.manager.component.AbstractDataProvider;
import org.eclipse.sensinact.gateway.app.manager.component.DataListenerItf;
import org.eclipse.sensinact.gateway.app.manager.component.Event;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.core.ResourceImpl;

public class RegisterPropertyBlock extends AbstractPropertyBlock implements DataListenerItf {
    public static final String PROPERTY = "register";
    private final ResourceImpl resource;
    private final AbstractDataProvider dataProvider;

    public RegisterPropertyBlock(ResourceImpl resource, AbstractDataProvider dataProvider) {
        this.resource = resource;
        this.dataProvider = dataProvider;
    }

    /**
     * @see AbstractPropertyBlock#instantiate()
     */
    public void instantiate() {
        dataProvider.addListener(this, null);
    }

    /**
     * @see AbstractPropertyBlock#uninstantiate()
     */
    public void uninstantiate() {
        dataProvider.removeListener(this);
    }

    /**
     * @see DataListenerItf#eventNotification(Event)
     */
    public void eventNotification(Event event) {
        try {
            resource.getAttribute(DataResource.VALUE).setValue(event.getData().getValue());
        } catch (InvalidValueException e) {
            e.printStackTrace();
        }
    }
}
