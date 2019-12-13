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
package org.eclipse.sensinact.gateway.app.manager.test;

import org.eclipse.sensinact.gateway.api.core.DataResource;
import org.eclipse.sensinact.gateway.api.core.Metadata;
import org.eclipse.sensinact.gateway.api.message.UpdateMessageImpl;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.json.JSONObject;

class AppTestSnaMessage extends UpdateMessageImpl {
    /**
     * Constructor of the AppSnaMessage
     *
     * @param uri   the URI of the service
     * @param type  the type of the value
     * @param value the object value
     * @see UpdateMessageImpl
     */
    AppTestSnaMessage(Mediator mediator, String uri, Class type, Object value) {
        super(mediator, uri, Update.ATTRIBUTE_VALUE_UPDATED);
        JSONObject json = new JSONObject().put(Metadata.TIMESTAMP, System.currentTimeMillis()).put(DataResource.VALUE, value).put(DataResource.TYPE, type.getCanonicalName()).put(DataResource.NAME, uri.split("/")[2]);
        super.setNotification(json);
    }
}
