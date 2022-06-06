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
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

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
        JsonObject json = JsonProviderFactory.getProvider()
				.createObjectBuilder()
				.add(Metadata.TIMESTAMP, System.currentTimeMillis())
				.add(DataResource.VALUE, CastUtils.cast(JsonValue.class, value))
				.add(DataResource.TYPE, type.getCanonicalName())
				.add(DataResource.NAME, uri.split("/")[2])
				.build();
        super.setNotification(json);
    }
}
