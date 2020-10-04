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
package org.eclipse.sensinact.gateway.generic;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration.BuildPolicy;
import org.eclipse.sensinact.gateway.generic.annotation.SensiNactBridgeConfiguration;
import org.eclipse.sensinact.gateway.generic.annotation.ServiceProviderDefinition;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.util.json.JSONValidator;
import org.eclipse.sensinact.gateway.util.json.JSONValidator.JSONToken;
import org.json.JSONException;

/**
 * Plain old java object describing the configuration of a sensiNact bridge 
 *
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public class SensiNactBridgeConfigurationPojo {
	
	public static final String DEFAULT_RESOURCE_DEFINITION = "resources.xml";

	public static final Class<? extends Packet> DEFAULT_PACKET_TYPE = Packet.class;
	public static final Class<? extends ProtocolStackEndpoint> DEFAULT_PROTOCOL_STACK_ENDPOINT_TYPE = LocalProtocolStackEndpoint.class;
	
	public static final boolean DEFAULT_START_AT_INITIALIZATION_TIME = true;
	public static final boolean DEFAULT_OUTPUT_ONLY = false;
	
	public static final BuildPolicy[] DEFAULT_SERVICE_BUILD_POLICY = {BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION};
	public static final BuildPolicy[] DEFAULT_RESOURCE_BUILD_POLICY = {BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION};

	private final String defaultResourceDefinition;
	private String resourceDefinition;

	private final boolean defaultStartAtInitializationTime;
	private boolean startAtInitializationTime;
	
	private final boolean defaultOutputOnly;
	private boolean outputOnly;

	private final BuildPolicy[] defaultServiceBuildPolicy;
	private BuildPolicy[] serviceBuildPolicy;
	
	private final BuildPolicy[] defaultResourceBuildPolicy;
	private BuildPolicy[] resourceBuildPolicy;

	private final Map<String, String> defaultInitialProviders;
	private Map<String, String> initialProviders;

	private final String[] defaultObserved;
	private String[] observed;

	private final Class<? extends Packet> defaultPacketType;	
	private Class<? extends Packet> packetType;	

	private final Class<? extends ProtocolStackEndpoint> defaultEndpointType;
	private Class<? extends ProtocolStackEndpoint> endpointType;
	
	/**
	 * Constructor
	 */
	public SensiNactBridgeConfigurationPojo() {
		this.defaultResourceDefinition = DEFAULT_RESOURCE_DEFINITION;
		this.defaultStartAtInitializationTime = DEFAULT_START_AT_INITIALIZATION_TIME;
		this.isStartAtInitializationTime(this.defaultStartAtInitializationTime);	
		this.defaultOutputOnly = DEFAULT_OUTPUT_ONLY;
		this.isOutputOnly(this.defaultOutputOnly);
		this.defaultServiceBuildPolicy = DEFAULT_SERVICE_BUILD_POLICY;
		this.defaultResourceBuildPolicy = DEFAULT_RESOURCE_BUILD_POLICY;
		this.defaultInitialProviders = Collections.<String,String>emptyMap();
		this.defaultObserved = new String[0];
		this.defaultPacketType = DEFAULT_PACKET_TYPE;
		this.defaultEndpointType = DEFAULT_PROTOCOL_STACK_ENDPOINT_TYPE;
	}

	/**
	 * Constructor
	 * 
	 * @param configurator the {@link ProtocolStackEndpointConfigurator} holding 
	 * the default configuration elements of the SensiNactBridgeConfigurationPojo to 
	 * be instantiated
	 */
	public SensiNactBridgeConfigurationPojo(ProtocolStackEndpointConfigurator configurator) {
		this.defaultResourceDefinition = configurator.getDefaultResourceDefinition();
		this.defaultStartAtInitializationTime = configurator.getDefaultStartAtInitializationTime();
		this.isStartAtInitializationTime(this.defaultStartAtInitializationTime);		
		this.defaultOutputOnly = configurator.getDefaultOutputOnly();
		this.isOutputOnly(this.defaultOutputOnly);
		this.defaultServiceBuildPolicy = configurator.getDefaultServiceBuildPolicy();
		this.defaultResourceBuildPolicy = configurator.getDefaultResourceBuildPolicy();
		this.defaultInitialProviders = configurator.getDefaultInitialProviders();
		this.defaultObserved = configurator.getDefaultObserved();
		this.defaultPacketType = configurator.getDefaultPacketType();
		this.defaultEndpointType = configurator.getDefaultEndpointType();
	}
	
	
	/**
	 * Populates this SensiNactBridgeConfigurationPojo with the elements of the 
	 * bridge configuration described in the JSON formated String argument
	 * 
	 * @param json the JSON formated String describing the bridge configuration
	 */
	public void populate(String json){		
		if(json != null) {
			List<String> observedPath = new ArrayList<>();
			this.initialProviders = new HashMap<>();
			
			JSONValidator validator = new JSONValidator(new StringReader(json));
			
			String packetClass = null;
			String endpointClass = null;
			
			boolean parsingObserved=false;
			boolean parsingProviders=false;
			
			String provider = null;
			String profile = null;
					
			try {
	            while (true) {
	                JSONToken t = validator.nextToken();
	                if (t == null) {
	                    break;
	                }
	               switch(t) {
	               case JSON_OBJECT_ITEM:
	                	if(t.getContext().key == null) {
	                		continue;
	                	}	                	
	                	switch(t.getContext().key) {
		                	case "providerId":
		                		if(parsingProviders) {
		                			if(profile!=null) {
		                				this.initialProviders.put(String.valueOf(t.getContext().value),profile);
		                				profile = null;
		                			} else {
		                				provider = String.valueOf(t.getContext().value);
		                			}
		                		}
		                		break;
		                	case "profileId":
		                		if(parsingProviders) {
		                			if(provider!=null) {
		                				this.initialProviders.put(provider,String.valueOf(t.getContext().value));
		                				provider = null;
		                			} else {
		                				profile = String.valueOf(t.getContext().value);
		                			}
		                		}
		                		break;
		                	case "resourceDefinition":
		                		if(!parsingProviders) {
		                			this.setResourceDefinition(String.valueOf(t.getContext().value));
		                		}
		                		break;
		                	case "packetType":
		                		if(!parsingProviders) {
		                			packetClass = String.valueOf(t.getContext().value);
		                		}
		                		break;
		                	case "endpointType":
		                		if(!parsingProviders) {
		                			endpointClass = String.valueOf(t.getContext().value);
		                		}
		                		break;
		                	case "resourceBuildPolicy":
		                		if(!parsingProviders) {
		                			Object policy = t.getContext().value;
		                			if(policy instanceof String)
		                				this.setResourceBuildPolicy(new BuildPolicy[] {BuildPolicy.valueOf((String)policy)});
		                			else if(policy instanceof Byte) {
		                				this.setResourceBuildPolicy(BuildPolicy.valueOf((Byte)policy));
		                			}
		                		}
		                		break;
		                	case "serviceBuildPolicy":
		                		if(!parsingProviders) {
		                			Object policy = t.getContext().value;
		                			if(policy instanceof String)
		                				this.setServiceBuildPolicy(new BuildPolicy[] {BuildPolicy.valueOf((String)policy)});
		                			else if(policy instanceof Byte) {
		                				this.setServiceBuildPolicy(BuildPolicy.valueOf((Byte)policy));
		                			}
		                		}
		                		break;
		                	case "startAtInitializationTime":
		                		if(!parsingProviders)
		                			this.isStartAtInitializationTime(Boolean.valueOf(String.valueOf(t.getContext().value)));
		                		break;
		                	case "outputOnly":
		                		if(!parsingProviders)
		                			this.isOutputOnly(Boolean.valueOf(String.valueOf(t.getContext().value)));
		                		break;
	                	}
					case JSON_ARRAY_ITEM:
						if(parsingObserved)
							observedPath.add(String.valueOf(t.getContext().value));
						break;
					case JSON_ARRAY_CLOSING:
						if(parsingObserved) {
							parsingObserved=false;
							this.setObserved(observedPath.toArray(new String[0]));
						}
						break;
					case JSON_ARRAY_OPENING:
						if(parsingProviders || parsingObserved)
							throw new JSONException("Unexpected array opening bracket");
						if("observed".equals(t.getContext().key))
							parsingObserved = true;
						break;
					case JSON_OBJECT_OPENING:
						if(parsingProviders || parsingObserved)
							throw new JSONException("Unexpected object opening brace");
						if("initialProviders".equals(t.getContext().key))
							parsingProviders=true;
						break;
					case JSON_OBJECT_CLOSING:
						if(parsingProviders)
							parsingProviders = false;						
						break;
					default:
						break;
	                }
	            }
	        } catch (JSONException e) {
	            e.printStackTrace();
	        }		
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			if(packetClass !=null) {
				try {
					this.setPacketType((Class<? extends Packet>) loader.loadClass(packetClass));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			if(endpointClass !=null) {
				try {
					this.setEndpointType((Class<? extends ProtocolStackEndpoint>) loader.loadClass(endpointClass));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	/**
	 * Populates this SensiNactBridgeConfigurationPojo with the elements of the 
	 * bridge configuration provided by the Dictionary argument
	 * 
	 * @param dictionary the Dictionary providing the elements of the bridge configuration
	 */
	public void populate(Dictionary dictionary){		
		if(dictionary != null) {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();

			this.initialProviders = new HashMap<>();
			List<String> observedPath = new ArrayList<>();
			
			Object resourceDefinition = dictionary.get("resourceDefinition");
			if(resourceDefinition != null) {
				this.setResourceDefinition(String.valueOf(resourceDefinition));
			}						
			Object packetClass = dictionary.get("packetType");
			if(packetClass != null) {
				if(packetClass instanceof Class) {
					this.setPacketType((Class<? extends Packet>) packetClass);
				} else {
					try {				
						this.setPacketType((Class<? extends Packet>) loader.loadClass(String.valueOf(packetClass)));
					} catch(ClassNotFoundException e){
						e.printStackTrace();
					}
				}
			}			
			Object endpointClass = dictionary.get("endpointType");
			if(endpointClass != null) {
				if(endpointClass instanceof Class) {
					this.setEndpointType((Class<? extends ProtocolStackEndpoint>) endpointClass);
				} else {
					try {				
						this.setEndpointType((Class<? extends ProtocolStackEndpoint>) loader.loadClass(String.valueOf(endpointClass)));
					} catch(ClassNotFoundException e){
						e.printStackTrace();
					}
				}
			}
			Object startAtInitializationTime = dictionary.get("startAtInitializationTime");	
			if(startAtInitializationTime!=null) {
				if(startAtInitializationTime instanceof Boolean) {
					this.isStartAtInitializationTime((Boolean)startAtInitializationTime);
				} else {
					this.isStartAtInitializationTime(Boolean.valueOf(String.valueOf(startAtInitializationTime)));
				}
			}
			Object outputOnly = dictionary.get("outputOnly");	
			if(outputOnly!=null) {
				if(outputOnly instanceof Boolean) {
					this.isOutputOnly((Boolean)outputOnly);
				} else {
					this.isOutputOnly(Boolean.valueOf(String.valueOf(outputOnly)));
				}
			}
			Object observed = dictionary.get("observed");
			if(observed != null) {
				if(observed instanceof String[]) {
					this.setObserved((String[]) observed);
				} else {
					String obs = String.valueOf(observed);
					if(obs.startsWith("[")) {
						obs = obs.substring(1);
					}
					if(obs.endsWith("]")) {
						obs = obs.substring(0,obs.length()-1);
					}
					String[] obsArray = obs.split(",");
					for(String o : obsArray) {
						observedPath.add(o.trim());
					}
				}
			}
			Object serviceBuildPolicy = dictionary.get("serviceBuildPolicy");
			if(serviceBuildPolicy != null) {
				if(serviceBuildPolicy instanceof BuildPolicy[]) {
					this.setServiceBuildPolicy((BuildPolicy[]) serviceBuildPolicy);
				} else if(serviceBuildPolicy instanceof String[]) {
					BuildPolicy[] sbp = new BuildPolicy[((String[]) serviceBuildPolicy).length];
					for(int i=0;i<((String[]) serviceBuildPolicy).length;i++) {
						sbp[i] = BuildPolicy.valueOf(((String[]) serviceBuildPolicy)[i]);
					}
					this.setServiceBuildPolicy(sbp);
				} else {
					String bps = String.valueOf(serviceBuildPolicy);
					if( bps.startsWith("[")) {
						 bps =  bps.substring(1);
					}
					if( bps.endsWith("]")) {
						 bps =  bps.substring(0, bps.length()-1);
					}
					String[]  bpsArray =  bps.split(",");
					BuildPolicy[] sbp = new BuildPolicy[ bpsArray.length];
					for(int i=0;i<((String[]) serviceBuildPolicy).length;i++) {
						sbp[i] = BuildPolicy.valueOf( bpsArray[i]);
					}
					this.setServiceBuildPolicy(sbp);
				}
			}
			Object resourceBuildPolicy = dictionary.get("resourceBuildPolicy");
			if(resourceBuildPolicy != null) {
				if(resourceBuildPolicy instanceof BuildPolicy[]) {
					this.setResourceBuildPolicy((BuildPolicy[]) resourceBuildPolicy);
				} else if(resourceBuildPolicy instanceof String[]) {
					BuildPolicy[] sbp = new BuildPolicy[((String[]) resourceBuildPolicy).length];
					for(int i=0;i<((String[]) resourceBuildPolicy).length;i++) {
						sbp[i] = BuildPolicy.valueOf(((String[]) resourceBuildPolicy)[i]);
					}
					this.setResourceBuildPolicy(sbp);
				} else {
					String bps = String.valueOf(resourceBuildPolicy);
					if( bps.startsWith("[")) {
						 bps =  bps.substring(1);
					}
					if( bps.endsWith("]")) {
						 bps =  bps.substring(0, bps.length()-1);
					}
					String[] bpsArray =  bps.split(",");
					BuildPolicy[] sbp = new BuildPolicy[ bpsArray.length];
					for(int i=0;i<((String[]) resourceBuildPolicy).length;i++) {
						sbp[i] = BuildPolicy.valueOf( bpsArray[i]);
					}
					this.setResourceBuildPolicy(sbp);
				}
			}
			Object initialProviders = dictionary.get("initialProviders");
			if(initialProviders != null) {
				if(initialProviders instanceof Map) {
					this.setInitialProviders((Map<String, String>) initialProviders);
				} else if(initialProviders instanceof String[][]) {
					Map<String, String> ips = new HashMap<String,String>();
					for(int i=0;i<((String[][])initialProviders).length;i++) {
						ips.put(((String[][])initialProviders)[i][0], ((String[][])initialProviders)[i][1]);
					}
					this.setInitialProviders(ips);
				} else if(initialProviders instanceof String[]) {
					Map<String, String> ips = new HashMap<String,String>();
					for(int i=0;i<((String[])initialProviders).length;i++) {
						String ip = ((String[])initialProviders)[i];
						String[] els = ip.split(";");
						if(els.length == 2) {
							ips.put(els[0],els[1]);
						}
					}
					this.setInitialProviders(ips);					
				} else if(initialProviders instanceof String) {
					Map<String, String> ips = new HashMap<String,String>();
					String[] providers = ((String)initialProviders).split(",");
					for(int i=0;i<providers.length;i++) {
						String ip = providers[i];
						String[] els = ip.split(";");
						if(els.length == 2) {
							ips.put(els[0],els[1]);
						}
					}
					this.setInitialProviders(ips);
				}
			}
		}
	}

	
	/**
	 * Populates this SensiNactBridgeConfigurationPojo with the elements of the 
	 * bridge configuration provided by the SensiNactBridgeConfiguration annotation argument
	 * 
	 * @param annotation the {@link SensiNactBridgeConfiguration} annotation describing the 
	 * bridge configuration
	 */
	public void populate(SensiNactBridgeConfiguration annotation){
		if(annotation != null) {
			this.setPacketType(annotation.packetType());
			this.setEndpointType(annotation.endpointType());
			this.setServiceBuildPolicy(annotation.serviceBuildPolicy());			
			this.setResourceBuildPolicy(annotation.resourceBuildPolicy());			
			this.setInitialProviders(Arrays.stream(annotation.initialProviders()).collect(
			   Collectors.<ServiceProviderDefinition, String, String>toMap(
					   p->p.name(), p->p.profileId())));
			this.setObserved(annotation.observed());
		}
	}
	
	/**
	 * Returns the resources definition file path  
	 * 
	 * @return the resources definition file path 
	 */
	public String getResourceDefinition() {
		if(this.resourceDefinition == null) {
			return this.defaultResourceDefinition;
		}
		return this.resourceDefinition;
	}

	/**
	 * Defines the resources definition file path  
	 * 
	 * @param resourceDefinition the resources definition file path 
	 */
	public void setResourceDefinition(String resourceDefinition) {
		this.resourceDefinition = resourceDefinition;
	}
	
	/**
	 * Returns the array of {@link BuildPolicy}s applying on service
	 * build  
	 * 
	 * @return the array of {@link BuildPolicy}s applying 
	 */
	public BuildPolicy[] getServiceBuildPolicy() {
		if(this.serviceBuildPolicy == null) {
			return this.defaultServiceBuildPolicy;
		}
		return this.serviceBuildPolicy;
	}

	/**
	 * Defines the array of {@link BuildPolicy}s applying on service
	 * build  
	 * 
	 * @param serviceBuildPolicy the array of {@link BuildPolicy}s applying 
	 */
	public void setServiceBuildPolicy(BuildPolicy[] serviceBuildPolicy) {
		this.serviceBuildPolicy = serviceBuildPolicy;
	}

	/**
	 * Returns the array of {@link BuildPolicy}s applying on resource
	 * build  
	 * 
	 * @return the array of {@link BuildPolicy}s applying 
	 */
	public BuildPolicy[] getResourceBuildPolicy() {
		if(this.resourceBuildPolicy == null) {
			return this.defaultResourceBuildPolicy;
		}
		return this.resourceBuildPolicy;
	}

	/**
	 * Defines the array of {@link BuildPolicy}s applying on resource
	 * build  
	 * 
	 * @param resourceBuildPolicy the array of {@link BuildPolicy}s applying 
	 */
	public void setResourceBuildPolicy(BuildPolicy[] resourceBuildPolicy) {
		this.resourceBuildPolicy = resourceBuildPolicy;
	}

	/**
	 * Returns the boolean value defining whether a newly created resource is started 
	 * automatically or not
	 * 
	 * @return the resource automatic start status 
	 */
	public boolean isStartAtInitializationTime() {		
		return this.startAtInitializationTime;
	}

	/**
	 * Defines whether a newly created resource is started automatically or not
	 * 
	 * @param startAtInitializationTime the resource automatic start status 
	 */
	public void isStartAtInitializationTime(boolean startAtInitializationTime) {		
		this.startAtInitializationTime = startAtInitializationTime;
	}

	/**
	 * Returns the processed {@link Packet} type 
	 * 
	 * @return the processed {@link Packet} type  
	 */
	public Class<? extends Packet> getPacketType() {
		if(this.packetType == null) {
			return this.defaultPacketType;
		}
		return this.packetType;
	}

	/**
	 * Defines the processed {@link Packet} type 
	 * 
	 * @param packetType the processed {@link Packet} type  
	 */
	public void setPacketType( Class<? extends Packet> packetType) {
	    this.packetType = packetType;
	}

	/**
	 * Returns the {@link ProtocolStackEndpoint} type processing the communication
	 * packets
	 * 
	 * @return the {@link ProtocolStackEndpoint} type used 
	 */
	public Class<? extends ProtocolStackEndpoint> getEndpointType() {
		if(this.endpointType == null) {
			return this.defaultEndpointType;
		}
		return this.endpointType;
	}

	/**
	 * Defines the {@link ProtocolStackEndpoint} type processing the communication
	 * packets
	 * 
	 * @param endpointType the {@link ProtocolStackEndpoint} type used 
	 */
	public void setEndpointType(Class<? extends ProtocolStackEndpoint> endpointType) {
		this.endpointType = endpointType;
	}

	/**
	 * Returns the Map of initial set of providers to be created, mapped to 
	 * their profile
	 * 
	 * @return  the Map of initial set of providers and their profile
	 */
	public Map<String,String> getInitialProviders() {
		if(this.initialProviders == null) {
			return this.defaultInitialProviders;
		}
		return this.initialProviders;
	}

	/**
	 * Defines the Map of initial set of providers to be created, mapped to 
	 * their profile
	 * 
	 * @param initialProviders the Map of initial set of providers and their 
	 * profile
	 */
	public void setInitialProviders(Map<String,String> initialProviders) {
		this.initialProviders = initialProviders;
	}

	/**
	 * Returns the array of observed attributes String paths
	 * 
	 * @return the array of observed attributes String paths
	 */
	public String[] getObserved() {
		if(this.observed == null) {
			return this.defaultObserved;
		} 
		return this.observed;
	}

	/**
	 * Defines the array of observed attributes String paths
	 * 
	 * @param observed the array of observed attributes String paths
	 */
	public void setObserved(String[] observed) {
		this.observed = observed;
	}
	
	/**
	 * Returns the boolean value defining whether incoming communication packets will 
	 * have to be processed or no
	 * 
	 * @return the incoming communication packets processing status 
	 */
	public boolean isOutputOnly() {
		return this.outputOnly;
	}
	
	/**
	 * Defines whether incoming communication packets will have to be processed 
	 * or no
	 * 
	 * @param outputOnly the incoming communication packets processing status 
	 */
	public void isOutputOnly(boolean outputOnly) {
		this.outputOnly = outputOnly;
	}
}
