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
