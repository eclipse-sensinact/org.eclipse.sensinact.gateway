package org.eclipse.sensinact.gateway.device.openhab.sensinact;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.eclipse.sensinact.gateway.device.openhab.common.ServerLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenHabServerFinder {

	private static final Logger LOG = LoggerFactory.getLogger(OpenHabServerFinder.class);

    public static final String  OPENHAB_SERVICE_TYPE_PROPERTY_NAME = "org.eclipse.sensinact.gateway.device.openhab.type";
    public static final String  OPENHAB_SERVICE_NAME_PROPERTY_NAME = "org.eclipse.sensinact.gateway.device.openhab.name";
    
    public static final String  OPENHAB_SERVICE_TYPE_PROPERTY_SUFFIXES = "org.eclipse.sensinact.gateway.device.openhab.suffixes";
    public static final String  OPENHAB_SCHEME_PROPERTY_NAME       = "org.eclipse.sensinact.gateway.device.openhab.scheme";
    public static final String  OPENHAB_IP_PROPERTY_NAME           = "org.eclipse.sensinact.gateway.device.openhab.ip";
    public static final String  OPENHAB_PORT_PROPERTY_NAME         = "org.eclipse.sensinact.gateway.device.openhab.port";
    
    public static final String  DEFAULT_OPENHAB_SERVICE_TYPE       = "_openhab-server._tcp.local.";
    public static final String  DEFAULT_OPENHAB_SERVICE_SSL_TYPE   = "_openhab-server-ssl._tcp local.";    
    
    public static final String  DEFAULT_OPENHAB_SERVICE_NAME       = "openhab";   
    public static final String  DEFAULT_OPENHAB_SERVICE_SSL_NAME   = "openhab-ssl";
    
    public static final String  DEFAULT_OPENHAB_SCHEME             = "http";
    public static final String  DEFAULT_OPENHAB_SSL_SCHEME         = "https";
    
    public static final String  DEFAULT_OPENHAB_IP                 = "127.0.0.1";
    public static final int     DEFAULT_OPENHAB_PORT               = 8080;
    public static final int     DEFAULT_OPENHAB_SSL_PORT           = 8443;
    
    public static final String  ACTIVATE_DISCOVERY_PROPERTY_NAME   = "org.eclipse.sensinact.gateway.device.openhab.OpenHabDiscovery2.disabled";
   
    public static List<ServerLocation> getServerLocation(OpenHabMediator mediator, ServiceListener serviceListener) throws IOException {
        if (isDiscoveryActivated(mediator)) {
        	String openhabServiceType = getOpenhabServiceType(mediator,null);
            String openhabServiceName = getOpenhabServiceName(mediator,null);
        	return findServerLocationUsingDiscovery(mediator, serviceListener, openhabServiceType, openhabServiceName);
        } else
        	return loadServerLocatioFromProperties(mediator);
	}

    private static List<ServerLocation> loadServerLocatioFromProperties(OpenHabMediator mediator) {

        List<ServerLocation> locations = new ArrayList<ServerLocation>();
        
        String[] suffixes;
        String suffixesProp = (String) mediator.getProperty(OPENHAB_SERVICE_TYPE_PROPERTY_SUFFIXES);
        if(suffixesProp!=null) {
        	suffixes = suffixesProp.split(",");        
	        for(String suffix : suffixes) {	
	        	String openhabScheme = null;
	        	String openhabIP = null;
	        	int openhabPort=-1;
	        	
		        String openhabServiceType =  getOpenhabServiceType(mediator,suffix);

		        String prop = new StringBuilder().append(OPENHAB_SCHEME_PROPERTY_NAME).append(".").append(suffix).toString();
				
		        final String openhabSchemePropertyValue = (String) mediator.getProperty(prop);
		        if (openhabSchemePropertyValue != null)
		        	openhabScheme = openhabSchemePropertyValue;
		        else		        	
		        	openhabScheme =  DEFAULT_OPENHAB_SERVICE_SSL_TYPE.startsWith(openhabServiceType)?DEFAULT_OPENHAB_SSL_SCHEME:DEFAULT_OPENHAB_SCHEME;

		        prop = new StringBuilder().append(OPENHAB_IP_PROPERTY_NAME).append(".").append(suffix).toString();
				
		        final String openhabIPPropertyValue = (String) mediator.getProperty(prop);
		        if (openhabIPPropertyValue != null) 
		            openhabIP = openhabIPPropertyValue;
		        else
		            openhabIP = DEFAULT_OPENHAB_IP;

		        prop = new StringBuilder().append(OPENHAB_PORT_PROPERTY_NAME).append(".").append(suffix).toString();
				
		        final String openhabPortPropertyValue = (String) mediator.getProperty(prop);
		        if (openhabPortPropertyValue != null)
		            openhabPort = Integer.parseInt(openhabPortPropertyValue);
		        else
		            openhabPort = DEFAULT_OPENHAB_SERVICE_SSL_TYPE.startsWith(openhabServiceType)?DEFAULT_OPENHAB_SSL_PORT:DEFAULT_OPENHAB_PORT;

		        locations.add(new ServerLocation(openhabScheme, openhabIP, openhabPort));
	        }
	    } else {
        	String openhabScheme = null;
        	String openhabIP = null;
        	int openhabPort=-1;
        	
	    	String openhabServiceType =  getOpenhabServiceType(mediator, null);
	
	        final String openhabSchemePropertyValue = (String) mediator.getProperty(OPENHAB_SCHEME_PROPERTY_NAME);
	        if (openhabSchemePropertyValue != null)
	        	openhabScheme = openhabSchemePropertyValue;
	        else 	        	
	        	openhabScheme =  DEFAULT_OPENHAB_SERVICE_SSL_TYPE.startsWith(openhabServiceType)?DEFAULT_OPENHAB_SSL_SCHEME:DEFAULT_OPENHAB_SCHEME;
	                
	        final String openhabIPPropertyValue = (String) mediator.getProperty(OPENHAB_IP_PROPERTY_NAME);
	        if (openhabIPPropertyValue != null) 
	            openhabIP = openhabIPPropertyValue;
	        else 
	            openhabIP = DEFAULT_OPENHAB_IP;
	        	        
	        final String openhabPortPropertyValue = (String) mediator.getProperty(OPENHAB_PORT_PROPERTY_NAME);
	        if (openhabPortPropertyValue != null) 
	            openhabPort = Integer.parseInt(openhabPortPropertyValue);
	        else 
	            openhabPort = DEFAULT_OPENHAB_SERVICE_SSL_TYPE.startsWith(openhabServiceType)?DEFAULT_OPENHAB_SSL_PORT:DEFAULT_OPENHAB_PORT;
	        
	        locations.add(new ServerLocation(openhabScheme, openhabIP, openhabPort));
	    }
        return locations;
	}

	private static List<ServerLocation> findServerLocationUsingDiscovery(OpenHabMediator mediator, ServiceListener serviceListener, String openhabServiceType, String openhabServiceName) throws IOException {
    	String openhabScheme = DEFAULT_OPENHAB_SERVICE_SSL_TYPE.startsWith(openhabServiceType)?DEFAULT_OPENHAB_SSL_SCHEME:DEFAULT_OPENHAB_SCHEME;
		int openhabPort = 0;
                           
    	LOG.info("Starting openhab2 discovery...");
    	JmDNS dns = JmDNS.create();
        LOG.info("...dns created...");
        dns.addServiceListener(openhabServiceType, serviceListener);
        LOG.info("...started openhab2 discovery");
        LOG.debug("...dns searching service info for type " + openhabServiceType + "...");
        ServiceInfo[] list = dns.list(openhabServiceType);
        if (list.length == 0) {
            LOG.warn("...no service info found by dns for type " + openhabServiceType);
        }
        List<ServerLocation> locations = new ArrayList<ServerLocation>();
        if(openhabServiceName!=null) {            
            final ServiceInfo info = dns.getServiceInfo(openhabServiceType, openhabServiceName);
            if (info == null) {
                LOG.error("among the " + list.length + " found openhab2 service(s), unable to find one available with name " + openhabServiceName);
                throw new RuntimeException("unable to find any openhab2 service available with " + openhabServiceType + " type and " + openhabServiceName + " name");
            }
            openhabPort = info.getPort();
	        final String[] openhabHostAddresses = info.getHostAddresses();
	        if (openhabHostAddresses.length == 0) 
	            LOG.error("unexpected empty openhab2 host ip address");
	        else {
	            if (openhabHostAddresses.length > 1) 
	                LOG.warn("unexpected more than one address for openhab2 host: {}. Several openhab2 instances running?", 
	                		Arrays.asList(openhabHostAddresses));
	            
	            for(String ip: info.getHostAddresses()) {
	            	locations.add(new ServerLocation(openhabScheme, ip, openhabPort));
	            	LOG.debug("Openhab2 binded to ip {} and port {}", ip, openhabPort);
	            }
	        }
        }  else {      
		    for (ServiceInfo service : list) {
		         LOG.info("...dns found openhab2 service for type " + openhabServiceType + ": " + service.getName() + " " + service);
		         openhabPort = service.getPort();
		         final String[] openhabHostAddresses = service.getHostAddresses();
		         if (openhabHostAddresses.length == 0) 
		            LOG.error("unexpected empty openhab2 host ip address");
		         else {
		        	if (openhabHostAddresses.length > 1) 
			            LOG.warn("unexpected more than one address for openhab2 host: {}. Several openhab2 instances running?", 
			                	Arrays.asList(openhabHostAddresses));
			            
			        for(String ip: service.getHostAddresses()) {
			            locations.add(new ServerLocation(openhabScheme, ip, openhabPort));
			            	LOG.debug("Openhab2 binded to ip {} and port {}", ip, openhabPort);
			        }
		         } 
		    }
        }
        return locations;
    }
    
	private static String getOpenhabServiceType(OpenHabMediator mediator, String suffix) {
		String prop = null;
		if(suffix==null)
			prop = OPENHAB_SERVICE_TYPE_PROPERTY_NAME;
		else 
			prop = new StringBuilder().append(OPENHAB_SERVICE_TYPE_PROPERTY_NAME).append(".").append(suffix).toString();
		final String openhabServiceTypePropertyValue = (String) mediator.getProperty(prop);
        String openhabServiceType = null;
        if (openhabServiceTypePropertyValue != null) {
            openhabServiceType = openhabServiceTypePropertyValue;
            LOG.info("Openhab2 service type configurated by {} property set to {}", OPENHAB_SERVICE_TYPE_PROPERTY_NAME, openhabServiceType);
        } else {
        	openhabServiceType = DEFAULT_OPENHAB_SERVICE_TYPE;
            LOG.info("No openhab2 service type configurated. Using default type: " + openhabServiceType);
        }
		return openhabServiceType;
	}
    
	private static String getOpenhabServiceName(OpenHabMediator mediator, String suffix) {
		String prop = null;
		if(suffix==null)
			prop = OPENHAB_SERVICE_NAME_PROPERTY_NAME;
		else 
			prop = new StringBuilder().append(OPENHAB_SERVICE_NAME_PROPERTY_NAME).append(".").append(suffix).toString();
		final String openhabServiceNamePropertyValue = (String) mediator.getProperty(prop);
        String openhabServiceName = null;
        if (openhabServiceNamePropertyValue != null) {
            openhabServiceName = openhabServiceNamePropertyValue;
            LOG.info("Openhab2 service name configurated by {} property set to {}", OPENHAB_SERVICE_NAME_PROPERTY_NAME, openhabServiceName);
        } else {
        	openhabServiceName = DEFAULT_OPENHAB_SERVICE_SSL_TYPE.startsWith(getOpenhabServiceType(mediator,suffix))?DEFAULT_OPENHAB_SERVICE_SSL_NAME:DEFAULT_OPENHAB_SERVICE_NAME;
            LOG.info("No openhab2 service name configurated. Using default type: " + openhabServiceName);
        }
		return openhabServiceName;
	}

	private static boolean isDiscoveryActivated(OpenHabMediator mediator) {
		final String activateDiscoveryPropertyValue = (String) mediator.getProperty(ACTIVATE_DISCOVERY_PROPERTY_NAME);
        boolean discoveryDescativated = false;
        if (activateDiscoveryPropertyValue != null) {
            discoveryDescativated = Boolean.parseBoolean(activateDiscoveryPropertyValue);
            LOG.info("Openhab2 discovery configurated by {} property set to {}", ACTIVATE_DISCOVERY_PROPERTY_NAME, discoveryDescativated);
        } else {
            LOG.info("No openhab2 discovery configurated. Default configuration is enabled...");
        }
		return !discoveryDescativated;
	}
}