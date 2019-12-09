package org.eclipse.sensinact.gateway.device.openhab.sensinact;

import java.io.IOException;
import java.util.Arrays;

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
    public static final String  ACTIVATE_DISCOVERY_PROPERTY_NAME   = "org.eclipse.sensinact.gateway.device.openhab.OpenHabDiscovery2.disabled";
   
    public static ServerLocation getServerLocation(OpenHabMediator mediator, ServiceListener serviceListener) throws IOException {
        if (isDiscoveryActivated(mediator)) {
        	String openhabServiceType = getOpenhabServiceType(mediator);
            String openhabServiceName = getOpenhabServiceName(mediator);
        	return findServerLocationUsingDiscovery(mediator, serviceListener, openhabServiceType, openhabServiceName);
        } else {
        	return loadServerLocatioFromProperties(mediator);
        }
	}

    private static ServerLocation loadServerLocatioFromProperties(OpenHabMediator mediator) {
        String openhabScheme = null;	
    	String openhabIP = null;
        int openhabPort = 0;	
    	
        mediator.warn("The openhab2 discovery service was disabled by system property. Using openhab configuration found inside conf/config.properties:");

        final String openhabSchemePropertyValue = (String) mediator.getProperty(OPENHAB_SCHEME_PROPERTY_NAME);
        if (openhabSchemePropertyValue != null) {
        	openhabScheme = openhabSchemePropertyValue;
            mediator.info("Openhab2 port configurated by %s property set to %s", OPENHAB_SCHEME_PROPERTY_NAME, openhabScheme);
        } else {
        	openhabScheme =  DEFAULT_OPENHAB_SERVICE_SSL_TYPE.startsWith(getOpenhabServiceType(mediator))?DEFAULT_OPENHAB_SSL_SCHEME:DEFAULT_OPENHAB_SCHEME;
            mediator.info("No openhab2 port configurated with %s. Using default port: ", OPENHAB_SCHEME_PROPERTY_NAME, openhabScheme);
        }
        
        final String openhabIPPropertyValue = (String) mediator.getProperty(OPENHAB_IP_PROPERTY_NAME);
        if (openhabIPPropertyValue != null) {
            openhabIP = openhabIPPropertyValue;
            mediator.info("Openhab2 ip configurated by %s property set to %s", OPENHAB_IP_PROPERTY_NAME, openhabIP);
        } else {
            openhabIP = DEFAULT_OPENHAB_IP;
            mediator.info("No openhab2 ip configurated with %s. Using default ip: %s", OPENHAB_IP_PROPERTY_NAME, openhabIP);
        }
        
        final String openhabPortPropertyValue = (String) mediator.getProperty(OPENHAB_PORT_PROPERTY_NAME);
        if (openhabPortPropertyValue != null) {
            openhabPort = Integer.parseInt(openhabPortPropertyValue);
            mediator.info("Openhab2 port configurated by %s property set to %s", OPENHAB_PORT_PROPERTY_NAME, openhabPort);
        } else {
            openhabPort = DEFAULT_OPENHAB_PORT;
            mediator.info("No openhab2 port configurated with %s. Using default port: ", OPENHAB_PORT_PROPERTY_NAME, openhabPort);
        }
        
        return new ServerLocation(openhabScheme, openhabIP, openhabPort);
	}

	private static ServerLocation findServerLocationUsingDiscovery(OpenHabMediator mediator, ServiceListener serviceListener, String openhabServiceType, String openhabServiceName) throws IOException {
    	String openhabScheme = DEFAULT_OPENHAB_SERVICE_SSL_TYPE.startsWith(openhabServiceType)?DEFAULT_OPENHAB_SSL_SCHEME:DEFAULT_OPENHAB_SCHEME;
		String openhabIP = DEFAULT_OPENHAB_IP;
        int openhabPort = DEFAULT_OPENHAB_PORT;
               
    	mediator.info("Starting openhab2 discovery...");
    	JmDNS dns = JmDNS.create();
        mediator.info("...dns created...");
        dns.addServiceListener(openhabServiceType, serviceListener);
        mediator.info("...started openhab2 discovery");
        mediator.debug("...dns searching service info for type " + openhabServiceType + "...");
        ServiceInfo[] list = dns.list(openhabServiceType);
        for (ServiceInfo service : list) {
            mediator.info("...dns found openhab2 service for type " + openhabServiceType + ": " + service.getName() + " " + service);
        }
        if (list.length == 0) {
            mediator.warn("...no service info found by dns for type " + openhabServiceType);
        }
        
        final ServiceInfo info = dns.getServiceInfo(openhabServiceType, openhabServiceName);
        if (info == null) {
            mediator.error("among the " + list.length + " found openhab2 service(s), unable to find one available with name " + openhabServiceName);
            throw new RuntimeException("unable to find any openhab2 service available with " + openhabServiceType + " type and " + openhabServiceName + " name");
        }
        openhabPort = info.getPort();
        final String[] openhabHostAddresses = info.getHostAddresses();
        if (openhabHostAddresses.length == 0) {
            LOG.error("unexpected empty openhab2 host ip address");
        } else {
            openhabIP = info.getHostAddresses()[0];
            if (openhabHostAddresses.length > 1) {
                LOG.warn("unexpected more than one address for openhab2 host: {}. Several openhab2 instances running?. Will use first {}", Arrays.asList(openhabHostAddresses), openhabIP);
            }
            LOG.debug("Openhab2 binded to ip {} and port {}", openhabIP, openhabPort);
        }        
        return new ServerLocation(openhabScheme, openhabIP, openhabPort);
    }
    
	private static String getOpenhabServiceType(OpenHabMediator mediator) {
		final String openhabServiceTypePropertyValue = (String) mediator.getProperty(OPENHAB_SERVICE_TYPE_PROPERTY_NAME);
        String openhabServiceType = DEFAULT_OPENHAB_SERVICE_TYPE;
        if (openhabServiceTypePropertyValue != null) {
            openhabServiceType = openhabServiceTypePropertyValue;
            mediator.info("Openhab2 service type configurated by %s property set to %s", OPENHAB_SERVICE_TYPE_PROPERTY_NAME, openhabServiceType);
        } else {
            mediator.info("No openhab2 service type configurated. Using default type: " + openhabServiceType);
        }
		return openhabServiceType;
	}
    
	private static String getOpenhabServiceName(OpenHabMediator mediator) {
		final String openhabServiceNamePropertyValue = (String) mediator.getProperty(OPENHAB_SERVICE_NAME_PROPERTY_NAME);
        String openhabServiceName = DEFAULT_OPENHAB_SERVICE_NAME;
        if (openhabServiceNamePropertyValue != null) {
            openhabServiceName = openhabServiceNamePropertyValue;
            mediator.info("Openhab2 service name configurated by %s property set to %s", OPENHAB_SERVICE_NAME_PROPERTY_NAME, openhabServiceName);
        } else {
            mediator.info("No openhab2 service name configurated. Using default type: " + openhabServiceName);
        }
		return openhabServiceName;
	}

	private static boolean isDiscoveryActivated(OpenHabMediator mediator) {
		final String activateDiscoveryPropertyValue = (String) mediator.getProperty(ACTIVATE_DISCOVERY_PROPERTY_NAME);
        boolean discoveryDescativated = false;
        if (activateDiscoveryPropertyValue != null) {
            discoveryDescativated = Boolean.parseBoolean(activateDiscoveryPropertyValue);
            mediator.info("Openhab2 discovery configurated by %s property set to %s", ACTIVATE_DISCOVERY_PROPERTY_NAME, discoveryDescativated);
        } else {
            mediator.info("No openhab2 discovery configurated. Default configuration is enabled...");
        }
		return ! discoveryDescativated;
	}
}
