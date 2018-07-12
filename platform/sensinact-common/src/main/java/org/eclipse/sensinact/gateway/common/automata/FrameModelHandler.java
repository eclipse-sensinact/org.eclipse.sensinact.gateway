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

import org.xml.sax.ContentHandler;

/**
 * Signature of a service use create a {@link FrameModel} implementation
 * instance parsing an xml model
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface FrameModelHandler extends ContentHandler {
    /**
     * Return the created {@link FrameModel} using the parsed xml model
     *
     * @return the created {@link FrameModel}
     */
    FrameModel getModel();

    /**
     * Returns the delimitation type used to identify frames :
     * <p>
     * START_DELIMITED : 0;
     * END_DELIMITED : 1;
     * SIZE_DELIMITED : 2;
     * START_END_DELIMITED : 3;
     * SIZE_START_DELIMITED : 4;
     * SIZE_END_DELIMITED : 5;
     * SIZE_START_END_DELIMITED : 6;
     *
     * @return the delimitation type
     */
    int getDelimitation();

    /**
     * Returns the Byte used to delimit a starting frame
     *
     * @return the Byte used to delimit a starting frame
     */
    Byte getStartDelimiter();

    /**
     * Returns the Byte used to delimit an ending frame
     *
     * @return the Byte used to delimit an ending frame
     */
    Byte getEndDelimiter();

    /**
     * Returns the Byte used to specify that the next one has
     * to be interpreted as data instead of a control one
     *
     * @return the Byte used to specify an escape
     */
    Byte getEscape();
}
