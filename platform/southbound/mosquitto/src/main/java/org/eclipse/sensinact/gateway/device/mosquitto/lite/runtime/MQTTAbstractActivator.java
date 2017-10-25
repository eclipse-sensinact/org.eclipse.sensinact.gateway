package org.eclipse.sensinact.gateway.device.mosquitto.lite.runtime;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.Activator;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.client.MQTTClient;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.client.MQTTConnectionHandler;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.client.ServerConnectionCache;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.client.subscriber.MQTTTopicMessage;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.device.exception.MQTTConnectionException;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.mqtt.MQTTBroker;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.runtime.exception.MQTTManagerException;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class MQTTAbstractActivator<M extends Mediator> extends AbstractActivator implements MQTTConnectionHandler{
    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);
    //In charge of MQTT basic connection and operation
    private MQTTClient mqttClient;
    //Uses the StackConnector of MQTT-SB to publish messages
    private MQTTManagerRuntime runtime;
    //providers created by the instance that inherited from this class
    private Set<String> providers=new HashSet<String>();
    protected final MQTTBroker broker;

    public MQTTAbstractActivator() {
        broker=getBroker();
        mqttClient=ServerConnectionCache.getInstance(getId(), broker, this);
        try {
            runtime=MQTTManagerRuntime.getInstance();
        } catch (MQTTManagerException e) {
            LOG.error("Failed to connect to broker {}",e,broker.toString());
        }
    }

    @Override
    public Mediator doInstantiate(BundleContext context)
    {
        return new Mediator(context);
    }

    /**
     * This subscribes to a topic on the broker configured by the inherited class
     * @param topic
     * @param message
     */
    protected void subscribe(String topic,MQTTTopicMessage message){

        try {
            mqttClient.getConnection().subscribe(topic, message);
        } catch (Exception e) {
            LOG.error("Failed to start connection",e);
        }
    }

    /**
     * Method called by child instances to notify the arrival of a new provider/service/resource/value
     * @param provider
     * @param service
     * @param resource
     * @param data
     */
    public void data(String provider,String service,String resource,String data){
        providers.add(provider);
        runtime.messageReceived(provider,service,resource,data);
    }

    /**
     * This represents the connection id in the pool of connection to the broker, this id is used to close the connection
     * @return
     */
    private String getId() {
        return this.getClass().getCanonicalName();
    }

    /***
     * This implementation will remove all providers created by the instance inherited from this class.
     * The MQTT connection created will be closed as well.
     * @throws Exception
     */
    @Override
    public void doStop() throws Exception {

        for(String provider:providers){
            try {
                runtime.processRemoval(provider);
            }catch(Exception e){
                LOG.warn("Failed to remove provider {}.",provider,e);
            }
        }

        try {
            ServerConnectionCache.disconnectInstance(getId());
        }catch(Exception e){
            LOG.warn("Failed closing mqtt connection with id {}.",getId(),e);
        }

    }

    /**
     * This is the pojo that contains the information about the broker address and connection characteristics.
     * @return the object that specifies that information about the broker that we will be connected to
     */
    public abstract MQTTBroker getBroker();

}
