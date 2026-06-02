/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.core.notification;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Utility methods to manipulate the topic of a notification
 */
public class TopicUtils {

    /**
     * Escapes the given topic to be used as a topic in the typed event bus
     *
     * @param inputTopic the topic to escape
     * @return the escaped topic
     */
    public static String escapeTopic(final String inputTopic) {
        if (inputTopic == null || inputTopic.isEmpty()) {
            // Pass through
            return inputTopic;
        }

        return Arrays.stream(inputTopic.split("/")).map(topicPart -> escapeTopicPart(topicPart, false))
                .collect(Collectors.joining("/"));
    }

    /**
     * Unescapes the given topic to be used as a topic in the typed event bus
     *
     * @param escapedTopic the topic to unescape
     * @return the unescaped topic
     */
    public static String unescapeTopic(final String escapedTopic) {
        return Arrays.stream(escapedTopic.split("/")).map(TopicUtils::unescapeTopicPart)
                .collect(Collectors.joining("/"));
    }

    /**
     * Escapes the given topic filter to be used as a topic filter in the typed
     * event bus
     *
     * @param inputTopicFilter the topic filter to escape
     * @return the escaped topic filter
     */
    public static String escapeTopicFilter(final String inputTopicFilter) {
        return Arrays.stream(inputTopicFilter.split("/")).map(topicPart -> escapeTopicPart(topicPart, true))
                .collect(Collectors.joining("/"));
    }

    /**
     * Unescapes the given topic filter to be used as a topic filter in the typed
     * event bus
     *
     * @param escapedTopicFilter the topic filter to unescape
     * @return the unescaped topic filter
     */
    public static String unescapeTopicFilter(final String escapedTopicFilter) {
        return Arrays.stream(escapedTopicFilter.split("/")).map(TopicUtils::unescapeTopicPart)
                .collect(Collectors.joining("/"));
    }

    /**
     * Escapes a topic part by replacing invalid characters with their Unicode escape sequences
     *
     * @param topicPart the topic part to escape
     * @param allowWildcards whether to allow wildcard characters
     * @return the escaped topic part
     */
    public static CharSequence escapeTopicPart(final String topicPart, final boolean allowWildcards) {
        if(topicPart == null || topicPart.isEmpty()) {
            return topicPart;
        }

        StringBuilder sb = new StringBuilder(topicPart.length());

        for(int i = 0; i < topicPart.length(); i++) {
            int cp = topicPart.codePointAt(i);
            switch(cp) {
                case '$':
                    // $ must be escaped
                    sb.append("$0024");
                    break;
                case '*':
                    sb.append(allowWildcards? "*" : "$002a");
                    break;
                case '+':
                    sb.append(allowWildcards? "+" : "$002b");
                    break;
                case '-':
                    // can pass through
                    sb.append('-');
                    break;
                default:
                    if(Character.isJavaIdentifierPart(cp)) {
                        // permissable character
                        sb.appendCodePoint(cp);
                        // Step over the char we have already consumed
                        if(!Character.isBmpCodePoint(cp)) {
                            i++;
                        }
                    } else {
                        // must be escaped
                        if(Character.isBmpCodePoint(cp)) {
                            sb.append(String.format("$%04x", cp));
                        } else {
                            char[] chars = Character.toChars(cp);
                            sb.append(String.format("$%04x$%04x", (int)chars[0], (int)chars[1]));
                            // Step over the char we have already consumed
                            i++;
                        }
                    }
                    break;
            }
        }
        return sb;
    }

    /**
     * Unescapes a topic part by replacing the escape sequences with the original characters
     *
     * @param escapedTopicPart the topic part to unescape
     * @return the unescaped topic part
     */
    public static CharSequence unescapeTopicPart(final String escapedTopicPart) {
        if(escapedTopicPart == null || escapedTopicPart.isEmpty()) {
            return escapedTopicPart;
        }

        int idx = escapedTopicPart.indexOf('$');
        if(idx < 0) {
            return escapedTopicPart;
        } else {
            final StringBuilder unescaped = new StringBuilder(escapedTopicPart.subSequence(0, idx));
            for(int i = idx; i < escapedTopicPart.length(); i++) {
                int codePoint = escapedTopicPart.codePointAt(i);
                if(!Character.isBmpCodePoint(codePoint)) {
                    // We have moved two character indexes in the string
                    i++;
                } else if(codePoint == '$') {
                    if(escapedTopicPart.length() < i+5) {
                        throw new IllegalArgumentException("Invalid escape sequence " + escapedTopicPart.substring(i)
                            + " in segment " + escapedTopicPart);
                    }
                    try {
                        int first = Integer.parseInt(escapedTopicPart.substring(i+1, i+5), 16);
                        if(Character.isSurrogate((char)first)) {
                            if(escapedTopicPart.length() < i+10 || escapedTopicPart.charAt(i+5) != '$') {
                                throw new IllegalArgumentException("Invalid escape sequence " + escapedTopicPart.substring(i, i + 10)
                                + " in segment " + escapedTopicPart);
                            }
                            int second = Integer.parseInt(escapedTopicPart.substring(i+6, i+10), 16);
                            codePoint = Character.toCodePoint((char) first, (char) second);
                            // We have moved 9 characters forward in the string
                            i += 9;
                        } else {
                            codePoint = first;
                            // we have moved four character indexes in the string
                            i += 4;
                        }
                    } catch (NumberFormatException nfe) {
                        throw new IllegalArgumentException("Invalid escape sequence " + escapedTopicPart.substring(i, i + 4)
                            + " in segment " + escapedTopicPart);
                    }
                }
                unescaped.appendCodePoint(codePoint);
            }
            return unescaped.toString();
        }
    }
}
