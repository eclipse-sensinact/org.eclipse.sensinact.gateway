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
import org.eclipse.sensinact.gateway.common.automata.Frame;
import org.eclipse.sensinact.gateway.common.automata.FrameException;

import java.util.Arrays;

public class TFrame extends AbstractFrame {

    private byte[] frame;


    public TFrame() {
        super();
        this.frame = new byte[10];
    }

    /**
     * @inheritDoc
     * @see Frame.box.services.api.frame.FrameItf#getBytes()
     */
    @Override
    public byte[] getBytes() {
        return Arrays.copyOfRange(this.frame, 0, super._pos);
    }

    /**
     * @inheritDoc
     * @see Frame.box.services.api.frame.FrameItf#append(byte)
     */
    @Override
    public void append(byte bte) throws FrameException {
        super.append(bte);
        increase();
    }

    /**
     * @inheritDoc
     * @see Frame.box.services.api.frame.FrameItf#append(byte[])
     */
    @Override
    public void set(byte bte, int pos) throws FrameException {
        this.frame[pos] = bte;
    }

    /**
     * Increase the size of the bytes array frame
     */
    private void increase() {
        if ((super._pos + 3) > this.frame.length) {
            byte[] newFrame = new byte[this.frame.length + 10];
            System.arraycopy(this.frame, 0, newFrame, 0, super._pos);
            this.frame = newFrame;
        }
    }
}
