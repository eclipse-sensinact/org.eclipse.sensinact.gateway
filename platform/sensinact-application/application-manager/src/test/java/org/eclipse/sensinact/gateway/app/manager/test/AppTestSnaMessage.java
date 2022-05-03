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

import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.Metadata;
import org.eclipse.sensinact.gateway.core.message.SnaUpdateMessageImpl;
import org.json.JSONObject;

class AppTestSnaMessage extends SnaUpdateMessageImpl {
    /**
     * Constructor of the AppSnaMessage
     *
     * @param uri   the URI of the service
     * @param type  the type of the value
     * @param value the object value
     * @see SnaUpdateMessageImpl
     */
    AppTestSnaMessage(String uri, Class<?> type, Object value) {
        super(uri, Update.ATTRIBUTE_VALUE_UPDATED);
        JSONObject json = new JSONObject().put(Metadata.TIMESTAMP, System.currentTimeMillis()).put(DataResource.VALUE, value).put(DataResource.TYPE, type.getCanonicalName()).put(DataResource.NAME, uri.split("/")[2]);
        super.setNotification(json);
    }
}
