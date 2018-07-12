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
package org.eclipse.sensinact.gateway.common.automata;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.util.xml.AbstractContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link AbstractContentHandler} implementation to build a FrameModel implementation
 * instance by parsing an xml model
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class FrameModelHandlerImpl extends AbstractContentHandler<FrameType> implements FrameModelHandler {
    private static final Map<String, Method> methods = new HashMap<String, Method>();

    static {
        String[] endMethodsNames = new String[]{"nameEnd", "lengthEnd", "implementationEnd", "typesEnd", "frameEnd", "framesEnd"};
        String[] startMethodsNames = new String[]{"typeStart", "framesStart", "frameStart"};
        Method method = null;
        try {
            for (String methodName : startMethodsNames) {
                method = FrameModelHandlerImpl.class.getDeclaredMethod(methodName, new Class[]{Attributes.class});
                methods.put(methodName, method);
            }
            for (String methodName : endMethodsNames) {
                method = FrameModelHandlerImpl.class.getDeclaredMethod(methodName);
                methods.put(methodName, method);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, FrameType> typesMap;
    private FrameModel frameModel;

    private int delimitation = -1;
    private Byte endDelimiter = null;
    private Byte startDelimiter = null;
    private Byte escape = null;
    private Mediator mediator;

    /**
     * Constructor
     *
     * @param mediator the {@link ServiceMediator} to use
     */
    public FrameModelHandlerImpl(Mediator mediator) {
        super();
        this.mediator = mediator;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.util.frame.AbstractContentHandler#
     * end(java.lang.String)
     */
    @Override
    public FrameType end(String tag, String qname) {
        Method endMethod = methods.get(tag + "End");
        if (endMethod != null) {
            try {
                return (FrameType) endMethod.invoke(this);

            } catch (Exception e) {
                this.mediator.error(e, e.getMessage());
            }
        }
        return null;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.util.frame.AbstractContentHandler#
     * start(java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public FrameType start(String tag, String qname, Attributes atts) throws SAXException {
        Method startMethod = methods.get(tag + "Start");
        if (startMethod != null) {
            try {
                return (FrameType) startMethod.invoke(this, new Object[]{atts});

            } catch (Exception e) {
                throw new SAXException(e);
            }
        }
        return null;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.util.frame.FrameModelHandlerItf#getModel()
     */
    public FrameModel getModel() {
        return this.frameModel;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.util.frame.FrameModelHandlerItf#
     * getDelimitation()
     */
    public int getDelimitation() {
        return this.delimitation;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.util.frame.FrameModelHandlerItf#
     * getStartDelimiter()
     */
    public Byte getStartDelimiter() {
        return this.startDelimiter;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.util.frame.FrameModelHandlerItf#
     * getEndDelimiter()
     */
    public Byte getEndDelimiter() {
        return this.endDelimiter;
    }

    /**
     * @inheritDoc
     * @see FrameModelHandler#getEscape()
     */
    public Byte getEscape() {
        return this.escape;
    }

    /**
     * Start to process a "frames" Element
     */
    public void framesStart(Attributes atts) {
        String startDelimiterStr = atts.getValue("start");
        if (startDelimiterStr != null) {
            this.startDelimiter = Byte.decode(startDelimiterStr);
        }
        String endDelimiterStr = atts.getValue("end");
        if (endDelimiterStr != null) {
            this.endDelimiter = Byte.decode(endDelimiterStr);
        }
        String escapeStr = atts.getValue("escape");
        if (escapeStr != null) {
            this.escape = Byte.decode(escapeStr);
        }
        String delimitationStr = atts.getValue("delimiter");
        this.delimitation = Integer.parseInt(delimitationStr);
    }

    /**
     * End to process a "frames" Element
     */
    public void framesEnd() {
        if (!stack.isEmpty()) {
            this.frameModel = (FrameModel) super.stack.pop();

        } else {
            this.frameModel = null;
        }
    }

    /**
     * Start to process an "frame" Element
     *
     * @throws FrameModelException
     */
    public FrameType frameStart(Attributes atts) throws FrameModelException {
        String typeStr = atts.getValue("name");
        FrameType type = null;
        if (typeStr != null) {
            type = this.typesMap.get(typeStr);
        }
        if (type != null) {
            FrameModel frameModel = FrameModelImpl.newInstance(this.mediator, atts, type);

            return frameModel;
        }
        return null;
    }

    /**
     * End to process a "frame" Element
     */
    public FrameType frameEnd() {
        if (super.stack.size() > 1) {
            FrameModel current = (FrameModel) super.stack.pop();
            FrameModel parent = (FrameModel) super.stack.pop();
            parent.addChild(current);

            return parent;
        }
        return null;
    }

    /**
     * End to process a "types" Element
     */
    public void typesEnd() {
        this.typesMap = new HashMap<String, FrameType>();

        while (!stack.isEmpty()) {
            FrameType type = super.stack.pop();
            this.typesMap.put(type.getName(), type);
        }
    }

    /**
     * Start to process a "types" Element
     */
    public FrameType typeStart(Attributes atts) {
        return FrameTypeImpl.newInstance(atts);
    }

    /**
     * Process an "name" Element
     */
    public FrameType nameEnd() {
        FrameType type = super.stack.pop();
        type.setName(textContent.toString().trim());
        return type;
    }

    /**
     * Process an "implementation" Element
     */
    public FrameType implementationEnd() {
        FrameType type = super.stack.pop();
        type.setClassName(textContent.toString().trim());
        return type;
    }

    /**
     * Process a "length" Element
     */
    public FrameType lengthEnd() {
        FrameType type = super.stack.pop();
        type.setLength(Integer.parseInt(textContent.toString().trim()));
        return type;
    }
}
