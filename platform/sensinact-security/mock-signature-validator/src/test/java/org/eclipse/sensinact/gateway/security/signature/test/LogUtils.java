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
package org.eclipse.sensinact.gateway.security.signature.test;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.Calendar;

public class LogUtils {
    private long beginning = 0;

    public static void logStringArray(final Logger logger, final String header, final String[] array) {
        LogUtils.logStringArray(logger, header, array, Level.DEBUG);
    }

    public static void logStringArray(final Logger logger, final String header, final String[] array, final Level level) {
        final StringBuffer logData = new StringBuffer();
        if (array == null) {
            logData.append("void array");
        } else {

            logData.append(header);
            for (int i = 0; i < array.length; i++) {
                logData.append(array[i]);
                logData.append(';');
            }
        }
        logger.log(level, logData.toString());
    }

    public void initTimeMeasure() {
        beginning = Calendar.getInstance().getTimeInMillis();
    }

    public void showDuration(final Logger logger, final String task) {
        long end = Calendar.getInstance().getTimeInMillis();
        logger.log(Level.DEBUG, "beginning: " + beginning);
        logger.log(Level.DEBUG, "end: " + end);

        long duration = end - beginning;
        logger.log(Level.DEBUG, "duration for " + task + ": " + duration + " ms.");
    }
}
