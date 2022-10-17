/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.basic.sna;

import org.eclipse.sensinact.gateway.app.api.function.AbstractFunction;
import org.eclipse.sensinact.gateway.app.api.function.DataItf;
import org.eclipse.sensinact.gateway.app.api.plugin.PluginHook;

import java.util.List;

/**
 * The component that add an act/set action to the hook stack of the application
 *
 * @author Remi Druilhe
 * @see AbstractFunction
 */
public abstract class ActionFunction extends AbstractFunction<PluginHook> implements PluginHook {
    protected List<DataItf> variables;

    /**
     * The list of supported operators
     */
    public enum SnaOperator {
        ACT("act"), SET("set");
        private String type;

        SnaOperator(String type) {
            this.type = type;
        }

        public String getOperator() {
            return type;
        }
    }

    /**
     * @see AbstractFunction#process(List)
     */
    public void process(List<DataItf> datas) {
        this.variables = datas;
        super.update(this);
    }
}
