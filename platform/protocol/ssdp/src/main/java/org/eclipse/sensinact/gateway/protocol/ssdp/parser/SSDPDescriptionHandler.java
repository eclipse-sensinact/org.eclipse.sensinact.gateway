/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.protocol.ssdp.parser;

import org.eclipse.sensinact.gateway.protocol.ssdp.model.SSDPDescriptionPacket;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Stack;

public class SSDPDescriptionHandler extends DefaultHandler {
    private SSDPDescriptionPacket descriptionPacket;
    private Stack<String> elementStack = new Stack<String>();
    private Stack<SSDPDescriptionPacket> descriptionStack = new Stack<SSDPDescriptionPacket>();

    public SSDPDescriptionHandler() {
    }

    /**
     * @inheritDoc
     */
    public void startDocument() throws SAXException {
        descriptionStack.push(new SSDPDescriptionPacket());
    }

    /**
     * @inheritDoc
     */
    public void endDocument() throws SAXException {
        descriptionPacket = descriptionStack.pop();
    }

    /**
     * @inheritDoc
     */
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        this.elementStack.push(qName);
    }

    /**
     * @inheritDoc
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
        String value = new String(ch, start, length).trim();
        if (value.length() == 0) {
            return;
        }
        if ("friendlyName".equalsIgnoreCase(this.elementStack.peek())) {
            SSDPDescriptionPacket packet = this.descriptionStack.peek();
            packet.setFriendlyName(value.replace(" ", "_"));
        } else if ("URLBase".equalsIgnoreCase(this.elementStack.peek())) {
            SSDPDescriptionPacket packet = this.descriptionStack.peek();
            packet.setUrl(value.split(":")[1].replace("/", ""));
        } else if ("presentationURL".equalsIgnoreCase(this.elementStack.peek())) {
            if (value.startsWith("http")) {
                SSDPDescriptionPacket packet = this.descriptionStack.peek();
                if (packet.getUrl() == null) {
                    packet.setUrl(value.split(":")[1].replace("/", ""));
                }
            }
        }
    }

    /**
     * Gets the packet after XML parsing
     *
     * @return the Java representation of the packet after parsing
     */
    public SSDPDescriptionPacket getDescriptionPacket() {
        return descriptionPacket;
    }
}
