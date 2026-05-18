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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TopicUtilsTest {

    @Test
    void testEscapeTopic() {
        // Test that normal topics are not changed
        String topic = "a/b/c";
        assertEquals(topic, TopicUtils.escapeTopic(topic));
        assertEquals(topic, TopicUtils.unescapeTopic(topic));

        // Test '-' (not escaped) and wildcard characters (escaped)
        topic = "a/b-c/d+e/f*g";
        assertEquals("a/b-c/d$002be/f$002ag", TopicUtils.escapeTopic(topic));
        assertEquals(topic, TopicUtils.unescapeTopic(TopicUtils.escapeTopic(topic)));

        // Test that topics with special characters are escaped
        topic = "a/$b/c/d~e";
        assertEquals("a/$0024b/c/d$007ee", TopicUtils.escapeTopic(topic));
        assertEquals(topic, TopicUtils.unescapeTopic(TopicUtils.escapeTopic(topic)));

        // Test topics with UTF-32 characters
        topic = "a/𐍈/c";
        assertEquals("a/$d800$df48/c", TopicUtils.escapeTopic(topic));
        assertEquals(topic, TopicUtils.unescapeTopic(TopicUtils.escapeTopic(topic)));

        // Test URL encoding
        topic = "http://example.com/resource";
        assertEquals("http$003a//example$002ecom/resource", TopicUtils.escapeTopic(topic));
        assertEquals(topic, TopicUtils.unescapeTopic(TopicUtils.escapeTopic(topic)));
    }

    @Test
    void testEscapeTopicFilter() {
        // Test that normal topics are not changed
        String topicFilter = "a/b/c";
        assertEquals(topicFilter, TopicUtils.escapeTopicFilter(topicFilter));
        assertEquals(topicFilter, TopicUtils.unescapeTopicFilter(topicFilter));

        // Test '-' and wildcard characters
        topicFilter = "a/b-c/+/*";
        assertEquals(topicFilter, TopicUtils.escapeTopicFilter(topicFilter));
        assertEquals(topicFilter, TopicUtils.unescapeTopicFilter(TopicUtils.escapeTopicFilter(topicFilter)));

        // ... even if the topic is invalid
        topicFilter = "a/b-c/d+e/f*g";
        assertEquals(topicFilter, TopicUtils.escapeTopicFilter(topicFilter));
        assertEquals(topicFilter, TopicUtils.unescapeTopicFilter(TopicUtils.escapeTopicFilter(topicFilter)));

        // Test that topics with special characters are escaped
        topicFilter = "a/$b/c/d~e";
        assertEquals("a/$0024b/c/d$007ee", TopicUtils.escapeTopicFilter(topicFilter));
        assertEquals(topicFilter, TopicUtils.unescapeTopicFilter(TopicUtils.escapeTopicFilter(topicFilter)));

        // Test topics with UTF-32 characters
        topicFilter = "a/𐍈/c";
        assertEquals("a/$d800$df48/c", TopicUtils.escapeTopicFilter(topicFilter));
        assertEquals(topicFilter, TopicUtils.unescapeTopicFilter(TopicUtils.escapeTopicFilter(topicFilter)));

        // Test URL encoding
        topicFilter = "http://example.com/resource";
        assertEquals("http$003a//example$002ecom/resource", TopicUtils.escapeTopicFilter(topicFilter));
        assertEquals(topicFilter, TopicUtils.unescapeTopicFilter(TopicUtils.escapeTopicFilter(topicFilter)));
    }

    @Test
    void testNestedEscapeTopic() {
        String topic = "a/𐍈/+/c";
        String escapedTopic = TopicUtils.escapeTopic(topic);
        assertEquals("a/$d800$df48/$002b/c", escapedTopic);
        String nestedEscapedTopic = TopicUtils.escapeTopic(escapedTopic);
        assertEquals("a/$0024d800$0024df48/$0024002b/c", nestedEscapedTopic);
        assertEquals(escapedTopic, TopicUtils.unescapeTopic(nestedEscapedTopic));
        assertEquals(topic, TopicUtils.unescapeTopic(escapedTopic));
    }

    @Test
    void testNestedEscapeTopicFilter() {
        String topicFilter = "a/𐍈/+/c";
        String escapedTopicFilter = TopicUtils.escapeTopicFilter(topicFilter);
        assertEquals("a/$d800$df48/+/c", escapedTopicFilter);
        String nestedEscapedTopicFilter = TopicUtils.escapeTopicFilter(escapedTopicFilter);
        assertEquals("a/$0024d800$0024df48/+/c", nestedEscapedTopicFilter);
        assertEquals(escapedTopicFilter, TopicUtils.unescapeTopicFilter(nestedEscapedTopicFilter));
        assertEquals(topicFilter, TopicUtils.unescapeTopicFilter(escapedTopicFilter));
    }
}
