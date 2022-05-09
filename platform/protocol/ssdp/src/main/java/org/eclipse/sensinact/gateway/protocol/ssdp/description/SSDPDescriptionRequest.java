/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.protocol.ssdp.description;

import org.eclipse.sensinact.gateway.protocol.ssdp.model.SSDPDescriptionPacket;
import org.eclipse.sensinact.gateway.protocol.ssdp.model.SSDPReceivedMessage;
import org.eclipse.sensinact.gateway.protocol.ssdp.parser.SSDPDescriptionHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SSDPDescriptionRequest {
    public SSDPDescriptionRequest() {
    }

    /**
     * Retrieves the XML description of device from the URL in the {@link ResponseMessage}
     *
     * @param packet the discovery packet
     * @return the resulting {@link SSDPDescriptionPacket}
     */
    public static SSDPDescriptionPacket getDescription(SSDPReceivedMessage packet) {
        String response = null;
        try {
            URL url = new URL(packet.getLocation());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/xml");
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder buffer = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    buffer.append(inputLine);
                }
                in.close();
                response = buffer.toString();
            } else {
                return null;
            }
            connection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response == null) {
            return null;
        }
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            SSDPDescriptionHandler descriptionHandler = new SSDPDescriptionHandler();
            saxParser.parse(new InputSource(new ByteArrayInputStream(response.getBytes("utf-8"))), descriptionHandler);
            return descriptionHandler.getDescriptionPacket();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }
}