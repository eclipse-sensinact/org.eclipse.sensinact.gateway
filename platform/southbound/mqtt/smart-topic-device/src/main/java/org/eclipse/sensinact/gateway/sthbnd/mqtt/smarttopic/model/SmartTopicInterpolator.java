/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.model;

import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.exception.MessageInvalidSmartTopicException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmartTopicInterpolator {
    private final String PROVIDER_TAG = "{provider}";
    private final String SERVICE_TAG = "{service}";
    private final String RESOURCE_TAG = "{resource}";
    private final String VALUE_TAG = "{value}";
    private final String MQTT_TAG = "+";
    private String smartTopic = "";
    private Pattern pattern;

    public SmartTopicInterpolator(String smartTopic) {
        this.smartTopic = smartTopic;
    }

    public String getTopic() {
        StringBuffer buffer = new StringBuffer();
        Integer counter = 0;
        for (String partTopic : smartTopic.split("/")) {
            if (counter != 0) buffer.append("/");
            if (partTopic.contains(PROVIDER_TAG) || partTopic.contains(SERVICE_TAG) || partTopic.contains(RESOURCE_TAG) || partTopic.contains(VALUE_TAG)) {
                buffer.append("+");
            } else {
                buffer.append(partTopic);
            }
            counter++;
        }
        return buffer.toString();
    }

    public String getRegex() {
        StringBuffer buffer = new StringBuffer();
        String[] partTopics = smartTopic.split("/");
        Integer counter = 0;
        for (String partTopic : partTopics) {
            if (counter != 0) buffer.append("/");
            if (partTopic.contains(PROVIDER_TAG)) {
                buffer.append(partTopic.replace(PROVIDER_TAG, "(?<provider>.*)"));
            } else if (partTopic.contains(SERVICE_TAG)) {
                buffer.append(partTopic.replace(SERVICE_TAG, "(?<service>.*)"));
            } else if (partTopic.contains(RESOURCE_TAG)) {
                buffer.append(partTopic.replace(RESOURCE_TAG, "(?<resource>.*)"));
            } else if (partTopic.contains("{value}")) {
                buffer.append(partTopic.replace(VALUE_TAG, "(?<value>.*)"));
            } else if (partTopic.equals(MQTT_TAG)) {
                buffer.append(".*");
            } else {
                buffer.append(partTopic);
            }
            counter++;
        }
        return buffer.toString();
    }

    public String getGroup(String message, String groupName) throws MessageInvalidSmartTopicException {
        if (pattern == null) {
            pattern = Pattern.compile(getRegex());
        }
        Matcher matcher = pattern.matcher(message);
        if (!matcher.matches()) {
            throw new MessageInvalidSmartTopicException("Message value does not match the smartTopic");
        }
        try {
            return matcher.group(groupName);
        } catch (Exception e) {
            throw new MessageInvalidSmartTopicException(e);
        }
    }

    public String getSmartTopic() {
        return smartTopic;
    }
}
