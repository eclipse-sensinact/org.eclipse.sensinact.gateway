/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.prototype.command;

import java.time.Instant;
import java.util.List;

public interface SensinactModel extends CommandScoped {

    SensinactResource getOrCreateResource(String model, String provider, String service, String resource,
            Class<?> valueType);

    List<SensinactProvider> getProviders();

    SensinactProvider getProvider(String providerName);

    SensinactProvider getProvider(String model, String providerName);

    SensinactService getService(String model, String providerName, String service);

    SensinactService getService(String providerName, String service);

    SensinactResource getResource(String model, String providerName, String service, String resource);

    SensinactResource getResource(String providerName, String service, String resource);

    <T> TimedValue<T> getResourceValue(String model, String providerName, String service, String resource,
            Class<T> type);

    <T> TimedValue<T> getResourceValue(String providerName, String service, String resource, Class<T> type);

    /**
     * Sets the value of resource. Creates it if necessary
     *
     * @param model     Model name
     * @param provider  Provider name
     * @param service   Service name
     * @param resource  Resource name
     * @param resource2
     * @param type      Resource content type
     * @param value     Resource value
     * @param instant   Update time stamp
     */
    void setOrCreateResource(String model, String provider, String service, String resource, Class<?> type,
            Object value, Instant instant);

    void setOrCreateResource(String provider, String service, String resource, Class<?> type, Object value,
            Instant instant);

}
