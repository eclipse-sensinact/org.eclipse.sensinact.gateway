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

/**
 * Signature of a service which is able to build a {@link Frame}
 * implementation instance using an array of bytes
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface Processor {
    /**
     * Returns the last {@link Frame} build
     *
     * @return the last {@link Frame} build
     */
    Frame getFrame();

    /**
     * Returns a new empty {@link Frame} created using the
     * {@link FrameModel} passed as parameter
     *
     * @param model the {@link FrameModel} use to create the new
     *              {@link Frame} implementation instance
     * @return a new empty {@link Frame}
     * @throws FrameProcessorException
     * @throws FrameException
     */
    Frame newFrame(FrameModel model) throws FrameProcessorException, FrameException;

    /**
     * Returns a new {@link FrameModel} created using the xml file model
     * which path is passed as parameter
     *
     * @return a new {@link FrameModel}
     * @throws FrameModelException
     */
    FrameModelHandler newModel(String xmlModelPath) throws FrameModelException;

    /**
     * Returns a the frame delimitation use to create
     * {@link Frame} implementation instances
     *
     * @return the delimitation type
     * @throws FrameModelException
     * @see sensinact.box.services.api.Frame
     */
    int getDelimitation();

    /**
     * Returns the array of bytes (potentially one at the start and
     * one at the end) used to delimit a frame
     *
     * @return the array of bytes used to delimit a frame
     * @throws FrameModelException
     */
    byte[] getDelimiters();
}
