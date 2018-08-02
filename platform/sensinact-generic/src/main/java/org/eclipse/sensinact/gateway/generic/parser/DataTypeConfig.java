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
package org.eclipse.sensinact.gateway.generic.parser;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class DataTypeConfig {
    private final AttributeDefinition attributeDefinition;

    /**
     * Constructor
     *
     * @param attributeDefinition the {@link AttributeDefinition} defining a
     *                            value {@link Attribute}
     */
    DataTypeConfig(AttributeDefinition attributeDefinition) {
        this.attributeDefinition = attributeDefinition;
    }

    /**
     * Returns the type of the associated {@link ExtResourceImpl}'s
     * enclosed value attribute for the specified target
     *
     * @param target the target's name for which retrieve the data type
     * @return the type of the associated {@link ExtResourceImpl}'s
     * enclosed attribute value for the specified target
     */
    public Class<?> getDataType() {
        TypeDefinition typeDefinition = attributeDefinition.getTypeDefinition();

        if (typeDefinition != null) {
            return typeDefinition.getType();
        }
        return null;
    }

    /**
     * Returns the default object value of the associated
     * {@link ExtResourceImpl}'s enclosed value attribute for
     * the specified target
     *
     * @param target the target's name for which retrieve the
     *               object value
     * @return the default object value of the associated
     * {@link ExtResourceImpl}'s enclosed value attribute
     * for the specified target
     */
    public Object getDefaultValue() {
        ValueDefinition valueDefinition = attributeDefinition.getValueDefinition();

        if (valueDefinition != null) {
            return valueDefinition.getValue();
        }
        return null;
    }
}
