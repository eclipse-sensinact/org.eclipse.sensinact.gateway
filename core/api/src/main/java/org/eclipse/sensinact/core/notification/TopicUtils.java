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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private static String escapeTopicPart(final String topicPart, final boolean allowWildcards) {
        return topicPart.chars().mapToObj(c -> {
            if (c != '$'
                    && (c == '-' || Character.isJavaIdentifierPart(c) || (allowWildcards && (c == '*' || c == '+')))) {
                // Valid character other than '$', return as is
                return Character.toString(c);
            } else {
                // Escape the character as a Unicode escape sequence
                return String.format("$%04x", c);
            }
        }).collect(Collectors.joining());
    }

    /**
     * Unescapes a topic part by replacing the escape sequences with the original characters
     *
     * @param escapedTopicPart the topic part to unescape
     * @return the unescaped topic part
     */
    private static String unescapeTopicPart(final String escapedTopicPart) {
        final StringBuilder unescaped = new StringBuilder(escapedTopicPart.length());

        final Pattern escapePattern = Pattern.compile("\\$([a-fA-F0-9]{4})");
        final Matcher matcher = escapePattern.matcher(escapedTopicPart);

        if(matcher.find()) {
            int lastEnd = 0;
            do {
                if(matcher.start() > lastEnd) {
                    // Append the text before the escape sequence
                    unescaped.append(escapedTopicPart.subSequence(lastEnd, matcher.start()));
                }

                lastEnd = matcher.end();

                // Parse the block
                String hex = matcher.group(1);
                try {
                    int codePoint = Integer.parseInt(hex, 16);
                    unescaped.append((char) codePoint);
                } catch (NumberFormatException e) {
                    // Not a valid escape sequence, append the original text
                    unescaped.append(matcher.group());
                }
            } while (matcher.find());

            // Append the remaining text after the last escape sequence
            unescaped.append(escapedTopicPart.substring(lastEnd));
        } else {
            // No escape sequences found, return the original string
            return escapedTopicPart;
        }

        return unescaped.toString();
    }
}
