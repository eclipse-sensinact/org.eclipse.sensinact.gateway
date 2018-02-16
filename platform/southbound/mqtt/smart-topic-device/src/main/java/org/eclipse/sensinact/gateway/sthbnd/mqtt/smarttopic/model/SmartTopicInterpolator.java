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
package org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.model;


import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.exception.MessageInvalidSmartTopicException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmartTopicInterpolator {

    private final String PROVIDER_TAG="{provider}";
    private final String SERVICE_TAG="{service}";
    private final String RESOURCE_TAG="{resource}";
    private final String VALUE_TAG="{value}";
    private final String MQTT_TAG="+";
    private String smartTopic="";

    private Pattern pattern;

    public SmartTopicInterpolator(String smartTopic){
        this.smartTopic=smartTopic;
    }

    public String getTopic(){

        StringBuffer buffer=new StringBuffer();

        Integer counter=0;
        for(String partTopic:smartTopic.split("/")){
            if(counter!=0) buffer.append("/");
            if(partTopic.contains(PROVIDER_TAG)||partTopic.contains(SERVICE_TAG)||partTopic.contains(RESOURCE_TAG)||partTopic.contains(VALUE_TAG)){
                buffer.append("+");
            }else {
                buffer.append(partTopic);
            }
            counter++;
        }

        return buffer.toString();
    }

    public String getRegex(){

        StringBuffer buffer=new StringBuffer();
        String[] partTopics=smartTopic.split("/");

        Integer counter=0;
        for(String partTopic:partTopics){

            if(counter!=0) buffer.append("/");

            if(partTopic.contains(PROVIDER_TAG)){
                buffer.append(partTopic.replace(PROVIDER_TAG,"(?<provider>.*)"));
            }else if(partTopic.contains(SERVICE_TAG)){
                buffer.append(partTopic.replace(SERVICE_TAG,"(?<service>.*)"));
            }else if(partTopic.contains(RESOURCE_TAG)){
                buffer.append(partTopic.replace(RESOURCE_TAG,"(?<resource>.*)"));
            }else if(partTopic.contains("{value}")){
                buffer.append(partTopic.replace(VALUE_TAG,"(?<value>.*)"));
            }else if(partTopic.equals(MQTT_TAG)){
                buffer.append(".*");
            }else {
                buffer.append(partTopic);
            }

            counter++;

        }

        return buffer.toString();
    }

    public String getGroup(String message,String groupName) throws MessageInvalidSmartTopicException {
        if(pattern==null){
            pattern=Pattern.compile(getRegex());
        }
        Matcher matcher=pattern.matcher(message);
        if(!matcher.matches()){
            throw new MessageInvalidSmartTopicException("Message value does not match the smartTopic");
        }
        try {
            return matcher.group(groupName);
        }catch(Exception e){
            throw new MessageInvalidSmartTopicException(e);
        }

    }

    public String getSmartTopic() {
        return smartTopic;
    }

    public static void main(String[] args) throws MessageInvalidSmartTopicException {


        String smartTopic="/+/blah/aaa-{provider}/{service}/{resource}";

        SmartTopicInterpolator m=new SmartTopicInterpolator(smartTopic);

        System.out.println("SmartTopic:"+smartTopic);
        System.out.println("Regex:"+m.getRegex());
        System.out.println("Topic:"+m.getTopic());

        System.out.println(m.getGroup("/me/blah/aaa-pi/air/co2", "provider"));

        /*
        Pattern p=Pattern.compile("/blah/(?<provider>.*)/(?<service>.*)/(?<resource>.*)");

        Matcher matcher=p.matcher("/blah/pi/air/co2");

        System.out.println(matcher.matches());
        System.out.println(matcher.group("service"));
*/

    }
}
