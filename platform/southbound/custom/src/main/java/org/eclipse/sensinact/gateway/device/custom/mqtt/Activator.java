package org.eclipse.sensinact.gateway.device.custom.mqtt;

import org.eclipse.sensinact.gateway.common.annotation.Property;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.client.MQTTClient;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.client.MQTTConnection;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.client.subscriber.MQTTTopicMessage;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.mqtt.MQTTAuth;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.mqtt.MQTTBroker;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.mqtt.MQTTSession;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.runtime.MQTTAbstractActivator;

public class Activator extends MQTTAbstractActivator {

    @Property
    public String name;

    @Override
    public MQTTBroker getBroker(){

        /**
         * Configure the broker address
         */
        MQTTBroker b=new MQTTBroker();
        //b.setHost("localhost");
        //b.setPort(1883l);
        b.setHost("eu.thethings.network");
        b.setPort(1883l);
        MQTTSession se=new MQTTSession();
        MQTTAuth ma=new MQTTAuth();
        ma.setUsername("cea-wise-iot");
        ma.setPassword("ttn-account-v2.7rjnw_oXCLCJWSIGBKAJtVg_Q1LyMOOqNQPjUvh7IM4");
        b.setAuth(ma);

        return b;
    }



    @Override
    public void doStart() throws Exception {
        System.out.println("name:"+name);
        subscribe("+/devices/+/events/activations", new MQTTTopicMessage() {
            @Override
            public void messageReceived(MQTTConnection connection, String topic, String message) {
                System.out.println("GOT ME");
                Activator.this.data("jander", "me", "blop", message);
            }
        });
        /*
        subscribe("/jander/him", new MQTTTopicMessage() {
            @Override
            public void messageReceived(MQTTConnection connection, String topic, String message) {
                System.out.println("GOT HIM");
                Activator.this.data("jander", "him", "blop", message);
            }
        });
        subscribe("/jander/+", new MQTTTopicMessage() {
            @Override
            public void messageReceived(MQTTConnection connection, String topic, String message) {
                //Activator.this.providerRemoval("jander");
                System.out.println("OFF");
            }
        });
        */
    }

    @Override
    public void connectionFailed(MQTTClient connectionId) {
        System.out.println("Connection failed.");
    }

    @Override
    public void connectionEstablished(MQTTClient connectionId) {
        System.out.println("Connection established.");
    }

    @Override
    public void connectionLost(MQTTClient connectionId) {
        System.out.println("Connection lost.");
    }
}
