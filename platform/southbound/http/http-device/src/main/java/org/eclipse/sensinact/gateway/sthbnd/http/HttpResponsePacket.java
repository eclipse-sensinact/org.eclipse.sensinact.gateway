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
package org.eclipse.sensinact.gateway.sthbnd.http;

import java.io.InputStream;

import org.eclipse.sensinact.gateway.generic.Task.CommandType;

/**
 * Extended {@link HttpPacket} wrapping an HTTP response message
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class HttpResponsePacket extends HttpPacket {
	
    protected CommandType command;
    protected int statusCode = -1;
    protected String path;
    protected String savedContent;
	protected boolean consume;
	
	private HttpResponse response;

    /**
     * Constructor
     * 
     * @param response
     */
    public HttpResponsePacket(HttpResponse response) {
    	this(response, false, true);
    	this.savedContent = null;
    }

    /**
     * Constructor
     * 
     * @param response
     * @param save
     */
    public HttpResponsePacket(HttpResponse response, boolean save, boolean consume) {
        super((response != null && !save && consume)?response.getContent():new byte[0]);
        if(save)
        	this.savedContent = response.save();  
        if(!consume)
        	this.response = response;
        this.consume = consume;
        this.setStatusCode(statusCode);
        this.path = response.getPath();
        this.command = response.getCommand();
        super.addHeaders(response.getHeaders());
    }
    
    /**
     * @return
     */
    public CommandType getCommand() {
        return this.command;
    }

    /**
     * @return
     */
    public String getPath() {
        return this.path;
    }
    
    /**
     * Returns the wrapped {@link HttpResponse}'s {@link InputStream} if any
     * 
     * @return the wrapped {@link HttpResponse}'s {@link InputStream}
     */
    public InputStream getInputStream() {
    	if(!consume && this.response!=null)
    		return this.response.inputStream();
    	return null;
    }

    /**
     * Defines the integer status of the wrapped HTTP message
     *
     * @param statusCode the integer status of the wrapped HTTP message
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Returns the integer status of the wrapped
     * HTTP message
     *
     * @return the integer status of the wrapped
     * HTTP message
     */
    public int getStatusCode() {
        return this.statusCode;
    }
    
    @Override
    public void finalize() {
    	if(this.response != null)
    		this.response.disconnect();
    	this.response = null;
    	
    }
}
