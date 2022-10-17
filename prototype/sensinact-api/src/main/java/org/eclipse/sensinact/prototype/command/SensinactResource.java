/*********************************************************************
* Copyright (c) 2022 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.prototype.command;

import java.time.Instant;
import java.util.Map;

import org.eclipse.sensinact.prototype.model.Resource;
import org.osgi.util.promise.Promise;

public interface SensinactResource extends CommandScoped, Resource {

    default Promise<Void> setValue(Object value) {
        return setValue(value, Instant.now());
    };

    Promise<Void> setValue(Object value, Instant timestamp);

    Promise<Object> getValue();

    Promise<Void> setMetadataValue(String name, Object value, Instant timestamp);

    Promise<Object> getMetadataValue(String name);

    Promise<Map<String, Object>> getMetadataValues();

    @Override
    SensinactService getService();

}
