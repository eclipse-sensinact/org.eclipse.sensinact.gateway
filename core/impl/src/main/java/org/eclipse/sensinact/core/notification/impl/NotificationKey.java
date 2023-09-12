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
package org.eclipse.sensinact.core.notification.impl;

import org.eclipse.sensinact.core.notification.AbstractResourceNotification;
import org.eclipse.sensinact.core.notification.LifecycleNotification;
import org.eclipse.sensinact.core.notification.ResourceActionNotification;
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.notification.ResourceMetaDataNotification;

class NotificationKey implements Comparable<NotificationKey> {

    private final String provider;
    private final String service;
    private final String resource;

    private final Class<? extends AbstractResourceNotification> type;

    public NotificationKey(String provider, String service, String resource,
            Class<? extends AbstractResourceNotification> type) {
        this.provider = provider;
        this.service = service;
        this.resource = resource;
        this.type = type;
    }

    @Override
    public int compareTo(NotificationKey nk) {
        int value = compareType(type, nk.type);

        if (value == 0) {
            value = safeCompare(provider, nk.provider);
        }

        if (value == 0) {
            value = safeCompare(service, nk.service);
        }

        if (value == 0) {
            value = safeCompare(resource, nk.resource);
        }

        return value;
    }

    private int compareType(Class<? extends AbstractResourceNotification> a,
            Class<? extends AbstractResourceNotification> b) {
        return mapTypeToInt(a) - mapTypeToInt(b);
    }

    private int mapTypeToInt(Class<? extends AbstractResourceNotification> clazz) {
        if (LifecycleNotification.class == clazz) {
            return 1;
        }
        if (ResourceMetaDataNotification.class == clazz) {
            return 2;
        }
        if (ResourceDataNotification.class == clazz) {
            return 3;
        }
        if (ResourceActionNotification.class == clazz) {
            return 4;
        }
        throw new IllegalArgumentException("Unkown type " + clazz.getName());
    }

    private int safeCompare(String a, String b) {
        if (a == null) {
            return b == null ? 0 : -1;
        }

        return b == null ? 1 : a.compareTo(b);
    }
}
