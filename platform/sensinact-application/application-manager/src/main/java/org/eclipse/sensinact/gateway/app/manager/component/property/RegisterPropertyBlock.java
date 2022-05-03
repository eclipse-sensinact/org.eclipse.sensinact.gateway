/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.manager.component.property;

import org.eclipse.sensinact.gateway.app.manager.component.AbstractDataProvider;
import org.eclipse.sensinact.gateway.app.manager.component.DataListenerItf;
import org.eclipse.sensinact.gateway.app.manager.component.Event;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.core.Attribute;
import org.eclipse.sensinact.gateway.core.DataResource;
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
        	Attribute attr = resource.getAttribute(DataResource.VALUE);
        	if(attr!=null)
            	attr.setValue(event.getData().getValue());
        } catch (InvalidValueException e) {
            e.printStackTrace();
        }
    }
}
