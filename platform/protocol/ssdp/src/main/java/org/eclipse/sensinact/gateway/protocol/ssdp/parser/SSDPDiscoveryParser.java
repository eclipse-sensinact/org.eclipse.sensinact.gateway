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
package org.eclipse.sensinact.gateway.protocol.ssdp.parser;

import org.eclipse.sensinact.gateway.protocol.ssdp.model.NotifyMessage;
import org.eclipse.sensinact.gateway.protocol.ssdp.model.ResponseMessage;
import org.eclipse.sensinact.gateway.protocol.ssdp.model.SSDPMessage;

/**
 * Class to parse the SSDP messages received.
 * According to the type of message, the field are not the same.
 */
public class SSDPDiscoveryParser {
    public static SSDPMessage parse(String message) {
        String[] content = message.split("\\r\\n");
        if (content[0].equals(SSDPMessage.RequestLine.RESPONSE.getRequestLine())) {
            ResponseMessage responseMessage = new ResponseMessage();
            for (int i = 1; i < content.length; i++) {
                String[] splittedLine = content[i].split(":", 2);
                if (splittedLine.length > 1) {
                    String header = splittedLine[0];
                    if (splittedLine[1] != null) {
                        String value = splittedLine[1];
                        if (header.equalsIgnoreCase("LOCATION")) {
                            responseMessage.setLocation(value.replace(" ", ""));
                        } else if (header.equalsIgnoreCase("USN")) {
                            responseMessage.setUsn(value.replace(" ", ""));
                        } else if (header.equalsIgnoreCase("CACHE-CONTROL")) {
                            if (value.startsWith("max-age")) {
                                responseMessage.setMaxAge(new Integer(value.split("=")[1]));
                            }
                        } else if (header.equalsIgnoreCase("EXT")) {
                            responseMessage.setExt(value);
                        } else if (header.equalsIgnoreCase("ST")) {
                            responseMessage.setSt(value);
                        } else if (header.equalsIgnoreCase("SERVER")) {
                            responseMessage.setServer(value);
                        }
                    }
                }
            }
            return responseMessage;
        } else if (content[0].equals(SSDPMessage.RequestLine.NOTIFY.getRequestLine())) {
            NotifyMessage notifyMessage = new NotifyMessage();
            for (int i = 1; i < content.length; i++) {
                String[] splittedLine = content[i].split(":", 2);
                if (splittedLine.length > 1) {
                    String header = splittedLine[0];
                    if (splittedLine[1] != null) {
                        String value = splittedLine[1];
                        if (header.equalsIgnoreCase("LOCATION")) {
                            notifyMessage.setLocation(value.replace(" ", ""));
                        } else if (header.equalsIgnoreCase("USN")) {
                            notifyMessage.setUsn(value.replace(" ", ""));
                        } else if (header.equalsIgnoreCase("CACHE-CONTROL")) {
                            if (value.startsWith("max-age")) {
                                notifyMessage.setMaxAge(new Integer(value.split("=")[1]));
                            }
                        } else if (header.equalsIgnoreCase("NTS")) {
                            notifyMessage.setEvent(value);
                        } else if (header.equalsIgnoreCase("NT")) {
                            notifyMessage.setNotificationType(value);
                        } else if (header.equalsIgnoreCase("SERVER")) {
                            notifyMessage.setServer(value);
                        }
                    }
                }
            }
            return notifyMessage;
        } else if (content[0].equals(SSDPMessage.RequestLine.MSEARCH.getRequestLine())) {
            //Do nothing
        }
        return null;
    }
}
