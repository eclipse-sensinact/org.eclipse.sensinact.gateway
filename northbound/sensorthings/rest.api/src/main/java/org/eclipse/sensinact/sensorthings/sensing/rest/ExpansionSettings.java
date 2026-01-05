/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.sensorthings.sensing.rest;

import org.eclipse.sensinact.sensorthings.sensing.dto.Id;

public interface ExpansionSettings {

    ExpansionSettings getExpansionSettings(String pathSegment);

    boolean shouldExpand(String pathSegment, Id context);

    void addExpansion(String pathSegment, Id context, Object expansion);

    ExpansionSettings EMPTY = new ExpansionSettings() {
        @Override
        public boolean shouldExpand(String pathSegment, Id context) {
            return false;
        }

        @Override
        public ExpansionSettings getExpansionSettings(String pathSegment) {
            throw new IllegalArgumentException("Empty Expansion Settings");
        }

        @Override
        public void addExpansion(String pathSegment, Id context, Object expansion) {
            throw new IllegalArgumentException("Empty Expansion Settings");
        }
    };
}
