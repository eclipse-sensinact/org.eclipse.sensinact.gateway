package org.eclipse.sensinact.gateway.core;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.core.ServiceProvider.LifecycleStatus;
import org.eclipse.sensinact.gateway.core.message.AbstractMidCallback;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessage;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessage.Lifecycle;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.core.message.SnaMessageSubType;
import org.eclipse.sensinact.gateway.core.message.SnaUpdateMessage;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.security.AccessLevelOption;
import org.eclipse.sensinact.gateway.core.security.MutableAccessNode;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONObject;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * Wraps the {@link ServiceRegistration} of a {@link SensiNactResourceModel}
 * instance and updates the properties of its associated {@link ServiceReference}.
 *   
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ModelInstanceRegistration extends AbstractMidCallback
{
	private boolean registered;
	private ServiceRegistration<?> instanceRegistration;
	private ModelConfiguration configuration;

	/**
	 * Constructor
	 * 
	 * @param path the String uri of the {@link SensiNactResourceModel} 
	 * registered
	 * @param registration the {@link ServiceRegistration} of the {@link 
	 * SensiNactResourceModel} 
	 * @param configuration the {@link ModelConfiguration} of the {@link 
	 * ModelInstance} whose registration will be wrapped by the 
	 * ModelInstanceRegistration to be instantiated
	 */
	public ModelInstanceRegistration(
			String path, ServiceRegistration<?> registration, 
			ModelConfiguration configuration)
	{
		super(false);
		super.setIdentifier(path);
		this.instanceRegistration = registration;
		this.configuration = configuration;
		this.registered = true;
	}

	/**
	 * Unregisters the {@link ModelInstance} service 
	 * registration of this ModelInstanceRegistration
	 */
	public void unregister() 
	{
		this.registered = false;
		if(this.instanceRegistration!=null)
		{
			this.instanceRegistration.unregister();
		}
	}	

	/**
     * @param uri
     * @param location
     */
	void update(final Dictionary<String, Object> properties)
    {
    	if(!registered
    		||properties==null
    		||properties.size()==0
    		||this.instanceRegistration==null)
    	{
    		return;
    	}
    	synchronized(this.instanceRegistration)
    	{
    		AccessController.<Void>doPrivileged(
    				new PrivilegedAction<Void>()
    		{
    			@Override
    			public Void run()
    			{
    				try
    	    		{
    	    			ModelInstanceRegistration.this.instanceRegistration.setProperties(
    	    					properties);
    	    			
    	    		} catch(IllegalArgumentException e)
    	    		{
    	    			//if it is a duplicate service property
    	    			//try to retrieve it and to remove it
    	    			String message = e.getMessage();
    	    			String duplicateMessage = "Duplicate service property: "; 
    	    			String duplicateProperty = null;
    	    			if(message.startsWith(duplicateMessage))
    	    			{
    	    				duplicateProperty = message.substring(
    	    					duplicateMessage.length());
    	    			}
    	    			if(duplicateProperty!=null && properties.remove(
    	    					duplicateProperty)!=null)
    	    			{
    	    				update(properties);
    	    			}
    	    		} catch(Exception e)
    	    		{
    	    			e.printStackTrace();
    	    		}
    				return null;
    			}
    		}); 
    	}
    }

	/**
	 * @return
	 */
	private Dictionary<String, Object> properties()
	{ 
		final Hashtable<String, Object> properties = 
				new Hashtable<String, Object>();
	
	    if(this.instanceRegistration == null)
	    {
	    	return properties;
	    }
    	synchronized(this.instanceRegistration)
    	{
    		AccessController.<Void>doPrivileged(
			new PrivilegedAction<Void>()
			{
    			@Override
    			public Void run()
    			{	
	    			ServiceReference<?> ref = null;
	    			if((ref = 
	    			ModelInstanceRegistration.this.instanceRegistration.getReference()
	    					)!= null)
		    		{
			    		String[] keys = ref.getPropertyKeys();
			    		for(String key:keys)
			    		{
			    			properties.put(key, ref.getProperty(key));
			    		}
		    		}
	    			return null;
    			}
			});
    	}
    	return properties;
	}
	
	/**
     * @param uri
     * @param location
     */
	public void updateLifecycle(LifecycleStatus status)
    {
    	if(!registered)
    	{
    		return;
    	}
    	Dictionary<String, Object> properties = properties();
    	properties.put("lifecycle.status", status.name());
    	this.update(properties);
    }
	
	/**
     * @param uri
     * @param location
     */
	public void updateLocation(String location)
    {
    	if(!registered)
    	{
    		return;
    	}
    	Dictionary<String, Object> properties = properties();
    	properties.remove(LocationResource.LOCATION);
		properties.remove("latitude");
		properties.remove("longitude");		
    	if(location==null)
    	{ 
    		location = "0:0";
    	}
    	properties.put(LocationResource.LOCATION, location);
		String[] latlon = location.split(":");
		if(latlon.length == 2)
		{
			try
			{
	        	properties.put("latitude",
	        			Double.parseDouble(latlon[0]));
	        	properties.put("longitude",
	        			Double.parseDouble(latlon[1]));
			} catch(NumberFormatException e)
			{
				return;
			}
		}
    	this.update(properties);
    }
		
    /**
     * @param uri
     * @param object 
     * @param content
     */
    @SuppressWarnings("unchecked")
    public void updateContent(
    	String uri,
    	SnaLifecycleMessage.Lifecycle lifecycle, 
    	String type)
    {
    	if(!registered)
    	{
    		return;
    	}
    	String[] uriElements = UriUtils.getUriElements(uri);
    	int length = uriElements==null?0:uriElements.length;	
    	String service = (length > 1)?uriElements[1]:null;
		if(service == null)
		{
			return;
		}		
		MutableAccessNode node = null;
		MutableAccessNode root = this.configuration.getAccessTree().getRoot();
		AccessMethod.Type[] accessMethodTypes = AccessMethod.Type.values();
		int typesLength = accessMethodTypes==null?0:accessMethodTypes.length;
		
		if((node = (MutableAccessNode) root.get(uri)) == null)
		{
			node = root;
		}		
    	String resource = (length > 2)?uriElements[2]:null;
    	boolean added = !lifecycle.equals(Lifecycle.RESOURCE_DISAPPEARING)
    		&& !lifecycle.equals(Lifecycle.SERVICE_DISAPPEARING);
    	
    	Dictionary<String, Object> properties = properties();
    	
		if(resource != null)
		{
			String serviceKey = service.concat(".resources");
			String resourceKey = new StringBuilder().append(service).append(
					".").append(resource).toString();
			List<String> resources = (List<String>) properties.get(serviceKey);		
		    if(resources == null)
		    {
		    	resources = new ArrayList<String>();	
		    	properties.put(serviceKey, resources);
		    }
	    	if(added)
	    	{  		
	    		resources.add(resource);
	    		properties.put(resourceKey.concat(".type"),type);
				int index = 0;		
				for(;index < typesLength; index++)
				{
					AccessLevelOption accessLevelOption = node.getAccessLevelOption(
						accessMethodTypes[index]);
					
					properties.put(new StringBuilder().append(resourceKey).append(
						".").append(accessMethodTypes[index].name()).toString(),
							accessLevelOption.getAccessLevel().getLevel());
				}
	    	} else
	    	{
	    		resources.remove(resource);
	    		properties.remove(resourceKey.concat(".type"));
				int index = 0;				
				for(;index < typesLength; index++)
				{
					properties.remove(new StringBuilder().append(resourceKey).append(
						".").append(accessMethodTypes[index].name()).toString());
				}
	    	}
		} else
		{
			List<String> services = (List<String>) properties.get("services");			
		    if(services==null)
		    {
		    	services = new ArrayList<String>();	
		    	properties.put("services", services);
		    }		    
	    	if(added)
	    	{
				int index = 0;		
				for(;index < typesLength; index++)
				{
					AccessLevelOption accessLevelOption = node.getAccessLevelOption(
						accessMethodTypes[index]);
					
					properties.put(new StringBuilder().append(service).append(
						".").append(accessMethodTypes[index].name()).toString(),
							accessLevelOption.getAccessLevel().getLevel());
				}
	    		services.add(service);
	    		
	    	} else
	    	{
	    		services.remove(service);
	    		List<String> tobeRemoved = new ArrayList<String>();
	    		Enumeration enumeration = properties.keys();
	    		while(enumeration.hasMoreElements())
	    		{
	    			String key = (String) enumeration.nextElement();
	    			if(key!=null && key.startsWith(service.concat(".")))
	    			{
	    				tobeRemoved.add(key);
	    			}
	    		}
	    		Iterator<String> iterator = tobeRemoved.iterator();
	    		while(iterator.hasNext())
	    		{
	    			properties.remove(iterator.next());
	    		}  		
	    	}
		}
    	this.update(properties);
    }	

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.message.AbstractMidCallback#
	 * doCallback(org.eclipse.sensinact.gateway.core.message.SnaMessage)
	 */
	@Override
	public void doCallback(SnaMessage<?> message)
	{
		String uri = message.getPath();
		switch(((SnaMessageSubType)message.getType()
				).getSnaMessageType())
		{
			case UPDATE:
					SnaUpdateMessage m = (SnaUpdateMessage) message;
					JSONObject notification = m.getNotification();
					String location = (String) notification.opt("value");
					if(location != null)
					{
						this.updateLocation(location);
					}
				break;
			case LIFECYCLE:
					SnaLifecycleMessage l = (SnaLifecycleMessage) message;
					String type = null;
				    switch(l.getType())
				    {
						case RESOURCE_APPEARING:
						    Object initial = null;
						    Object loc = null;
							if(uri.endsWith("/admin/location") 
							&& (initial =((SnaLifecycleMessageImpl)l).get("initial"))!=null
							&& (!JSONObject.NULL.equals((loc = ((JSONObject)initial
									).opt("value")))))
							{
								this.updateLocation((String) loc);									
							} 
							type = ((SnaLifecycleMessageImpl)l).getNotification(
									).optString("type");
						case SERVICE_APPEARING:
						case PROVIDER_DISAPPEARING:
						case RESOURCE_DISAPPEARING:
						case SERVICE_DISAPPEARING:
							this.updateContent(uri, l.getType(), type);
						case PROVIDER_APPEARING:
						default:
							break;
				    }
				break;
			case ERROR:
			case RESPONSE:
			default:
				break;
		}
	}

    /**
     * @inheritDoc
     *
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
    	return super.getName().hashCode();
    }
}