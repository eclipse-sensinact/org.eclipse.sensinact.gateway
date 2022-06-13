package org.eclipse.sensinact.gateway.core.remote;

import java.io.IOException;
import java.io.StringReader;
import java.util.Dictionary;
import java.util.Set;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.message.AbstractMidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.MidCallbackException;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.core.message.SnaUpdateMessageImpl;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.util.api.MqttBroker;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.json.JsonValue;

public class SensinactCoreBase implements SensinactCoreBaseIface {

    private static final Logger LOG= LoggerFactory.getLogger(SensinactCoreBase.class);

    private ConfigurationAdmin ca;
    private Core sensinact;
	private Mediator mediator;
    private MqttBroker mb;

	private final String namespace;
	
    public SensinactCoreBase(Mediator mediator, String namespace){
    	this.mediator = mediator;
    	this.namespace = namespace;
    	this.ca = this.mediator.getContext().getService(
        	this.mediator.getContext().getServiceReference(ConfigurationAdmin.class));
    	this.sensinact = this.mediator.getContext().getService(
        	this.mediator.getContext().getServiceReference(Core.class));
    }

    public void activate(){
        LOG.info("Activating Sensinact Remote object");
        try {
            Dictionary<String,Object> properties=ca.getConfiguration("sensinact").getProperties();
            final String brokerAddr = properties.get("broker").toString();
            final String brokerTopicPrefix=properties.get("broker.topic.prefix").toString();
            MQTTURLExtract mqttURL=new MQTTURLExtract(brokerAddr);

            mb=new MqttBroker.Builder().host(mqttURL.getHost()).port(mqttURL.getPort()).protocol(MqttBroker.Protocol.valueOf(
            		mqttURL.getProtocol().toUpperCase())).build();
            sensinact.registerAgent(this.mediator, new AbstractMidAgentCallback(){
                @Override
                public void doHandle(SnaLifecycleMessageImpl message) throws MidCallbackException {
                    publicRawMessage(message);
                }

                @Override
                public void doHandle(SnaUpdateMessageImpl message) throws MidCallbackException {
                    publicRawMessage(message);
                }

                @Override
                public boolean propagate() {
                    return false;
                }

                @Override
                public void stop() {
                }

                private void publicRawMessage(SnaMessage<?> event){
                    try {
                    	  String uri = JsonProviderFactory.getProvider()
                    			  .createReader(new StringReader(event.getJSON()))
                    			  .readObject()
                    			  .getString("uri");
                    	  int index = uri.indexOf(':');
	                      if(index < 0){
	                         mb.publish(String.format("%s%s",brokerTopicPrefix, namespace), 
	                        		 event.getJSON().toString());
	                         LOG.debug("Sending from namespace {} the message {}", namespace, 
	                        		 event.getJSON().toString());
	                      }else {	                    	  
	                         LOG.debug("Not propagating message from remote Sensinact instance :\n {} ", 
	                        		 event.getJSON().toString());
	                      }
                    } catch (Exception e) {
                        LOG.error("Failed",e);
                    }
                }
            },null);
            mb.connect();
        } catch (IOException e) {
            LOG.error("Failed",e);
        } catch (Exception e) {
            LOG.error("Failed",e);
        }
    }

    public void deactivate(){
        try {
            mb.disconnect();
        } catch (Exception e) {
            //Doesnt matter the result of this disconnection
        }
    }

    @Override
    public String namespace() {
        return this.namespace;
    }

    Session getSession(String identifier){
    	Session s = sensinact.getRemoteSession(identifier);
    	return s;
    }

    @Override
    public String getAll(String identifier, String filter) {
        return getSession(identifier).getAll(filter, null).getJSON();
    }

    @Override
    public String getProviders(String identifier, String filter) {
       Session session= getSession(identifier);
       StringBuilder sb = new StringBuilder();
       Set<ServiceProvider> value = session.serviceProviders(filter);
       for(ServiceProvider sp: value){
    	   sb.append(sb.length() >0 ? ',':"");
    	   sb.append('"');
    	   sb.append(sp.getName());
    	   sb.append('"');  	   
       }
       return sb.toString();
    }

    
    @Override
    public String getServices(String identifier, String serviceProviderId) {
        String value = getSession(identifier).getServices(serviceProviderId).getJSON();
        return value;
    }

    @Override
    public String getService(String identifier, String serviceProviderId, String serviceId) {
        String value = getSession(identifier).getService(serviceProviderId,serviceId).getJSON();
        return value;
    }

    @Override
    public String getResources(String identifier, String serviceProviderId, String serviceId) {
        String value = getSession(identifier).getResources(serviceProviderId,serviceId).getJSON();
        return value;
    }

    @Override
    public String getProvider(String identifier, String serviceProviderId) {
        String value = getSession(identifier).getProvider(serviceProviderId).getJSON();
        return value;
    }

    @Override
    public String getResource(String identifier, String serviceProviderId, String serviceId, String resourceId) {
        String value = getSession(identifier).getResource(identifier,serviceProviderId,serviceId,resourceId).getJSON();
        return value.toString();
    }

    @Override
    public String get(String identifier, String serviceProviderId, String serviceId, String resourceId, String attributeId) {
        String value = getSession(identifier).get(serviceProviderId,serviceId,resourceId,attributeId).getJSON();
        return value;
    }

    @Override
    public String set(String identifier, String serviceProviderId, String serviceId, String resourceId, String attributeId, String parameter) {
        String resultResponse = getSession(identifier).set(serviceProviderId,serviceId,resourceId,attributeId,parameter).getJSON();
        return resultResponse;
    }

    @Override
    public String act(String identifier, String serviceProviderId, String serviceId, String resourceId, String parameters) {
        String resultResponse = getSession(identifier).act(serviceProviderId,serviceId,resourceId,createObjectArrayParamFromJSON(parameters)).getJSON();
        return resultResponse;
    }

    private String[] createObjectArrayParamFromJSON(String parameters){
    	return JsonProviderFactory.getProvider()
    			.createReader(new StringReader(parameters))
    			.readArray()
    			.stream()
    			.map(JsonValue::toString)
    			.toArray(String[]::new);
    }

	@Override
	public boolean isAccessible(String identifier, String serviceProviderId, String serviceId, String resourceId) {
		return getSession(identifier).isAccessible(serviceProviderId, serviceId, resourceId);
	}

}
