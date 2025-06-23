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

import org.eclipse.sensinact.filters.resource.selector.api.Selection;
import org.eclipse.sensinact.filters.resource.selector.api.Selection.MatchType;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;

public class SelectionDeserializer extends AbstractSelectionDeserializer<Selection> {

    private static final long serialVersionUID = 2805370682011614734L;

    protected SelectionDeserializer() {
        super(Selection.class);
    }

    @Override
    public Selection convert(JsonNode root, DeserializationContext ctxt) throws IOException {
        return switch(root.getNodeType()) {
            case STRING: yield new Selection(root.textValue(), MatchType.EXACT, false);
            case OBJECT: yield new Selection(toString(root, "value", ctxt), 
                    ctxt.readTreeAsValue(root.get("type"), MatchType.class),
                    toBoolean(root, "negate", ctxt));
            case NULL: yield null;
            default:
                ctxt.reportBadCoercion(this, Selection.class, root, "Selection must be a String or JSON object but was %s", root.getNodeType());
                // Never reached
                yield null;
        };
    }
}
