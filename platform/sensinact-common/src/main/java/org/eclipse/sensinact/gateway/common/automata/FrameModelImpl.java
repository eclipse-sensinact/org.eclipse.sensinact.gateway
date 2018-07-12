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
import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the {@link FrameModel} interface
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class FrameModelImpl implements FrameModel {
    private final FrameType type;

    private Mediator mediator;
    private List<FrameModel> children;
    private int offset;

    /**
     * Return a new {@link FrameModel} implementation instance
     *
     * @param attributes the {@link Attributes} of the associated XML element
     * @return a new {@link FrameModel} implementation instance
     */
    public static FrameModel newInstance(Mediator mediator, Attributes attributes, FrameType type) {
        FrameModelImpl model = new FrameModelImpl(mediator, type);
        String offsetStr = attributes.getValue("offset");

        if (offsetStr != null) {
            model.setOffset(Integer.parseInt(offsetStr));
        }
        return model;
    }

    /**
     * Constructor
     */
    public FrameModelImpl(Mediator mediator, FrameType type) {
        this.type = type;
        this.mediator = mediator;
    }

    /**
     * - only one zero-length frame by scope which has to be the last declared
     * in its scope
     * <p>
     * the others rules are defined in the model's schema :
     * frames.xsd
     *
     * @inheritDoc
     * @see FrameModel.box.services.api.frame.model.FrameModelItf#checkValid()
     */
    public void checkValid() throws FrameModelException {
        if ((children == null) || (children.size() == 0)) {
            return;
        }
        int index = 0;

        for (; index < children.size(); index++) {
            FrameModel child = children.get(index);
            ((FrameModelImpl) child).checkValid();

            if ((child.length() == 0) && index < (children.size() - 1)) {
                throw new FrameModelException("a zero-length frame must be the last one declared in its scope");
            }
        }
    }

    /**
     * @inheritDoc
     * @see FrameModel.box.services.api.frame.model.FrameModelItf#
     * children()
     */
    public FrameModel[] children() {
        if (children == null) {
            return new FrameModel[0];
        }
        return this.children.toArray(new FrameModel[this.children.size()]);
    }

    /**
     * @inheritDoc
     * @see FrameModel.box.services.api.frame.model.FrameModelItf#
     * addChild(FrameModel.box.services.api.frame.model.FrameModelItf)
     */
    public void addChild(FrameModel model) {
        if (model != null) {
            if (this.children == null) {
                this.children = new ArrayList<FrameModel>();
            }
            children.add(model);
        }
    }

    /**
     * @inheritDoc
     * @see FrameModel.box.services.api.frame.model.FrameModelItf#setOffset(int)
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * @inheritDoc
     * @see FrameModel.box.services.api.frame.model.FrameModelItf#
     * offset(int)
     */
    public int offset() {
        return this.offset;
    }

    /**
     * @inheritDoc
     * @see FrameModel.box.services.api.frame.model.FrameModelItf#size()
     */
    public int size() {
        if (this.children == null) {
            return 0;
        }
        return this.children.size();
    }

    /**
     * @inheritDoc
     * @see FrameType.box.services.api.frame.model.FrameTypeItf#length()
     */
    public int length() {
        return this.type.length();
    }

    /**
     * @inheritDoc
     * @see FrameType.box.services.api.frame.model.FrameTypeItf#getClassName()
     */
    public String getClassName() {
        return this.type.getClassName();
    }

    /**
     * @inheritDoc
     * @see FrameType.box.services.api.frame.model.FrameTypeItf#getName()
     */
    public String getName() {
        return this.type.getName();
    }

    /**
     * @inheritDoc
     * @see FrameType.box.services.api.frame.model.FrameTypeItf#setName(java.lang.String)
     */
    public void setName(String name) {
        this.mediator.debug("The name cannot be parameterized throught the model");
    }

    /**
     * @inheritDoc
     * @see FrameType.box.services.api.frame.model.FrameTypeItf#setLength(int)
     */
    public void setLength(int length) {
        this.mediator.debug("The length cannot be parameterized throught the model");
    }

    /**
     * @inheritDoc
     * @see FrameType.box.services.api.frame.model.FrameTypeItf#setClassName(java.lang.String)
     */
    public void setClassName(String className) {
        this.mediator.debug("The type cannot be parameterized throught the model");
    }
}
