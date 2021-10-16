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
package org.eclipse.sensinact.gateway.protocol.http.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.gateway.protocol.http.HeadersCollection;
import org.eclipse.sensinact.gateway.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Http Response
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class SimpleResponse extends HeadersCollection implements Response {
	
	private static final Logger LOG = LoggerFactory.getLogger(SimpleResponse.class);
	
	private static final int BUFFER_SIZE = 1024*64;
	
    protected int responseCode = -1;
    protected byte[] content = null;
    protected File save = null;
    protected HttpURLConnection connection;
    protected List<Throwable> exceptions;

    public SimpleResponse(HttpURLConnection connection) throws IOException {
        super(connection.getHeaderFields());
        this.exceptions = new ArrayList<Throwable>();
        this.responseCode = connection.getResponseCode();
        this.connection = connection;
    }

    //TODO: allow to check catched exceptions
    //map exceptions to the different lifecycle steps
    private void addException(Exception e) {
        this.exceptions.add(e);
    }

    /**
     * Returns the bytes array content of the wrapped
     * HTTP response
     *
     * @return the bytes array content of the wrapped
     * HTTP response
     */
    public byte[] getContent()  {
    	if(this.content == null && this.connection!=null) {
			String contentLength = super.getHeaderAsString("Content-Length");
            int length;
            try {
                length = contentLength == null ? 0 : Integer.parseInt(contentLength.trim());
            } catch (NumberFormatException e) {
                length = 0;
            }
            byte[] bytes = null;
            InputStream input = null;
			try {
				input = this.connection.getInputStream();
	            bytes = length == 0 ? IOUtils.read(input, false) : IOUtils.read(input, length, false);
	            length = bytes == null ? 0 : bytes.length;
	            this.content = bytes;
			} catch (IOException e) {
				LOG.error(e.getMessage(),e);
			} finally {			
				if(input!=null) {
					try {
						input.close();
					} catch (IOException ex) {
						LOG.error(ex.getMessage(),ex);
					}
					input = null;
				}
			}
			this.connection.disconnect();
			this.connection = null;
    	}
        return this.content;
    }
    
    /**
     * Save the content of the wrapped HTTP response as a temporary file 
     * and its String absolute path
     *
     * @return the  String absolute path of the temporary file used to save the
     * content of the wrapped HTTP response
     */
    public String save()  {
    	if(this.save == null && this.connection!=null) {
	    	byte[] buffer = new byte[BUFFER_SIZE];    		
	    	File tmpFolder = null;	    	
    		FileOutputStream output = null;
			InputStream input = null;				
    		try {
	    		File tmpFile = File.createTempFile("response", ".tmp");
	    		tmpFile.delete();
	    		tmpFolder = tmpFile.getParentFile();
	    		String filename = String.format("res_%s_%s", this.connection.hashCode(), System.currentTimeMillis());
	    		File tmpsave = new File(tmpFolder, filename);
				output = new FileOutputStream(tmpsave);
				input = this.connection.getInputStream();
	    		while(true) {
	    			int read = input.read(buffer);
	    			if(read < 0)
	    				break;
	    			output.write(buffer, 0, read);
	    		}
	    		this.save = tmpsave;
			} catch (IOException e) {
				LOG.error(e.getMessage(),e);
			} finally {
				if(input != null) {
					try {
						input.close();
					} catch (IOException e) {
						LOG.error(e.getMessage(),e);
					} finally {
						this.connection.disconnect();
						this.connection = null;
					}
				}
				if(output != null) {
					try {
						output.close();
					} catch (IOException e) {
						LOG.error(e.getMessage(),e);
					}
				}
			}
    	}
    	return save==null?null:save.getAbsolutePath();
    }
    
    /**
     * Return the {@link InputStream} of the wrapped HTTP response 
     *
     * @return the {@link InputStream} of the wrapped HTTP response
     */
    public InputStream inputStream()  {
    	try {
			return this.connection.getInputStream();
		} catch (IOException | NullPointerException e) {
			LOG.error(e.getMessage(),e);
		}
    	return null;
    }

    /**
     * Returns the HTTP code of the wrapped
     * HTTP response
     *
     * @return the wrapped HTTP response's code
     */
    public int getStatusCode() {
        return this.responseCode;
    }
    
    public void disconnect() {
    	if(this.connection!=null) {
    		this.connection.disconnect();
    		this.connection = null;
    	}
    }
}
