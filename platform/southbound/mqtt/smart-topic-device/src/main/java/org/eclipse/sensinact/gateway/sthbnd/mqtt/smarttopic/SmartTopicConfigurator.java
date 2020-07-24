package org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic;

import java.util.ArrayList;
import java.util.Dictionary;

import org.eclipse.sensinact.gateway.core.ModelConfiguration;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration.BuildPolicy;
import org.eclipse.sensinact.gateway.generic.ProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.ProtocolStackEndpointConfiguratorAdapter;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.device.MqttPacket;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.device.MqttProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.exception.MessageInvalidSmartTopicException;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.model.SmartTopicInterpolator;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.ProcessorExecutor;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.ProcessorUtil;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.formats.ProcessorFormatArray;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.formats.ProcessorFormatBase64;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.formats.ProcessorFormatDivide;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.formats.ProcessorFormatJSON;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.formats.ProcessorFormatMinus;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.formats.ProcessorFormatMultiply;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.formats.ProcessorFormatPlus;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.formats.ProcessorFormatToFloat;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.formats.ProcessorFormatToInteger;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.formats.ProcessorFormatToString;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.formats.ProcessorFormatURLEncode;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.formats.iface.ProcessorFormatIface;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.util.api.MqttAuthentication;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.util.api.MqttBroker;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.util.api.MqttTopic;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.util.listener.MqttTopicMessage;

class SmartTopicConfigurator extends ProtocolStackEndpointConfiguratorAdapter {
	
	@SuppressWarnings("serial")
	private static final ProcessorExecutor PROCESSOR_EXECUTOR = new ProcessorExecutor(
		new ArrayList<ProcessorFormatIface>() {{
	        add(new ProcessorFormatArray());
	        add(new ProcessorFormatBase64());
	        add(new ProcessorFormatJSON());
	        add(new ProcessorFormatToString());
	        add(new ProcessorFormatURLEncode());
	        add(new ProcessorFormatPlus());
	        add(new ProcessorFormatMinus());
	        add(new ProcessorFormatMultiply());
	        add(new ProcessorFormatDivide());
	        add(new ProcessorFormatToFloat());
	        add(new ProcessorFormatToInteger());
    }});
	private boolean preconfigured = false;
	private MqttTopicMessage listener;
	private String provider;
	private String service;
	private String resource;
	private SmartTopicInterpolator smartTopicInterpolator;
	private MqttBroker broker;
	private String topic;
	private Object latitude;
	private Object longitude;
	private boolean startAtInitializationTime;
	
	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.generic.ProtocolStackEndpointConfiguratorAdapter#preConnectConfiguration(org.eclipse.sensinact.gateway.generic.ProtocolStackEndpoint, org.eclipse.sensinact.gateway.core.ModelConfiguration, java.util.Dictionary)
	 */
	@Override
	public void preConnectConfiguration(final ProtocolStackEndpoint<?> endpoint, ModelConfiguration configuration, Dictionary props) {
		final String id = (String) props.get("id");
	    String username = (String) props.get("username");
	    String password =(String) props.get("password");
	    
		MqttAuthentication authentication = createAuthentication(username,password);

		Object protocol = props.get("protocol");
		if(protocol == null) {
			protocol = "TCP";
		}
		Object host = props.get("host");
		if(host == null) {
			host = "127.0.0.1";
		}
		Object port = props.get("port");
		if(port == null) {
			port = 1883;
		} else {
			port = Integer.valueOf(String.valueOf(port));
		}
		this.broker = createBroker(String.valueOf(protocol), String.valueOf(host), (int)port, authentication);
		if(broker == null) {
			return;
		}
		this.topic = (String) props.get("topic");
		if(topic == null) {
			return;
		}
		final String topicType;
		Object topicTypeProperties = props.get("topic.type");
		if(topicTypeProperties == null) {
			topicTypeProperties = "mqtt";
		}
		topicType = String.valueOf(topicTypeProperties);
		final String processor = (String) props.get("processor");
	    
	    Object interpolatedLatitude = props.get("location.latitude");
		if(interpolatedLatitude != null) {
			interpolatedLatitude = Float.valueOf(String.valueOf(interpolatedLatitude));
		}
		this.latitude = interpolatedLatitude;
	    Object interpolatedLongitude = props.get("location.longitude");
		if(interpolatedLongitude != null) {
			interpolatedLongitude = Float.valueOf(String.valueOf(interpolatedLongitude));
		}
		this.longitude = interpolatedLongitude;
		this.startAtInitializationTime = configuration.getStartAtInitializationTime();
		        
        String interpolatedProvider = null;
        String interpolatedService = null;
		String interpolatedResource = null; 
		
		switch(topicType) {
			case "smarttopic":			   
				this.smartTopicInterpolator = new SmartTopicInterpolator(topic);
	     	    try {
					interpolatedProvider = smartTopicInterpolator.getGroup(topic, "provider");
					try {
						interpolatedService = smartTopicInterpolator.getGroup(topic, "service");
	                } catch (MessageInvalidSmartTopicException e1) {
	                	interpolatedService = "info";
	                }
					try {
	                	interpolatedResource = smartTopicInterpolator.getGroup(topic, "resource");
	                } catch (MessageInvalidSmartTopicException e1) {
	                	interpolatedResource = "value";
	                }
	            } catch (Exception e) {
	            	e.printStackTrace();
	            	interpolatedProvider = null;
	            }
	            break;
			case "mqtt":
				interpolatedProvider = id;
				interpolatedService = "info";
				interpolatedResource = "value";	
				smartTopicInterpolator = null;
				break;			
			default:
				return;
		}
        this.provider = interpolatedProvider;
		this.service = interpolatedService;
        this.resource = interpolatedResource;
        if(provider == null) {
        	return;
        }
		this.listener = new MqttTopicMessage() {				
			private volatile boolean started = false;				
            @Override
            public void messageReceived(String topic, String message) {
                try {
                	System.out.println("TOPIC : "+ topic);
                	System.out.println("MESSAGE : "+ message);
                	if(!started && !startAtInitializationTime) {
        				MqttPacket packet = new MqttPacket(provider, service, resource, null);
        				packet.setHelloMessage(true);
        				((MqttProtocolStackEndpoint)endpoint).process(packet);
        				if(latitude!=null && longitude!=null) {
	        				packet = new MqttPacket(provider, "admin", "location", new StringBuilder(
	        					).append(latitude).append(':').append(longitude).toString());
	        				((MqttProtocolStackEndpoint)endpoint).process(packet);
        				}
                	}
                	started = true;
                	String value = null; 
                	switch(topicType) {
	                	case "mqtt":
	                        value = PROCESSOR_EXECUTOR.execute(message, ProcessorUtil.transformProcessorListInSelector(processor== null?"": processor));
	                        break;
	                	case "smarttopic":
	                 		try {	                			
	                	 		value = smartTopicInterpolator.getGroup(message, "value");
	                        } catch (MessageInvalidSmartTopicException e1) {
	                            value = message;
	                        }
	                	    value = PROCESSOR_EXECUTOR.execute(value, ProcessorUtil.transformProcessorListInSelector(processor == null ? "" : processor));
	                	    break;
	                	default:
	                	    break;
                	}
                    ((MqttProtocolStackEndpoint)endpoint).process(new MqttPacket(provider, service, resource, value));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        preconfigured = true;
		MqttTopic mqttTopic = new MqttTopic(topic, listener);
		//No need to subscribe as it will be done at connection time
		//TODO: to be updated when the broker will not send the reference to its internal list anymore
        broker.getTopics().add(mqttTopic);
		((MqttProtocolStackEndpoint)endpoint).addBroker(broker);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.generic.ProtocolStackEndpointConfiguratorAdapter#postConnectConfiguration(org.eclipse.sensinact.gateway.generic.ProtocolStackEndpoint, org.eclipse.sensinact.gateway.core.ModelConfiguration, java.util.Dictionary)
	 */
	@Override
	public void postConnectConfiguration(final ProtocolStackEndpoint<?> endpoint, ModelConfiguration configuration, Dictionary props) {
		if(!preconfigured)
			return;
		if(startAtInitializationTime) {
			MqttPacket packet = new MqttPacket(provider, service, resource, null);
			packet.setHelloMessage(true);
			try {
				((MqttProtocolStackEndpoint)endpoint).process(packet);				
			} catch (InvalidPacketException e) {
				e.printStackTrace();
			}
			if(latitude!=null && longitude!=null) {
				packet = new MqttPacket(provider, "admin", "location", new StringBuilder(
						).append(latitude).append(':').append(longitude).toString());
				try {
					((MqttProtocolStackEndpoint)endpoint).process(packet);
				} catch (InvalidPacketException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.generic.ProtocolStackEndpointConfiguratorAdapter#getDefaultEndpointType()
	 */
	@Override
	public Class<? extends ProtocolStackEndpoint> getDefaultEndpointType() {
		return MqttProtocolStackEndpoint.class;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.generic.ProtocolStackEndpointConfiguratorAdapter#getDefaultPacketType()
	 */
	@Override
	public Class<? extends Packet> getDefaultPacketType() {
		return MqttPacket.class;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.generic.ProtocolStackEndpointConfiguratorAdapter#getDefaultServiceBuildPolicy()
	 */
	@Override
	public BuildPolicy[] getDefaultServiceBuildPolicy() {
		return new BuildPolicy[] {BuildPolicy.BUILD_NON_DESCRIBED};		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.generic.ProtocolStackEndpointConfiguratorAdapter#getDefaultResourceBuildPolicy()
	 */
	@Override
	public BuildPolicy[] getDefaultResourceBuildPolicy() {
		return new BuildPolicy[] {BuildPolicy.BUILD_NON_DESCRIBED};		
	}

	private MqttAuthentication createAuthentication(String username, String password) {
		MqttAuthentication authentication = null;
        if(username!=null 
        	&& !username.trim().equals("") 
        	&& password!=null 
        	&& !password.trim().equals("")){
            authentication = new MqttAuthentication.Builder(
            	).username(username
            	).password(password
            	).build();
        }
		return authentication;
	}

	private MqttBroker createBroker(String protocol, String host, int port, MqttAuthentication authentication) {
		MqttBroker broker = new MqttBroker.Builder(
			).host(host
			).port(port
			).protocol(MqttBroker.Protocol.valueOf(protocol)
			).authentication(authentication
			).build();
		return broker;
	}
}