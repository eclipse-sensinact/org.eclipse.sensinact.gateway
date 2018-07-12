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

import org.xml.sax.Attributes;

/**
 * Implementation of the {@link FrameType} service
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class FrameTypeImpl implements FrameType {
    private int length = 0;
    private String className = null;
    private String name = null;

    /**
     * Return a new {@link FrameType} implementation instance
     *
     * @param attributes the {@link Attributes} of the associated XML element
     * @return a new {@link FrameType} implementation instance
     */
    public static FrameType newInstance(Attributes attributes) {
        return new FrameTypeImpl();
    }

    /**
     * @inheritDoc
     * @see sensinact.box.services.api.frame.model.FrameType#length()
     */
    public int length() {
        return this.length;
    }

    /**
     * @inheritDoc
     * @see sensinact.box.services.api.frame.model.FrameType#setLength(int)
     */
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * @inheritDoc
     * @see sensinact.box.services.api.frame.model.FrameType#getClassName()
     */
    public String getClassName() {
        return this.className;
    }

    /**
     * @inheritDoc
     * @see sensinact.box.services.api.frame.model.FrameType#setClassName(java.lang.String)
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @inheritDoc
     * @see sensinact.box.services.api.frame.model.FrameType#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * @inheritDoc
     * @see sensinact.box.services.api.frame.model.FrameType#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }
}
