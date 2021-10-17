/**
 * 
 */
package org.eclipse.sensinact.gateway.sthbnd.http.factory.endpoint;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.ProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.protocol.http.client.Request;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.sthbnd.http.SimpleHttpResponse;
import org.eclipse.sensinact.gateway.sthbnd.http.factory.packet.HttpMappingPacket;
import org.eclipse.sensinact.gateway.sthbnd.http.factory.packet.HttpNestedMappingPacket;
import org.eclipse.sensinact.gateway.sthbnd.http.factory.packet.HttpRootMappingPacket;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpMediator;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.SimpleHttpProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;
import org.eclipse.sensinact.gateway.sthbnd.http.task.config.MappingDescription;
import org.xml.sax.SAXException;

/**
 * Extended {@link HttpProtocolStackEndpoint} 
 */
public class HttpMappingProtocolStackEndpoint extends SimpleHttpProtocolStackEndpoint {

	private String serviceProviderIdPattern;

	/**
	 * Constructor
	 * 
	 * @param mediator the {@link Mediator} allowing the {@link 
	 * ProtocolStackEndpoint} to be instantiated to interact with the OSGi
	 * host environment
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public HttpMappingProtocolStackEndpoint(HttpMediator mediator)
			throws ParserConfigurationException, SAXException, IOException {
		super(mediator);
	}

	public void setServiceProviderIdPattern(String serviceProviderIdPattern) {
		this.serviceProviderIdPattern = serviceProviderIdPattern;
	}
	
    @Override
    public void send(Task task) {
        HttpTask<?,?> _task =  (HttpTask<?,?>)task;
        try {
        	((HttpMediator) mediator).configure(_task);        
	        _task.addHeaders(super.permanentHeaders.getHeaders());
	        
	        MappingDescription[] mappings = _task.getMapping();
	        if(mappings == null || mappings.length==0) 
	        	_task.setDirect(true);
	        else {
	        	Class<? extends HttpMappingPacket> clazz = null;
	        	switch(mappings[0].getMappingType()) {
        		case MappingDescription.ROOT:
        			clazz = HttpRootMappingPacket.class;
        			break;
        		case MappingDescription.NESTED:
        			clazz = HttpNestedMappingPacket.class;
        			break;
	        	}
	        	if(_task.getPacketType()==null || ( _task.getPacketType()!=null 
	        			&& !clazz.isAssignableFrom(_task.getPacketType())))
	        		_task.setPacketType(clazz);
	        	_task.setDirect(false);
	        }  
            Request<SimpleHttpResponse> request = (Request<SimpleHttpResponse>)_task.build();
            SimpleHttpResponse response = request.send();
            
            if (response == null) {
                mediator.error("Unable to connect");
                return;
            }
            if (!_task.isDirect()) {
                HttpPacket packet = response.createPacket();  
                ((HttpMappingPacket) packet).setMapping(mappings);
                
                ((HttpMappingPacket) packet).setServiceProviderIdPattern(serviceProviderIdPattern);
                
            	if(_task.getCommand()!=null) {
            		String serviceProviderId = ((HttpMediator)super.mediator).resolve(
            			_task, "task.serviceProvider");
            		if(serviceProviderId!=null)
            			((HttpMappingPacket) packet).setServiceProviderId(serviceProviderId);
            	}
                this.process(packet);
                packet = null;
            } else 
            	_task.setResult(new String(response.getContent()));            
        } catch (Exception e) {
            super.mediator.error(e);
        } finally {
            ((HttpMediator) mediator).unregisterProcessingContext(_task);
        }
    }
}
