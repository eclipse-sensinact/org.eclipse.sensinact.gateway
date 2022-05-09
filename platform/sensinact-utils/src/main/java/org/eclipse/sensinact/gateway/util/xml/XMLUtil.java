/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.util.xml;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.net.URL;

/**
 * Helper for xml parsing
 */
public class XMLUtil {
    /**
     * Parses the XML file which path is passed as parameter
     *
     * @param handler
     * @param schema
     * @param xml
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static void parse(ContentHandler handler, URL schema, String xml) throws ParserConfigurationException, SAXException, IOException {
        parse(handler, schema, new InputSource(xml));
    }

    /**
     * Parses the XML file which path is passed as parameter
     *
     * @param handler
     * @param schema
     * @param xml
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static void parse(ContentHandler handler, URL schema, InputSource xml) throws ParserConfigurationException, SAXException, IOException {
        if (handler == null) {
            throw new SAXException("Null ContentHandler");
        }
        SAXParserFactory factory = SAXParserFactory.newInstance();
        if (schema != null) {
            factory.setNamespaceAware(true);
            factory.setValidating(true);
        }
        SAXParser parser = factory.newSAXParser();
        if (schema != null) {
            parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
            parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", schema.toExternalForm());
        }
        XMLReader reader = parser.getXMLReader();
        reader.setContentHandler(handler);
        reader.parse(xml);
    }

    /**
     * Creates and returns an XML string
     * formated element
     *
     * @param prefix     the namespace prefix
     * @param tag        the tag name
     * @param attributes the tag's attributes array
     * @param content    the text content of the tag to create
     * @return the XML string formated tag
     */
    public static String createTag(String prefix, String tag, String[][] attributes, Object content) {
        StringBuilder tagBuilder = new StringBuilder();
        tagBuilder.append(openTag(prefix, tag, attributes));
        tagBuilder.append(content);
        tagBuilder.append(closeTag(prefix, tag));
        return tagBuilder.toString();
    }

    /**
     * Returns the XML string formated tag opening
     *
     * @param prefix     the namespace prefix
     * @param tag        the tag name
     * @param attributes the tag's attributes array
     * @return Returns the XML string formated tag opening
     */
    public static String openTag(String prefix, String tag, String[][] attributes) {
        StringBuilder tagBuilder = new StringBuilder();
        tagBuilder.append("<");
        tagBuilder.append((prefix != null && prefix.length() > 0) ? prefix : "");
        tagBuilder.append((prefix != null && prefix.length() > 0) ? ":" : "");
        tagBuilder.append(tag);
        if (attributes != null) {
            for (int index = 0; index < attributes.length; index++) {
                tagBuilder.append(" ");
                tagBuilder.append(attributes[index][0]);
                tagBuilder.append("=\"");
                tagBuilder.append(attributes[index][1]);
                tagBuilder.append("\"");
            }
        }
        tagBuilder.append(">");
        return tagBuilder.toString();
    }

    /**
     * Returns the XML string formated tag closure
     *
     * @param prefix the namespace prefix
     * @param tag    the tag name
     * @return Returns the XML string formated tag closure
     */
    public static String closeTag(String prefix, String tag) {
        StringBuilder tagBuilder = new StringBuilder();
        tagBuilder.append("</");
        tagBuilder.append((prefix != null && prefix.length() > 0) ? prefix : "");
        tagBuilder.append((prefix != null && prefix.length() > 0) ? ":" : "");
        tagBuilder.append(tag);
        tagBuilder.append(">");
        return tagBuilder.toString();
    }
}
