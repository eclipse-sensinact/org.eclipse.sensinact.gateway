package org.eclipse.sensinact.gateway.core.remote;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.message.AbstractSnaMessage;
import org.eclipse.sensinact.gateway.core.message.SnaAgent;
import org.eclipse.sensinact.gateway.core.message.SnaConstants;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.core.message.SnaRemoteMessage;
import org.eclipse.sensinact.gateway.core.message.SnaRemoteMessageImpl;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.util.api.MqttBroker;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.util.api.MqttTopic;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.util.listener.MqttTopicMessage;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensinactCoreBaseIFaceManagerImpl implements SensinactCoreBaseIFaceManager  {
	
	private final class SensinactCoreBaseIFaceObserverCustomizer implements ServiceTrackerCustomizer<SensinactCoreBaseIface,MqttBroker> {
		
		private final Mediator mediator;

		SensinactCoreBaseIFaceObserverCustomizer(Mediator mediator){
			this.mediator = mediator;
		}
		
		@Override
		public MqttBroker addingService(ServiceReference<SensinactCoreBaseIface> reference) {
			
			final String remoteNamespace = (String) reference.getProperty(REMOTE_NAMESPACE_PROPERTY);			
			LOG.info("Receiving RSA discovery notification about remote instance {}",remoteNamespace);

			if(remoteNamespace.equals(namespace())){
				return null;
			}			
			LOG.info("Connecting to RSA remote sensinact instance with namespace {}",remoteNamespace);

			final String brokerAddr = this.mediator.getProperty("broker").toString();
			final String brokerTopicPrefix = this.mediator.getProperty("broker.topic.prefix").toString();
			
			MQTTURLExtract mqttURL=new MQTTURLExtract(brokerAddr);
			MqttBroker mb = new MqttBroker.Builder().host(mqttURL.getHost()).port(mqttURL.getPort()).protocol(
					MqttBroker.Protocol.valueOf(mqttURL.getProtocol().toUpperCase())).build();

			MqttTopic topic=new MqttTopic(String.format("%s%s", brokerTopicPrefix, remoteNamespace), new MqttTopicMessage(){
				@Override
				protected void messageReceived(String topic, String mqttMessage) {
					LOG.info("Received remote notification from namespace {} on topic {} with message {}",remoteNamespace, topic, mqttMessage);
					JSONObject event=new JSONObject(mqttMessage);
					String path=event.getString("uri");
					String provider = path.split("/")[1];
					String uriTranslated= path.replaceFirst("/"+ provider, String.format("/%s:%s", remoteNamespace, provider));
					event.remove("uri");
					event.put("uri",uriTranslated);
					LOG.debug("Forwarding message received in local sensinact as {}",event.toString());
					SnaMessage<?> message = AbstractSnaMessage.fromJSON(SensinactCoreBaseIFaceObserverCustomizer.this.mediator,event.toString());
					notifyCallbacks(message);
				}
			});
			try {
				mb.subscribeToTopic(topic);
				mb.connect();
				SnaRemoteMessageImpl message = new SnaRemoteMessageImpl("/", SnaRemoteMessage.Remote.CONNECTED);
				message.setNotification(new JSONObject().append(SnaConstants.NAMESPACE, remoteNamespace));
				notifyCallbacks(message);
			} catch (Exception e) {
				LOG.error("Failed to connect to broker {}",brokerAddr,e);
			}

			return mb;
		}
		
		@Override
		public void removedService(ServiceReference<SensinactCoreBaseIface> reference, MqttBroker service) {
			String key = (String) reference.getProperty(REMOTE_NAMESPACE_PROPERTY);
			LOG.info("Removing RSA sensinact remote instance {} from the pool", key.toString());
			try {
				service.disconnect();
				SnaRemoteMessageImpl message = new SnaRemoteMessageImpl("/", SnaRemoteMessage.Remote.DISCONNECTED);
				message.setNotification(new JSONObject().append(SnaConstants.NAMESPACE, key));
				notifyCallbacks(message);
			} catch (Exception e) {
				LOG.error("Failing disconnecting from broker {}",service.getHost());
			}
		}

		@Override
		public void modifiedService(ServiceReference<SensinactCoreBaseIface> reference, MqttBroker service) {
			removedService(reference, service);
			addingService(reference);
		}
	}
	
    private final class SensinactCoreBaseIfaceConfigurationListener implements ConfigurationListener {
	
		@Override
		public void configurationEvent(ConfigurationEvent event) {
			LOG.debug("Receiving Configuration notification for {}",event.getPid());
			if(event.getPid().equals("sensinact")) {
				ConfigurationAdmin configurationAdmin = SensinactCoreBaseIFaceManagerImpl.this.mediator.getContext(
						).getService(event.getReference());
				Configuration confSensinact;
				try {
					confSensinact = configurationAdmin.getConfiguration("sensinact");
				} catch (IOException e) {
					LOG.error(e.getMessage(),e);
					return;
				}	
				SensinactCoreBaseIFaceManagerImpl.this.deactivateLocalSensinactCoreBaseIface();														
				switch(event.getType()) {
				   case ConfigurationEvent.CM_DELETED:
					    break;
				   case ConfigurationEvent.CM_LOCATION_CHANGED:
				   case ConfigurationEvent.CM_UPDATED:
						namespace = String.valueOf(confSensinact.getProperties().get("namespace"));
					    LOG.info("SensiNact configuration is available, starting up SensiNact core with namespace {}..", namespace );
					    SensinactCoreBaseIFaceManagerImpl.this.activateLocalSensinactCoreBaseIface();		
						break;
				}
			}
		}
    }
    
	private static final Logger LOG = LoggerFactory.getLogger(SensinactCoreBaseIFaceManager.class);
    
    private ServiceTracker<SensinactCoreBaseIface,MqttBroker> tracker;
	private ServiceRegistration<?> registration;
	private ServiceRegistration<?> registrationConfiguration;
	private ServiceRegistration<?> registrationCoreBaseIface;
	private SensinactCoreBase sensinactCoreBase;
	protected volatile String namespace;

	private Mediator mediator;
    
    SensinactCoreBaseIFaceManagerImpl(){
    }
    
    public void start(final Mediator mediator){
    	this.mediator = mediator;
    	final BundleContext context = mediator.getContext();
    	this.registrationConfiguration = context.registerService(ConfigurationListener.class.getCanonicalName(), 
    		new SensinactCoreBaseIfaceConfigurationListener(), new Hashtable<String,String>());   
    }
    
    public void stop() {    	
    	if(this.registrationConfiguration!=null) {
    		try {
    			this.registrationConfiguration.unregister();
    		} catch(IllegalStateException e) {
    			LOG.debug(e.getMessage());
    		}
    		this.registrationConfiguration = null;
    	}
    	deactivateLocalSensinactCoreBaseIface();
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private void activateLocalSensinactCoreBaseIface() {	 	
		this.tracker= new ServiceTracker<SensinactCoreBaseIface,MqttBroker>(this.mediator.getContext(), 
			FILTER_MAIN, new SensinactCoreBaseIFaceObserverCustomizer(mediator));
		this.tracker.open(true);
		
		this.registration =  this.mediator.getContext().registerService(
				SensinactCoreBaseIFaceManager.class, this , null);
		
    	sensinactCoreBase = new SensinactCoreBase(mediator, namespace);
		
		Dictionary props = new Hashtable();
		props.put("service.exported.interfaces", SensinactCoreBaseIface.class.getName());
		props.put("service.exported.configs","aries.fastbin");	
		props.put(REMOTE_NAMESPACE_PROPERTY, namespace);	
		
		this.registrationCoreBaseIface = this.mediator.getContext().registerService(
			SensinactCoreBaseIface.class, sensinactCoreBase ,props);
		
		sensinactCoreBase.activate();			
    }
    
    private void deactivateLocalSensinactCoreBaseIface() {    	
		if(sensinactCoreBase!=null) {
			sensinactCoreBase.deactivate();
		}
		if(this.registrationCoreBaseIface!=null) {
			try{
				this.registrationCoreBaseIface.unregister();
			} catch(IllegalStateException e) {
				LOG.error(e.getMessage(),e);
			}
			this.registrationCoreBaseIface = null;
			this.sensinactCoreBase = null;
		}
		if(this.registration != null) {
			try{
				this.registration.unregister();
			} catch(IllegalStateException e) {
				LOG.error(e.getMessage(),e);
			}
			this.registration = null;
		}
		if(this.tracker!=null) {
			this.tracker.close();
			this.tracker = null;
		}
    }

    final void notifyCallbacks(final SnaMessage<?> message){
		this.mediator.callServices(SnaAgent.class, new Executable<SnaAgent,Void>(){
			@Override
			public Void execute(SnaAgent agent) throws Exception {
				agent.register(message);
				return null;
			}			
		});
	}
    
	@Override
	public String namespace() {
		return this.namespace;
	}
}
