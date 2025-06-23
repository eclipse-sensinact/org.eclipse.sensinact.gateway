/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.filters.resource.selector.jackson;

import java.io.IOException;

import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection.CheckType;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection.OperationType;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;

public class ValueSelectionDeserializer extends AbstractSelectionDeserializer<ValueSelection> {

    public ValueSelectionDeserializer() {
        super(ValueSelection.class);
    }

    private static final long serialVersionUID = -4402801442280922151L;

    @Override
    public ValueSelection convert(JsonNode root, DeserializationContext ctxt) throws IOException {
        return switch(root.getNodeType()) {
            case OBJECT: yield new ValueSelection(toString(root, "value", ctxt),
                    root.has("operation") ? ctxt.readTreeAsValue(root.get("operation"), OperationType.class) : null,
                    toBoolean(root, "negate", ctxt), 
                    root.has("checkType") ? ctxt.readTreeAsValue(root.get("checkType"), CheckType.class) : null);
            case STRING:
            case NUMBER:
            case BOOLEAN: yield new ValueSelection(toString(root, ctxt), null, false, null);
            case NULL: yield null;
            default:
                ctxt.reportBadCoercion(this, ValueSelection.class, root, "ValueSelection must be one of [Object, String, Number, Boolean, null] but was %s", root.getNodeType());
                // Never reached
                yield null;
        };
    }
}

