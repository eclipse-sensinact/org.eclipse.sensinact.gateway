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

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Abstract implementation of the {@link Frame} interface
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractFrame implements Frame {
    //Bits mask
    public static final int MASK = 0x00FF;
    public static final int NULL_LENGTH = 0x00;
    //
    protected Frame _parent;
    protected LinkedList<Frame> children = null;
    protected int _pos = 0;
    protected int _length = 0;
    protected int _offset = 0;

    private boolean calculating = false;

    //All extended classes have to implement an empty constructor
    protected AbstractFrame() {
        this.children = new LinkedList<Frame>();
    }

    /**
     * @inheritDoc
     * @see Frame.box.services.api.frame.FrameItf#children()
     */
    public Frame[] getChildren() {
        if (this.children == null) {
            return new Frame[0];
        }
        return children.toArray(new Frame[children.size()]);
    }

    /**
     * @inheritDoc
     * @see Frame.box.services.api.frame.FrameItf#
     * addChild(Frame.box.services.api.frame.FrameItf)
     */
    public void addChild(Frame child) {
        if (child != null) {
            child.setParent(this);
            children.offer(child);
        }
    }

    /**
     * @inheritDoc
     * @see Frame.box.services.api.frame.FrameItf#getBytes()
     */
    public byte[] getBytes() {
        byte[] frame = null;
        if (this._parent != null && this._pos > 0) {
            frame = Arrays.copyOfRange(this._parent.getBytes(), this._offset, (this._offset + this._pos));
        }
        return frame;
    }

    /**
     * @inheritDoc
     * @see Frame.box.services.api.frame.FrameItf#append(byte)
     */
    public void append(byte bte) throws FrameException {
        if (this.size() == 0) {
            this.set(bte, this._pos);

        } else {
            Iterator<Frame> iterator = this.children.iterator();
            while (iterator.hasNext()) {
                Frame frame = iterator.next();
                if (!frame.isComplete()) {
                    frame.append(bte);
                    break;
                }
            }
        }
        this._pos++;
    }

    /**
     * @inheritDoc
     * @see Frame.box.services.api.frame.FrameItf#append(byte[])
     */
    public void set(byte bte, int pos) throws FrameException {
        if (this._parent != null) {
            this._parent.set(bte, (this._offset + pos));
        }
    }

    /**
     * @inheritDoc
     * @see Frame.box.services.api.frame.FrameItf#length()
     */
    public int getLength() {
        if (this.calculating) {
            return NULL_LENGTH;
        }
        if (this._length == 0) {
            this.calculating = true;
            //The length of the current frame can be found making the
            //difference between the parent's length and the sum of its
            //children's length except the current one
            if (this.getParent() != null && (this._length = this.getParent().getLength()) > 0) {
                for (Frame child : this.getParent().getChildren()) {
                    this._length -= (child == this) ? 0 : child.getLength();
                }
            }
            //The length of the current frame can be found making the
            //sum of children lengths
            if (this._length == 0 && this.size() > 0) {
                for (Frame child : children) {
                    int length = child.getLength();
                    if (length == 0) {
                        this._length = 0;
                        break;

                    } else {
                        this._length += child.getLength();
                    }
                }
            }
            this.calculating = false;
        }
        return this._length;
    }

    /**
     * @inheritDoc
     * @see Frame.box.services.api.frame.FrameItf#
     * setLength(int)
     */
    public void setLength(int length) {
        this._length = length;
    }

    /**
     * @inheritDoc
     * @see Frame.box.services.api.frame.FrameItf#
     * setOffset(int)
     */
    public void setOffset(int offset) {
        this._offset = offset;
    }

    /**
     * @inheritDoc
     * @see Frame.box.services.api.frame.FrameItf#
     * setParent(Frame.box.services.api.frame.FrameItf)
     */
    public void setParent(Frame parent) {
        this._parent = parent;
    }

    /**
     * @inheritDoc
     * @see Frame.box.services.api.frame.FrameItf#getParent()
     */
    public Frame getParent() {
        return this._parent;
    }

    /**
     * @inheritDoc
     * @see Frame.box.services.api.frame.FrameItf#size()
     */
    public int size() {
        return this.children == null ? 0 : this.children.size();
    }

    /**
     * @inheritDoc
     * @see Frame.box.services.api.frame.FrameItf#isComplete()
     */
    public boolean isComplete() {
        return (this._pos > 0 && this._pos == this.getLength());
    }

    /**
     * @inheritDoc
     * @see Frame.box.services.api.frame.FrameItf#clean()
     */
    public void clean() {
        if (isComplete()) {
            int index = 0;
            while (index < this.size()) {
                Frame child = this.children.get(index);
                if (!child.isComplete()) {
                    child.setParent(null);
                    child = null;
                    this.children.remove(index);

                } else {
                    index++;
                    child.clean();
                }
            }
        }
    }

    /**
     * @inheritDoc
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getClass().getSimpleName());
        builder.append(":");
        builder.append(super.toString());
        builder.append("[ ");

        int index = 0;
        byte[] frame = this.getBytes();

        if (frame != null) {
            for (; index < frame.length; index++) {
                builder.append("0x");
                builder.append(String.format("%02X", frame[index]));
                builder.append(" ");
            }
        }
        builder.append("]");
        index = 0;
        for (; index < this.size(); index++) {
            builder.append("\n\t");
            builder.append(this.children.get(index).toString());
        }
        return builder.toString();
    }
}
