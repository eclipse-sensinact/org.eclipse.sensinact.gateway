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
package org.eclipse.sensinact.gateway.nthbnd.jsonpath.builder;

import com.jayway.jsonpath.Predicate.PredicateContext;
import com.jayway.jsonpath.internal.filter.JsonNode;
import com.jayway.jsonpath.internal.filter.ValueListNode;
import com.jayway.jsonpath.internal.filter.ValueNode;
import com.jayway.jsonpath.spi.builder.AbstractNodeBuilder;
import org.eclipse.sensinact.gateway.nthbnd.jsonpath.JsonOrgJsonTokener;
import org.json.JSONException;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class JsonOrgNodeBuilder extends AbstractNodeBuilder {
    class JsonNodeImpl extends JsonNode {

        /**
         * @param charSequence
         */
        JsonNodeImpl(CharSequence charSequence) {
            super(charSequence);
        }

        /**
         * @param parsedJson
         */
        JsonNodeImpl(Object parsedJson) {
            super(parsedJson);
        }

        /**
         * @inheritDoc
         * @see com.jayway.jsonpath.internal.filter.JsonNode#parse(com.jayway.jsonpath.Predicate.PredicateContext)
         */
        @Override
        public Object parse(PredicateContext ctx) {
            if (parsed) {
                return json;
            }
            try {
                return new JsonOrgJsonTokener(json.toString().trim()).nextValue();
            } catch (JSONException e) {
                throw new IllegalArgumentException(e);
            }
        }

        /**
         * @inheritDoc
         * @see com.jayway.jsonpath.internal.filter.JsonNode#asValueListNode(com.jayway.jsonpath.Predicate.PredicateContext)
         */
        @Override
        @SuppressWarnings("rawtypes")
        public ValueNode asValueListNode(PredicateContext ctx) {
            if (!isArray(ctx)) {
                return ValueNode.UNDEFINED;
            } else {
                List list = (List) parse(ctx);
                Iterator iterator = list.iterator();
                List<ValueNode> valueNodes = new ArrayList<ValueNode>();
                while (iterator.hasNext()) {
                    valueNodes.add(toValueNode(iterator.next()));
                }
                return new ValueListNode(Collections.unmodifiableList(valueNodes));
            }
        }

    }

    /**
     *
     */
    public JsonOrgNodeBuilder() {
    }

    /**
     * @inheritDoc
     * @see com.jayway.jsonpath.spi.builder.NodeBuilder#isJson(java.lang.Object)
     */
    @Override
    public boolean isJson(Object o) {
        if (o == null || !(o instanceof String)) {
            return false;
        }
        String str = o.toString().trim();
        if (str.length() <= 1) {
            return false;
        }
        char c0 = str.charAt(0);
        char c1 = str.charAt(str.length() - 1);
        if ((c0 == '[' && c1 == ']') || (c0 == '{' && c1 == '}')) {
            try {
                new JSONTokener(str).nextValue();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    /**
     * @inheritDoc
     * @see com.jayway.jsonpath.spi.builder.NodeBuilder#createJsonNode(java.lang.CharSequence)
     */
    @Override
    public JsonNode createJsonNode(CharSequence json) {
        JsonNodeImpl n = new JsonNodeImpl(json);
        return n;
    }

    /**
     * @inheritDoc
     * @see com.jayway.jsonpath.spi.builder.NodeBuilder#createJsonNode(java.lang.Object)
     */
    @Override
    public JsonNode createJsonNode(Object parsedJson) {
        JsonNodeImpl n = new JsonNodeImpl(parsedJson);
        return n;
    }
}