/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */

package org.eclipse.sensinact.gateway.remote.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * The ClientSocketThread is in charge of sending requests to 
 * the remote connected SocketEndpoint's server
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
class ClientSocketThread implements Runnable
{
	private boolean running;
	private Socket socket = null;
	private OutputStream output;
	private InputStream input;
	
	private Map<String, String> requests;
	protected Mediator mediator;
	
	private InetAddress remoteAddress;
	private int remotePort;
	private SocketEndpoint endpoint;
	
	ClientSocketThread(Mediator mediator, SocketEndpoint endpoint, 
		String address, int port) throws IOException
	{
		this.mediator = mediator;
		this.endpoint = endpoint;
		
		this.requests = new HashMap<String, String>();
		this.remoteAddress = InetAddress.getByName(address);
		this.remotePort = port;
	}
	
	private Socket createSocket() throws IOException
	{
		socket = new Socket(remoteAddress,remotePort);
		return socket;
	}
	
	protected void stop()
	{
		this.running = false;
	}

	protected boolean running()
	{
		return this.running;
	}	
	
	protected String request(JSONObject object)
	{
		String result = null;
		if(!running)
		{
			return result;
		}
		long timestamp = System.currentTimeMillis() 
			+ this.hashCode();
		
		String uuid = new StringBuilder().append(
				"edpnt").append(timestamp
					).toString();
		
		object.put("uuid",uuid);
		byte[] data = object.toString().getBytes();
		int written = 0;
		int length = data==null?0:data.length;
		int block = SocketEndpoint.BUFFER_SIZE;
		try
		{
			while(written < length)
			{
				if((written+block) > length)
				{
					block = length-written;
				}
				output.write(data, written, block);
				written+=block;
			}
			output.write(new byte[]{'\0'});
			output.flush();
			
		} catch(IOException e)
		{
			this.mediator.error(e);
			if(!checkSocketStatus())
			{
				return result;
			}
		}
		long wait = 5000;
		while(wait > 0)
		{
			synchronized(this.requests)
			{
				if(this.requests.containsKey(uuid))
				{
					result = this.requests.get(uuid);
					break;
				}
			}
			try
			{
				wait-=100;
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				Thread.interrupted();
				break;
			}
		}
		return result;
	}
	
	@Override
	public void run() 
	{
		try 
		{
			this.socket = createSocket();
			this.output = this.socket.getOutputStream();
			this.input = this.socket.getInputStream();
			
		} catch (IOException e)
		{
		   mediator.error(e);
		   
		} finally
		{
		   if(this.checkSocketStatus())
		   {
			   this.running = true;
		   }
		}		
		while(running)
		{
			JSONObject object = null;
			int read = 0;
			int length = 0;
			byte[] content = new byte[length];
			byte[] buffer = new byte[SocketEndpoint.BUFFER_SIZE];			
			try
			{
				boolean eof = false;
				while((read = input.read(buffer))>-1)
				{
					eof = (buffer[read-1]=='\0');
					byte[] newContent = new byte[length+read];
					if(length > 0)
					{
						System.arraycopy(content, 0, newContent, 0, length);
					}
					System.arraycopy(buffer, 0, newContent, length, eof?read-1:read);					
					content = newContent;
					newContent = null;
					length+=(eof?read-1:read);
					if(eof)
					{
						break;
					}
				}
				if(content.length > 0)
				{
					object = new JSONObject(new String(content));
					if(JSONObject.NULL.equals(object))
					{
						continue;
					}
					String uuid = (String) object.remove("uuid");
					synchronized( this.requests)
					{
						if(uuid !=null)
						{
							this.requests.put(uuid, 
								object.optString("response"));
						}
					}
				} 
			} catch(IOException | JSONException e)
			{
			   mediator.error(e);
			   
			} finally
			{
				if(!this.checkSocketStatus())
				{
					break;
				}
			}
		} 
		closeSocket();
		this.running = false;
		this.endpoint.clientDisconnected();
	}

	private boolean checkSocketStatus()
	{
		if(socket==null || !socket.isConnected())
		{
			return false;
		}
		return (!socket.isClosed() && 
				!socket.isInputShutdown() && 
				!socket.isOutputShutdown());
	}

	private void closeSocket()
	{
		if(socket!=null && socket.isConnected())
		{
			try 
			{
				socket.close();
				
			} catch (IOException e)
			{
				mediator.error(e);
			}
		}
	}
}