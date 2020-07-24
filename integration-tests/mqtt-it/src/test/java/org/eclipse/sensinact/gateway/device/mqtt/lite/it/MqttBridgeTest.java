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

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.core.method.legacy.SubscribeResponse;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.device.mqtt.lite.it.util.MqttTestITAbstract;
import org.eclipse.sensinact.gateway.util.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.BundleContext;
import org.sensinact.mqtt.server.MQTTServerService;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class MqttBridgeTest extends MqttTestITAbstract {

    @Inject
    BundleContext bc;

    @Inject
    MQTTServerService mqttServerService;

    private Core sensinactCore;
    
    private Session sensinactSession;

    @Before
    public void before() throws Exception {  
        mqttServerService.startService(MQTT_HOST,MQTT_PORT.toString());
    	try {
        	String fileName = "sensinact.config";
            File testFile = new File(new File("src/test/resources"), fileName);
            URL testFileURL = testFile.toURI().toURL();
            FileOutputStream output = new FileOutputStream(new File(new File("target/conf"),fileName));
            byte[] testCng = IOUtils.read(testFileURL.openStream(), true);
            IOUtils.write(testCng, output);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int n=0;
    	while(true) {
	    	try {
	    		Thread.sleep(150);
	    		n+=150;
			} catch (InterruptedException e) {
				Thread.interrupted();
				break;
			}
	    	if(!bc.getServiceReferences(Core.class,null).isEmpty()) {
	    		break;
	    	}
	    	if(n>=60000) {
	    		break;
	    	}
    	}
	    sensinactCore = bc.getService(bc.getServiceReference(Core.class));
	    sensinactSession=sensinactCore.getAnonymousSession();
    }

    @After
    public void after() throws Exception{
    	try {
            mqttServerService.stopService(String.format("%s:%s",MQTT_HOST,MQTT_PORT.toString()));
            sensinactCore.close();    
            new File(new File("target/conf"),"sensinact.config").delete();
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }

    @Test
    public void providerCreation() throws Exception {        
        createConfigurationFile("myprovider","/myresource");
        Thread.yield();
        JSONObject obj = new JSONObject(sensinactSession.getProviders().getJSON());
        JSONArray providers = obj.getJSONArray("providers");
        System.out.println("-->"+providers.toString());
        final Set<String> providersSet = parseJSONArrayIntoSet(providers);
        Assert.assertTrue("Provider was not created, or at least is not shown via REST api", providersSet.contains("myprovider"));
    	
    }
    
    @Test
    public void providerCreationViaRest() throws Exception {        
        providerCreation();
        MqttClient mqttClient = getMqttConnection(MQTT_HOST, MQTT_PORT);
        final String messageString1 = new Double(Math.random()).toString();
        MqttMessage message1 = new MqttMessage(messageString1.getBytes());
        message1.setQos(1);
        mqttClient.publish("/myresource",message1 );
        final Integer maxRetries=3;
        Integer currentRetry=0;
        Integer statusCode=null;
        JSONObject jsonResponse=null;
        while(currentRetry<maxRetries&&(statusCode==null||statusCode>299||statusCode<200)){
            System.out.println("Sending request ..."+currentRetry);
            jsonResponse=invokeRestAPI("sensinact/providers/myprovider/services/info/resources/value/GET");
            statusCode=jsonResponse.getInt("statusCode");
            currentRetry++;
            Thread.sleep(500);
        }
        String value =jsonResponse.getJSONObject("response").getString("value");
        Assert.assertEquals("Value should be updated on new message arrival, and was not the case", messageString1,value);
    }
    
    @Test
    public void providerRemoval() throws Exception {    	
    	providerCreation();
        deleteConfigurationFile();
        JSONObject obj = new JSONObject(sensinactSession.getProviders().getJSON());
        JSONArray providers = obj.getJSONArray("providers");
        final Set<String> providersSetNo = parseJSONArrayIntoSet(providers);
        Assert.assertTrue("Provider was removed",!providersSetNo.contains("myprovider"));
    }
    
    @Test
    public void serviceCreation() throws Exception {    	
        providerCreation();
        JSONObject obj = new JSONObject(sensinactSession.getServices("myprovider").getJSON());
        JSONArray services = obj.getJSONArray("services");
        System.out.println("-->"+services.toString());
        final Set<String> servicesSet = parseJSONArrayIntoSet(services);
        Assert.assertTrue("Service was not created, or at least is not shown via REST api",servicesSet.contains("info"));
    }
    
    @Test
    public void resourceCreation() throws Exception {    	
        serviceCreation();
        Thread.yield();
        JSONObject obj = new JSONObject(sensinactSession.getResources("myprovider", "info").getJSON());
        JSONArray resources = obj.getJSONArray("resources");
        final Set<String> resourcesSet = parseJSONArrayIntoSet(resources);
        Assert.assertTrue("Resource was not created, or at least is not shown via REST api", resourcesSet.contains("value"));
    }
    
    @Test
    public void resourceValueQuery() throws Exception {  
        resourceCreation();
        Object value = sensinactSession.get("myprovider", "info", "value", "value").getResponse("value");
        Assert.assertEquals("Initial Resource value should be empty ",JSONObject.NULL, value);
    }
    
    @Test
    public void resourceValueQueryViaRest() throws Exception {
        resourceValueQuery();
        final RecipientCustom rtc = new RecipientCustom();
        sensinactSession.subscribe("myprovider", "info", "value", rtc, new JSONArray());
        MqttClient mqttClient = getMqttConnection(MQTT_HOST, MQTT_PORT);
        final String messageString1 = new Double(Math.random()).toString();
        MqttMessage message1 = new MqttMessage(messageString1.getBytes());
        message1.setQos(0);
        mqttClient.publish("/myresource",message1 );
        waitForCallbackNotification();
        String value = invokeRestAPI("sensinact/providers/myprovider/services/info/resources/value/GET")
                .getJSONObject("response").getString("value");
        Assert.assertEquals("Value should be updated on new message arrival, and was not the case", messageString1,value);
        final String messageString2=new Double(Math.random()).toString();
        MqttMessage message2=new MqttMessage(messageString2.getBytes());
        message2.setQos(0);
        mqttClient.publish("/myresource",message2 );
        waitForCallbackNotification();
        Thread.yield();
        String value2=sensinactSession.get("myprovider", "info", "value", "value").getResponse(String.class,"value");
        Assert.assertEquals("Value should be updated on new message arrival, and was not the case", messageString2,value2);
  
    }
    
    @Test
    public void resourceValueQueryViaSubscription() throws Exception {
        resourceValueQuery();
        final RecipientCustom rtc=new RecipientCustom();
        SubscribeResponse r = sensinactSession.subscribe("myprovider", "info", "value", rtc, new JSONArray());
        System.out.println(r.getJSON());
        MqttClient mqttClient = getMqttConnection(MQTT_HOST, MQTT_PORT);
        final String messageString1 = new Double(Math.random()).toString();
        MqttMessage message1 = new MqttMessage(messageString1.getBytes());
        message1.setQos(0);
        mqttClient.publish("/myresource", message1 );
        waitForCallbackNotification();
        Assert.assertEquals("Sensinact Core did not dispatch any notification message for the subscription", 1,rtc.getMessages().length);
        Assert.assertEquals("The notification value does not correspond to the value sent", messageString1,new JSONObject(rtc.getMessages()[0].getJSON()).getJSONObject("notification").getString("value"));
        final String messageString2=new Double(Math.random()).toString();
        MqttMessage message2=new MqttMessage(messageString2.getBytes());
        message2.setQos(0);
        mqttClient.publish("/myresource",message2 );
        waitForCallbackNotification();
        Assert.assertEquals("Sensinact Core did not dispatch any notification message for the subscription", 1,rtc.getMessages().length);
        Assert.assertEquals("The notification value does not correspond to the value sent", messageString2,new JSONObject(rtc.getMessages()[0].getJSON()).getJSONObject("notification").getString("value"));
    }

    private void waitForCallbackNotification() throws InterruptedException {
    	Thread.dumpStack();
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