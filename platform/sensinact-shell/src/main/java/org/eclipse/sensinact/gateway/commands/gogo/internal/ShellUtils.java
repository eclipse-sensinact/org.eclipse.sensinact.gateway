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
package org.eclipse.sensinact.gateway.commands.gogo.internal;

class ShellUtils {

    /**
     * Generate a line separator made of "-"
     * @param numberOfCharacters the number of "-" in the line separator
     * @return the line separator
     */
    static String lineSeparator(int numberOfCharacters) {
        String lineString = "";

        for (int i = 0; i<numberOfCharacters; i++) {
            lineString = lineString + "-";
        }

        return lineString;
    }

    /**
     * Generate a tabulation
     * @param numberOfCharacters the number of characters in the tabulation
     * @return the tabulation
     */
    static String tabSeparator(int numberOfCharacters) {
        String tabString = "";

        for (int i = 0; i<numberOfCharacters; i++) {
            tabString = tabString + " ";
        }

        return tabString;
    }
}
