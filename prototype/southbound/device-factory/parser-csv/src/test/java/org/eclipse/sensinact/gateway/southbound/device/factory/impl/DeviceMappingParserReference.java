/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.gateway.southbound.device.factory.impl;

import java.util.Dictionary;

import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingParser;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

public class DeviceMappingParserReference implements ServiceReference<IDeviceMappingParser> {

    @Override
    public Object getProperty(String key) {
        return null;
    }

    @Override
    public String[] getPropertyKeys() {
        return null;
    }

    @Override
    public Bundle getBundle() {
        return null;
    }

    @Override
    public Bundle[] getUsingBundles() {
        return null;
    }

    @Override
    public boolean isAssignableTo(Bundle bundle, String className) {
        return false;
    }

    @Override
    public int compareTo(Object reference) {
        return 0;
    }

    @Override
    public Dictionary<String, Object> getProperties() {
        return null;
    }
}
