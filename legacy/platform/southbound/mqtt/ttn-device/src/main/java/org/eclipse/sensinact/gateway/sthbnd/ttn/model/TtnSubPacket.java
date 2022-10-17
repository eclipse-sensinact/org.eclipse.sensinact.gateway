/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.ttn.model;

public class TtnSubPacket<T> {

    private final String service;
    private final String resource;
    private final String attribute;
    private final String metadata;
    private final T value;

    public TtnSubPacket(String service, String resource, String attribute, String metadata, T value) {
        this.service = service;
        this.resource = resource;
        this.attribute = attribute;
        this.metadata = metadata;
        this.value = value;
    }

    public String getService() {
        return service;
    }

    public String getResource() {
        return resource;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getMetadata() {
        return metadata;
    }

    public T getValue() {
        return value;
    }
}
