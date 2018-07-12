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

import java.util.Date;
import java.util.TimeZone;

/**
 * Implementation of the {@link Frame} interface for a
 * frame representing a Date
 */
public class UnixEpoch extends AbstractFrame {
    /**
     * The static fix length of the associated bytes array frame
     */
    public static final int FIX_LENGTH = 0x04;

    public static final int DECAL_BITS_24 = 0x18;
    public static final int DECAL_BITS_16 = 0x10;
    public static final int DECAL_BITS_8 = 0x08;
    //
    public static final int SECONDS_MULTIPLICATOR = 0x3e8;

    /**
     * The associated Date object
     */
    private Date date;

    public UnixEpoch() {
        super();
    }

    /**
     * Return the Date object represented by the
     * bytes array frame
     *
     * @return the associated date
     */
    public Date getDate() {
        byte[] frame = super.getBytes();
        if (this.date == null && this.isComplete()) {
            long unixEpoch = ((frame[0] & MASK) << DECAL_BITS_24 | (frame[1] & MASK) << DECAL_BITS_16 | (frame[2] & MASK) << DECAL_BITS_8 | (frame[3] & MASK));

            long millis = (unixEpoch * SECONDS_MULTIPLICATOR);
            this.date = new Date(millis);
        }
        return this.date;
    }

    /**
     * Sets the associated Date as now
     */
    public void setDate() throws FrameException {
        int unixEpoch = unixEpoch();
        super.set((byte) ((unixEpoch >>> DECAL_BITS_24) & MASK), 0);
        super.set((byte) ((unixEpoch >> DECAL_BITS_16) & MASK), 1);
        super.set((byte) ((unixEpoch >> DECAL_BITS_8) & MASK), 2);
        super.set((byte) (unixEpoch & MASK), 3);
        super._pos = 4;
    }

    /**
     * Appends the associated Date as now
     */
    public void appendDate() throws FrameException {
        int unixEpoch = unixEpoch();
        super.append((byte) ((unixEpoch >>> DECAL_BITS_24) & MASK));
        super.append((byte) ((unixEpoch >> DECAL_BITS_16) & MASK));
        super.append((byte) ((unixEpoch >> DECAL_BITS_8) & MASK));
        super.append((byte) (unixEpoch & MASK));
    }

    /**
     * Returns the current date as an unix epoch formated
     * integer
     *
     * @return the current date as an unix epoch formated
     * integer
     * @throws FrameException
     */
    private int unixEpoch() throws FrameException {
        long unixTime = (System.currentTimeMillis() / 1000L);

        if (unixTime < Integer.MIN_VALUE || unixTime > Integer.MAX_VALUE) {
            throw new FrameException(unixTime + " cannot be cast to int without changing its value.");
        }
        return unixEpoch(unixTime);
    }

    /**
     * Returns the long milliseconds date passed as argument
     * as an unix epoch formated integer
     *
     * @return the date as an unix epoch formated integer
     * @throws FrameException
     */
    private int unixEpoch(long unixTime) {
        TimeZone tz = TimeZone.getDefault();
        int dstSaving = tz.getDSTSavings() / 1000;
        int offSet = tz.getRawOffset() / 1000;

        int unixEpoch = (int) unixTime;
        unixEpoch += dstSaving;
        unixEpoch += offSet;

        return unixEpoch;
    }
}
