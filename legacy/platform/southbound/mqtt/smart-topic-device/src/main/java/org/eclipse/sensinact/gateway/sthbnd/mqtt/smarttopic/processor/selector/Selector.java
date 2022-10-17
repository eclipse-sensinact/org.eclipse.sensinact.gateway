/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.selector;

/**
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class Selector implements SelectorIface {
    private final String name;
    private final String expression;

    public Selector(String name, String expression) {
        this.name = name;
        this.expression = expression;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getExpression() {
        return expression;
    }
}
