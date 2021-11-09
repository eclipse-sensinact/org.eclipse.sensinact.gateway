/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.util.json;

import org.eclipse.sensinact.gateway.util.CastUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A JSONObject builder
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class JSONMapper {
    static final Pattern PATH_PATTERN = Pattern.compile("\\/?([^\\/]+)");
    static final Pattern PATH_ELEMENT_PATTERN = Pattern.compile("([^\\[\\]]+)?(?:\\[([^\\[\\]]+)(?:\\]))?");
    static final Pattern PATH_CONDITION_PATTERN = Pattern.compile("([^\\(\\)]+)|((\\()\\1(\\)))");
    static final Pattern PATH_EXPRESSION_PATTERN = Pattern.compile("(((&|\\|)?([^&\\|]+))?)");
    static final Pattern PATH_CONSTRAINT_PATTERN = Pattern.compile("([^=<>!]+)((=|<|>|(!=))([^=<>!]+))");

    private Map<String, JSONPath> mapping;
    private Map<String, Object> patterns;


    /**
     * Constructor
     */
    private JSONMapper() {
        //{
        //"mykey1" : [
        //"55s",
        //"aaaa" ,
        //"9",
        //["tre", "44"],
        //{"myembeddedkey1" : "myembeddedvalue1","myembeddedkey2" : "myembeddedvalue2"},
        //12
        //],
        //"mykey2" : "myvalue2",
        //"mykey3" : {"myembeddedkey3" : "myembeddedvalue3","myembeddedkey4" : "myembeddedvalue4"},
        //"mykey4" : 88.5}


        //("/*/$(deep:1))"
        //("/mykey1/$(index:3))"
        //("/mykey1)"
        //("/$(pattern:mykey[2|4]))"
        //("/mykey3/myembeddedkey4)"
        this(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Constructor
     */
    private JSONMapper(ClassLoader classloader) {
        this.mapping = new HashMap<String, JSONPath>();
        this.patterns = new HashMap<String, Object>();
    }

    /**
     * Constructor
     *
     * @param mapping
     */
    public JSONMapper(JSONObject mapping) {
        this();
        int length = mapping == null ? 0 : mapping.length();
        if (length > 0) {
            int index = 0;

            String[] names = JSONObject.getNames(mapping);
            for (; index < length; index++) {
                this.mapping.put(names[index], new JSONPath(mapping.getString(names[index])));
            }
        }
    }

    /**
     * Constructor
     *
     * @param mapping
     */
    public JSONMapper(Map<String, String> mapping) {
        this();
        int length = mapping == null ? 0 : mapping.size();
        if (length > 0) {
            String key = null;
            for (Iterator<String> iterator = mapping.keySet().iterator(); iterator.hasNext(); key = iterator.next()) {
                this.mapping.put(key, new JSONPath(mapping.get(key)));
            }
        }
    }

    /**
     * Rebuilds and returns a JSONObject whose keys are the
     * ones of this JSONMapper and the values are the ones
     * extracted from the JSON object (JSONObject or JSONArray)
     * passed as parameter.
     *
     * @param json the JSON object (JSONObject or JSONArray) from which
     *             the values of the returned JSONObject are extracted
     * @return the builds JSONObject mapping the keys of this
     * JSONMapper to the values of the specified JSON object
     */
    public JSONObject parse(Object json) {
        String pattern = null;
        JSONObject result = new JSONObject();

        for (Iterator<String> iterator = this.mapping.keySet().iterator(); iterator.hasNext(); ) {
            String key = iterator.next();
//            JSONPath jsonPath = this.mapping.get(key);

            Iterator<String> patternIterator = this.patterns.keySet().iterator();
            while (patternIterator.hasNext()) {
                pattern = patternIterator.next();
                Object current = null;
                if (pattern != null) {
                    Object subject = this.patterns.get(pattern);
                    subject = subject == null ? json : subject;
                    current = this.mapping.get(key).extract(subject);
                    this.patterns.put(pattern, current);

                } else {
                    current = this.mapping.get(key).extract(json);
                }
                result.put(key, current);
            }
        }
        return result;
    }

    /**
     *
     */
    final class JSONPath {
        List<JSONPathElement> pathElements;

        JSONPath(String path) {
            pathElements = new LinkedList<JSONPathElement>();
            Matcher matcher = JSONMapper.PATH_PATTERN.matcher(path);
            while (matcher.find()) {
                this.pathElements.add(new JSONPathElement(matcher.group()));
            }
        }

        /**
         * Extracts the object from the JSON one passed as parameter
         * (JSONObject or JSONArray) according to this path.
         *
         * @param json the JSON object (JSONObject or JSONArray) from which
         *             to extract the search element(s)
         * @param json
         * @return the extracted object
         */
        private Object extract(Object json) {
            if (json == null || this.pathElements.size() == 0) {
                return null;
            }
            Object currentObject = json;
            Iterator<JSONPathElement> iterator = this.pathElements.iterator();

            while (iterator.hasNext()) {
                currentObject = iterator.next().extract(currentObject);
            }
            return currentObject;
        }
    }

    /**
     *
     */
    final class JSONPathElement {
        String element;
        JSONPathCondition condition;

        JSONPathElement(String pathElement) {
            Matcher matcher = null;

            if (!(matcher = JSONMapper.PATH_ELEMENT_PATTERN.matcher(pathElement)).matches()) {
                return;
            }
            element = matcher.group(1);
            String condition = null;
            if ((condition = matcher.group(2)) != null) {
                //for now only '=' operator available
                //if no operator the condition is evaluated as a one of presence
                //TODO : build a real condition/use constraint
                this.condition = new JSONPathCondition(condition);
            }
        }

        /**
         * Extracts the object from the JSON one passed as parameter
         * (JSONObject or JSONArray) according to this path element.
         *
         * @param json the JSON object (JSONObject or JSONArray) from which
         *             to extract the search element(s)
         * @return the extracted object
         */
        private Object extract(Object json) {
            if (json == null) {
                return null;
            }
            if (JSONArray.class.isAssignableFrom(json.getClass())) {
                JSONArray operand = (JSONArray) json;
                if (this.element != null) {
                    JSONArray resultArray = new JSONArray();
                    int index = 0;
                    int length = operand.length();

                    for (; index < length; index++) {
                        Object result = extract(operand.get(index));
                        if (result != null) {
                            resultArray.put(result);
                        }
                    }
                    return resultArray;
                }
                if (this.condition != null) {
                    return this.condition.apply(json);
                }
            } else if (JSONObject.class.isAssignableFrom(json.getClass())) {
                if (this.element != null) {
                    Object object = ((JSONObject) json).opt(this.element);
                    if (this.condition != null) {
                        return this.condition.apply(object);

                    } else {
                        return object;
                    }
                }
                if (this.condition != null) {
                    this.condition.apply(json);
                }
            }
            return null;
        }

        private class JSONPathCondition {
            String key = null;
            String value = null;

            JSONPathCondition(String condition) {
                char[] conditionChars = condition == null ? new char[0] : condition.toCharArray();

                int index = 0;
                int length = conditionChars.length;
                length += length > 0 ? 1 : 0;
                char[] chars = new char[length];
                if (length > 0) {
                    System.arraycopy(conditionChars, 0, chars, 0, conditionChars.length);
                    chars[length - 1] = '\0';
                }
                StringBuilder sb = new StringBuilder();
                for (; ; ) {
                    if (index == chars.length) {
                        break;
                    }
                    char c = chars[index++];
                    switch (c) {
                        case '=':
                            key = sb.toString();
                            sb = new StringBuilder();
                            break;
                        case ' ':
                            break;
                        case '\0':
                            value = sb.toString();
                            break;
                        default:
                            sb.append(c);
                    }
                }
            }

            public Object apply(Object json) {
                if (JSONArray.class.isAssignableFrom(json.getClass())) {
                    JSONArray jsonArray = (JSONArray) json;
                    if (key == null) {
                        if (value == null) {
                            return json;
                        }
                        try {
                            return jsonArray.get(Integer.parseInt(value));

                        } catch (NumberFormatException e) {
                            return null;

                        } catch (ArrayIndexOutOfBoundsException e) {
                            return null;
                        }
                    }
                    JSONArray resultArray = new JSONArray();
                    int index = 0;
                    int length = jsonArray.length();

                    for (; index < length; index++) {
                        Object result = apply(jsonArray.get(index));
                        if (result != null) {
                            resultArray.put(result);
                        }
                    }
                    return resultArray;

                } else if (JSONObject.class.isAssignableFrom(json.getClass())) {
                    JSONObject jsonObject = (JSONObject) json;
                    if (key == null) {
                        if (value == null || jsonObject.opt(value) != null) {
                            return json;
                        }
                        return null;
                    }
                    Object result = null;
                    try {
                        if ((result = jsonObject.opt(key)) != null && result.equals(CastUtils.cast(result.getClass(), value))) {
                            return json;
                        }
                    } catch (ClassCastException e) {
                        return null;
                    }
                }
                return null;
            }
        }

    }

}
