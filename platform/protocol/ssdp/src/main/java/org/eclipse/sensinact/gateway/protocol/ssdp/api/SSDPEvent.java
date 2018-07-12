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
package org.eclipse.sensinact.gateway.protocol.ssdp.api;

/**
 * Summarize the SSDP events
 */
public enum SSDPEvent {
    DISCOVER("ssdp:discover"), ALIVE("ssdp:alive"), UPDATE("ssdp:update"), GOODBYE("ssdp:byebye");
    private String ssdpEvent;

    SSDPEvent(String ssdpEvent) {
        this.ssdpEvent = ssdpEvent;
    }

    public String toString() {
        return ssdpEvent;
    }
}
