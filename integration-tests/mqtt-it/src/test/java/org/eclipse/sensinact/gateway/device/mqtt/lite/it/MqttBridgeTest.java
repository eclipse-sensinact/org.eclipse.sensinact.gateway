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
package org.eclipse.sensinact.gateway.device.mqtt.lite.it;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Set;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.core.method.SubscribeResponse;
import org.eclipse.sensinact.gateway.device.mqtt.lite.it.util.MqttTestITAbstract;
import org.eclipse.sensinact.gateway.util.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.sensinact.mqtt.server.MQTTServerService;

@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class MqttBridgeTest extends MqttTestITAbstract {

//    @InjectBundleContext
//    BundleContext bc;
//
//    @InjectService
//    MQTTServerService mqttServerService;
//
//    private Core sensinactCore;
//    
//    private Session sensinactSession;
//
//    @BeforeEach
//    public void before() throws Exception {  
//        mqttServerService.startService(MQTT_HOST,MQTT_PORT.toString());
//    	try {
//        	String fileName = "sensinact.config";
//            File testFile = new File(new File("src/test/resources"), fileName);
//            URL testFileURL = testFile.toURI().toURL();
//            FileOutputStream output = new FileOutputStream(new File(new File("target/conf"),fileName));
//            byte[] testCng = IOUtils.read(testFileURL.openStream(), true);
//            IOUtils.write(testCng, output);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        int n=0;
//    	while(true) {
//	    	try {
//	    		Thread.sleep(150);
//	    		n+=150;
//			} catch (InterruptedException e) {
//				Thread.interrupted();
//				break;
//			}
//	    	if(!bc.getServiceReferences(Core.class,null).isEmpty()) {
//	    		break;
//	    	}
//	    	if(n>=60000) {
//	    		break;
//	    	}
//    	}
//	    sensinactCore = bc.getService(bc.getServiceReference(Core.class));
//	    sensinactSession=sensinactCore.getAnonymousSession();
//    }
//
//    @AfterEach
//    public void after() throws Exception{
//    	try {
//            mqttServerService.stopService(String.format("%s:%s",MQTT_HOST,MQTT_PORT.toString()));
//            sensinactCore.close();    
//            new File(new File("target/conf"),"sensinact.config").delete();
//    	} catch(Exception e) {
//    		e.printStackTrace();
//    	}
//    }
//
//    @Test
//    public void providerCreation() throws Exception {        
//        createConfigurationFile("myprovider","/myresource");
//        Thread.yield();
//        JSONObject obj = new JSONObject(sensinactSession.getProviders().getJSON());
//        JSONArray providers = obj.getJSONArray("providers");
//        System.out.println("-->"+providers.toString());
//        final Set<String> providersSet = parseJSONArrayIntoSet(providers);
//        Assertions.assertTrue(providersSet.contains("myprovider"),"Provider was not created, or at least is not shown via REST api");
//    	
//    }
//    
//    @Test
//    public void providerCreationViaRest() throws Exception {        
//        providerCreation();
//        MqttClient mqttClient = getMqttConnection(MQTT_HOST, MQTT_PORT);
//        final String messageString1 = new Double(Math.random()).toString();
//        MqttMessage message1 = new MqttMessage(messageString1.getBytes());
//        message1.setQos(1);
//        mqttClient.publish("/myresource",message1 );
//        final Integer maxRetries=3;
//        Integer currentRetry=0;
//        Integer statusCode=null;
//        JSONObject jsonResponse=null;
//        while(currentRetry<maxRetries&&(statusCode==null||statusCode>299||statusCode<200)){
//            System.out.println("Sending request ..."+currentRetry);
//            jsonResponse=invokeRestAPI("sensinact/providers/myprovider/services/info/resources/value/GET");
//            statusCode=jsonResponse.getInt("statusCode");
//            currentRetry++;
//            Thread.sleep(500);
//        }
//        String value =jsonResponse.getJSONObject("response").getString("value");
//        Assertions.assertEquals("Value should be updated on new message arrival, and was not the case", messageString1,value);
//    }
//    
//    @Test
//    public void providerRemoval() throws Exception {    	
//    	providerCreation();
//        deleteConfigurationFile();
//        JSONObject obj = new JSONObject(sensinactSession.getProviders().getJSON());
//        JSONArray providers = obj.getJSONArray("providers");
//        final Set<String> providersSetNo = parseJSONArrayIntoSet(providers);
//        Assertions.assertTrue(!providersSetNo.contains("myprovider"),"Provider was removed");
//    }
//    
//    @Test
//    public void serviceCreation() throws Exception {    	
//        providerCreation();
//        JSONObject obj = new JSONObject(sensinactSession.getServices("myprovider").getJSON());
//        JSONArray services = obj.getJSONArray("services");
//        System.out.println("-->"+services.toString());
//        final Set<String> servicesSet = parseJSONArrayIntoSet(services);
//        Assertions.assertTrue(servicesSet.contains("info"),"Service was not created, or at least is not shown via REST api");
//    }
//    
//    @Test
//    public void resourceCreation() throws Exception {    	
//        serviceCreation();
//        Thread.yield();
//        JSONObject obj = new JSONObject(sensinactSession.getResources("myprovider", "info").getJSON());
//        JSONArray resources = obj.getJSONArray("resources");
//        final Set<String> resourcesSet = parseJSONArrayIntoSet(resources);
//        Assertions.assertTrue( resourcesSet.contains("value"),"Resource was not created, or at least is not shown via REST api");
//    }
//    
//    @Test
//    public void resourceValueQuery() throws Exception {  
//        resourceCreation();
//        Object value = sensinactSession.get("myprovider", "info", "value", "value").getResponse("value");
//        Assertions.assertEquals(JSONObject.NULL, value,"Initial Resource value should be empty ");
//    }
//    
//    @Test
//    public void resourceValueQueryViaRest() throws Exception {
//        resourceValueQuery();
//        final RecipientCustom rtc = new RecipientCustom();
//        sensinactSession.subscribe("myprovider", "info", "value", rtc, new JSONArray());
//        MqttClient mqttClient = getMqttConnection(MQTT_HOST, MQTT_PORT);
//        final String messageString1 = new Double(Math.random()).toString();
//        MqttMessage message1 = new MqttMessage(messageString1.getBytes());
//        message1.setQos(0);
//        mqttClient.publish("/myresource",message1 );
//        waitForCallbackNotification();
//        String value = invokeRestAPI("sensinact/providers/myprovider/services/info/resources/value/GET")
//                .getJSONObject("response").getString("value");
//        Assertions.assertEquals( messageString1,value,"Value should be updated on new message arrival, and was not the case");
//        final String messageString2=new Double(Math.random()).toString();
//        MqttMessage message2=new MqttMessage(messageString2.getBytes());
//        message2.setQos(0);
//        mqttClient.publish("/myresource",message2 );
//        waitForCallbackNotification();
//        Thread.yield();
//        String value2=sensinactSession.get("myprovider", "info", "value", "value").getResponse(String.class,"value");
//        Assertions.assertEquals( messageString2,value2,"Value should be updated on new message arrival, and was not the case");
//  
//    }
//    
//    @Test
//    public void resourceValueQueryViaSubscription() throws Exception {
//        resourceValueQuery();
//        final RecipientCustom rtc=new RecipientCustom();
//        SubscribeResponse r = sensinactSession.subscribe("myprovider", "info", "value", rtc, new JSONArray());
//        System.out.println(r.getJSON());
//        MqttClient mqttClient = getMqttConnection(MQTT_HOST, MQTT_PORT);
//        final String messageString1 = new Double(Math.random()).toString();
//        MqttMessage message1 = new MqttMessage(messageString1.getBytes());
//        message1.setQos(0);
//        mqttClient.publish("/myresource", message1 );
//        waitForCallbackNotification();
//        Assertions.assertEquals( 1,rtc.getMessages().length,"Sensinact Core did not dispatch any notification message for the subscription");
//        Assertions.assertEquals( messageString1,new JSONObject(rtc.getMessages()[0].getJSON()).getJSONObject("notification").getString("value"),"The notification value does not correspond to the value sent");
//        final String messageString2=new Double(Math.random()).toString();
//        MqttMessage message2=new MqttMessage(messageString2.getBytes());
//        message2.setQos(0);
//        mqttClient.publish("/myresource",message2 );
//        waitForCallbackNotification();
//        Assertions.assertEquals( 1,rtc.getMessages().length,"Sensinact Core did not dispatch any notification message for the subscription");
//        Assertions.assertEquals( messageString2,new JSONObject(rtc.getMessages()[0].getJSON()).getJSONObject("notification").getString("value"),"The notification value does not correspond to the value sent");
//    }
//
//    private void waitForCallbackNotification() throws InterruptedException {
//    	Thread.dumpStack();
//        synchronized (this){
//            wait();
//        }
//    }
//    
//    class RecipientCustom implements Recipient {
//        private SnaMessage[] messages;
//        @Override
//        public void callback(String callbackId, SnaMessage[] messages) throws Exception {
//            this.messages=messages;
//            for(SnaMessage sna:messages){
//                System.out.println("Message content received in callback:"+sna.getJSON());
//            }
//            synchronized (MqttBridgeTest.this){
//                MqttBridgeTest.this.notify();
//            }
//        }
//        
//        public String getJSON() {
//            return "{}";
//        }
//        public SnaMessage[] getMessages() {
//            return messages;
//        }
//    }
}