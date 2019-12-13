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
package org.eclipse.sensinact.gateway.device.openhab.internal;

import java.text.ParseException;

enum OpenhabType {
    SwitchItem,
    Switch,
    String,
    NumberItem,
    Number,
    Number_Temperature {
        @Override
        protected Object parseValue(final Object value) {
            Object parsedValue = null;
            if (value != null && !value.equals("NULL")) {
                try {
                    final Object[] parsedArray = OpenHabPacketReader.TEMPERATURE_FORMAT.parse(value.toString());
                    parsedValue = parsedArray[0];
                } catch (ParseException ex) {
                    OpenHabPacketReader.LOG.error("unexpected format for {}: not a {} format?", value, this);
                }
            }
            return parsedValue;
        }
    },
    Player,
    Rollershutter,
    Dimmer,
    Contact,
    Color,
    DateTime,
    Group,
    Image,
    Location,
    Default;
    
    
    private static OpenhabType getType(final String type$) {
        final String openhabType$ = type$.replaceAll(":", "_");
        OpenhabType openhabType = Default;
        try {
            openhabType = OpenhabType.valueOf(openhabType$);
        } catch (Exception e) {
            OpenHabPacketReader.LOG.error("unsupported openhab type {}. Using {}...", type$, openhabType, e);
        }
        return openhabType;
    }
    
    static Object parseValue(final String type, final Object value) {
        OpenhabType openhabType = getType(type);
        return openhabType.parseValue(value);
    }

    protected Object parseValue(final Object value) {
        return value;
    }
}