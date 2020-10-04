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
package org.eclipse.sensinact.gateway.util.xml;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract {@link ContentHandler} implementation for xml parsing
 */
public abstract class AbstractContentHandler<T> implements ContentHandler {
    protected static final Logger LOGGER = Logger.getLogger(AbstractContentHandler.class.getCanonicalName());

    /**
     * Delegate of the end element event method
     *
     * @param tag   the ending tag
     * @param qname the tag's QName
     * @return a <T> type object build using the ending tag
     * @throws SAXException
     */
    public abstract T end(String tag, String qname) throws SAXException;

    /**
     * Delegate of the start element event method
     *
     * @param tag   the starting tag
     * @param qname the tag's qname
     * @param atts  {@link Attributes} object the starting xml element
     * @return a <T> type object build using the starting tag
     * @throws SAXException
     */
    public abstract T start(String tag, String qname, Attributes atts) throws SAXException;

    protected StringBuilder textContent;
    protected Stack<T> stack = null;

    /**
     * Constructor
     *
     * @param mediator
     */
    public AbstractContentHandler() {
        this.textContent = new StringBuilder();
    }

    /**
     * @inheritDoc
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] chrs, int start, int length) throws SAXException {
        this.textContent.append(chrs, start, length);
    }

    /**
     * @inheritDoc
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] chrs, int start, int length) throws SAXException {
        textContent.append(chrs, start, length);
    }

    /**
     * @inheritDoc
     * @see org.xml.sax.ContentHandler#
     * startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String qname, Attributes atts) throws SAXException {
        T object = null;
        try {
            object = start(localName, qname, atts);

        } catch (Exception e) {
            e.printStackTrace();
            throw new SAXException(e);
        }
        if (object != null) {
            stack.push(object);
        }
        this.textContent = null;
        this.textContent = new StringBuilder();
    }

    /**
     * @inheritDoc
     * @see org.xml.sax.ContentHandler#
     * endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String qname) throws SAXException {
        T object = null;
        try {
            object = end(localName, qname);

        } catch (Exception e) {
            e.printStackTrace();
            throw new SAXException(e);
        }
        if (object != null) {
            stack.push(object);
        }
    }

    /**
     * @inheritDoc
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    public void setDocumentLocator(Locator locator) {
        LOGGER.log(Level.CONFIG, "Locator defined");
    }

    /**
     * @inheritDoc
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
     */
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        LOGGER.log(Level.CONFIG, "start prefix mapping : " + prefix);
    }

    /**
     * @inheritDoc
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    public void endPrefixMapping(String prefix) throws SAXException {
        LOGGER.log(Level.CONFIG, "end prefix mapping : " + prefix);
    }

    /**
     * @inheritDoc
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        LOGGER.log(Level.CONFIG, "start document parsing");
        this.stack = new Stack<T>();
    }

    /**
     * @inheritDoc
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        LOGGER.log(Level.CONFIG, "end document parsing");
    }

    /**
     * @inheritDoc
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
     */
    public void processingInstruction(String target, String data) throws SAXException {
        LOGGER.log(Level.CONFIG, "processing instruction : \n\t" + target + " \n\t " + data);
    }

    /**
     * @inheritDoc
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    public void skippedEntity(String name) throws SAXException {
        LOGGER.log(Level.CONFIG, "skipped entity : " + name);
    }
}
