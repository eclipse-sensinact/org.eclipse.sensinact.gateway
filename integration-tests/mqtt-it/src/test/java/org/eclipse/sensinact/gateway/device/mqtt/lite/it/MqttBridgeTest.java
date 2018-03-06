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
package org.eclipse.sensinact.gateway.device.mqtt.lite.it;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.device.mqtt.lite.it.util.MqttTestITAbstract;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.model.SmartTopicPacket;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import java.util.Hashtable;
import java.util.Set;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
@Ignore
public class MqttBridgeTest extends MqttTestITAbstract {

    @Inject
    BundleContext bc;

    @Inject
    Core sensinactCore;

    private Session sensinactSession;

    @Before
    public void prepare(){
        sensinactSession = sensinactCore.getAnonymousSession();
    }

    @Test
    public void providerCreation() throws Exception {
        Object provider = createDevicePojo("myprovider","myservice","myresource","/myresource");
        bc.registerService("org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.model.Provider", provider, new Hashtable<String, Object>());
        JSONObject obj = new JSONObject(sensinactSession.getProviders());
        JSONArray providers = obj.getJSONArray("providers");
        final Set<String> providersSet = parseJSONArrayIntoSet(providers);
        Assert.assertTrue("Provider was not created, or at least is not shown via REST api", providersSet.contains("myprovider"));
    }

    @Test
    public void providerCreationViaRest() throws Exception {
        providerCreation();
        MqttClient mqttClient = getMqttConnection(MQTT_HOST, MQTT_PORT);
        final String messageString1 = new Double(Math.random()).toString();
        MqttMessage message1 = new MqttMessage(messageString1.getBytes());
        mqttClient.publish("/myresource",message1 );

        final Integer maxRetries=3;
        Integer currentRetry=0;
        Integer statusCode=null;
        JSONObject jsonResponse=null;
        while(currentRetry<maxRetries&&(statusCode==null||statusCode>299||statusCode<200)){
            System.out.println("Sending request ..."+currentRetry);
            jsonResponse=invokeRestAPI("sensinact/providers/myprovider/services/myservice/resources/myresource/GET");
            statusCode=jsonResponse.getInt("statusCode");
            currentRetry++;
            Thread.sleep(500);
        }

        String value =jsonResponse.getJSONObject("response").getString("value");
        Assert.assertEquals("Value should be updated on new message arrival, and was not the case", messageString1,value);

    }

    @Test
    @Ignore
    public void providerRemoval() throws Exception {
        providerCreation();
        SmartTopicPacket packet = new SmartTopicPacket("myprovider");
        packet.setGoodbyeMessage(true);
        JSONObject obj = new JSONObject(sensinactSession.getProviders());
        JSONArray providers = obj.getJSONArray("providers");
        final Set<String> providersSetNo = parseJSONArrayIntoSet(providers);
        Assert.assertTrue("Provider was removed",!providersSetNo.contains("myprovider"));
    }

    @Test
    public void serviceCreation() throws Exception {
        providerCreation();

        JSONObject obj = new JSONObject(sensinactSession.getServices("myprovider"));
        JSONArray services = obj.getJSONArray("services");        
        final Set<String> servicesSet = parseJSONArrayIntoSet(services);
        Assert.assertTrue("Service was not created, or at least is not shown via REST api",servicesSet.contains("myservice"));
    }

    @Test
    public void resourceCreation() throws Exception {
        serviceCreation();
        JSONObject obj = new JSONObject(sensinactSession.getResources("myprovider", "myservice"));
        JSONArray resources = obj.getJSONArray("resources");
        final Set<String> resourcesSet = parseJSONArrayIntoSet(resources);
        Assert.assertTrue("Resource was not created, or at least is not shown via REST api", resourcesSet.contains("myresource"));
    }

    @Test
    public void resourceValueQuery() throws Exception {
        resourceCreation();
        String value=sensinactSession.get("myprovider", "myservice", "myresource", "value"
        		).getResult().getJSONObject("response").getString("value");
        Assert.assertTrue("Initial Resource value should be empty ", value.equals(""));
    }

    @Test
    public void resourceValueQueryViaRest() throws Exception {
        resourceValueQuery();
        MqttClient mqttClient = getMqttConnection(MQTT_HOST, MQTT_PORT);
        final String messageString1 = new Double(Math.random()).toString();
        MqttMessage message1 = new MqttMessage(messageString1.getBytes());
        mqttClient.publish("/myresource",message1 );
        final RecipientCustom rtc = new RecipientCustom();
        sensinactSession.subscribe("myprovider", "myservice", "myresource", rtc, new JSONArray());
        waitForCallbackNotification();
        String value = invokeRestAPI("sensinact/providers/myprovider/services/myservice/resources/myresource/GET")
                .getJSONObject("response").getString("value");
        Assert.assertEquals("Value should be updated on new message arrival, and was not the case", messageString1,value);

        final String messageString2=new Double(Math.random()).toString();
        MqttMessage message2=new MqttMessage(messageString2.getBytes());
        mqttClient.publish("/myresource",message2 );
        waitForCallbackNotification();
        String value2=sensinactSession.get("myprovider", "myservice", "myresource", "value").getResult(
        		).getJSONObject("response").getString("value");
        Assert.assertEquals("Value should be updated on new message arrival, and was not the case", messageString2,value2);
    }

    @Test(timeout = 10000)
    public void resourceValueQueryViaSubscription() throws Exception {

        resourceValueQuery();
        final RecipientCustom rtc=new RecipientCustom();
        sensinactSession.subscribe("myprovider", "myservice", "myresource", rtc, new JSONArray());
        MqttClient mqttClient=getMqttConnection(MQTT_HOST, MQTT_PORT);

        final String messageString1=new Double(Math.random()).toString();
        MqttMessage message1=new MqttMessage(messageString1.getBytes());
        mqttClient.publish("/myresource",message1 );
        waitForCallbackNotification();
        Assert.assertEquals("Sensinact Core did not dispatch any notification message for the subscription", 1,rtc.getMessages().length);
        Assert.assertEquals("The notification value does not correspond to the value sent", messageString1,new JSONObject(rtc.getMessages()[0].getJSON()).getJSONObject("notification").getString("value"));

        final String messageString2=new Double(Math.random()).toString();
        MqttMessage message2=new MqttMessage(messageString2.getBytes());
        mqttClient.publish("/myresource",message2 );
        waitForCallbackNotification();

        Assert.assertEquals("Sensinact Core did not dispatch any notification message for the subscription", 1,rtc.getMessages().length);
        Assert.assertEquals("The notification value does not correspond to the value sent", messageString2,new JSONObject(rtc.getMessages()[0].getJSON()).getJSONObject("notification").getString("value"));
    }

    private void waitForCallbackNotification() throws InterruptedException {
        synchronized (this){
            wait();
        }
    }

    class RecipientCustom implements Recipient {

        private SnaMessage[] messages;

        @Override
        public void callback(String callbackId, SnaMessage[] messages) throws Exception {
            this.messages=messages;
            for(SnaMessage sna:messages){
                System.out.println("Message content received in callback:"+sna.getJSON());
            }
            synchronized (MqttBridgeTest.this){
                MqttBridgeTest.this.notify();
            }
        }

        @Override
        public String getJSON() {
            return "{}";
        }

        public SnaMessage[] getMessages() {
            return messages;
        }
    }

}