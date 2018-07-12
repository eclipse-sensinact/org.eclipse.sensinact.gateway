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
package org.eclipse.sensinact.gateway.common.automata.test;

import org.eclipse.sensinact.gateway.common.automata.AbstractFrame;
import org.eclipse.sensinact.gateway.common.automata.FrameException;

/**
 * Implementation of the {@link Frame} interface for a
 * frame representing ASCII data
 */
public class ASCIIDataFrame extends AbstractFrame {
    /**
     * Byte to replace non ASCII characters
     */
    private static final byte UNKNOWN_ASCII_CHAR = 0x3F;

    /**
     * The associated Date object
     */
    private String data;

    public ASCIIDataFrame() {
        super();
    }

    /**
     * Return the ASCII String data represented by the
     * bytes array frame
     *
     * @return the associated data
     */
    public String getData() {
        byte[] frame = super.getBytes();
        if (this.data == null && frame != null) {
            int index = 0;
            StringBuilder dataBuilder = new StringBuilder();

            for (; index < super._pos; index++) {
                byte octet = frame[index];
                char character = (char) (octet & MASK);
                dataBuilder.append(character);
            }
            this.data = dataBuilder.toString();
        }
        return this.data;
    }

    /**
     * Define the ASCII String data represented by the
     * bytes array frame
     *
     * @param data the associated data
     * @throws FrameException
     */
    public void setData(String data) throws FrameException {
        char[] characters = data.toCharArray();
        for (char character : characters) {
            int charInt = (int) character;
            if (charInt < 256) {
                super.append((byte) (charInt & MASK));

            } else {
                super.append(UNKNOWN_ASCII_CHAR);
            }
        }
        this.data = data;
    }
}
