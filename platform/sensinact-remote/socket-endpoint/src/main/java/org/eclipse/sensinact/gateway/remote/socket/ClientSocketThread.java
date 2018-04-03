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
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
	private SocketHolder holder = null;
	
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
	
	protected void stop()
	{
		this.running = false;
		this.close();
	}

	protected boolean running()
	{
		return this.running;
	}	

	private boolean checkStatus()
	{
		return this.holder!=null && 
			this.holder.checkSocketStatus();
	}

	private void close()
	{
		if(this.holder != null)
		{
			this.holder.close();
		}
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
		try
		{
			this.holder.write(object);
			
		} catch(IOException e)
		{
			this.mediator.error(e);
			if(!this.holder.checkSocketStatus())
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
			Socket s = new Socket();
			s.setReuseAddress(true);
			s.connect(new InetSocketAddress(remoteAddress,remotePort));
			this.holder = new SocketHolder(mediator,s);
			
		} catch (IOException e)
		{
		   mediator.error(e);
		   
		} finally
		{
		   if(checkStatus())
		   {
			   this.running = true;
		   }
		}
		while(running)
		{
			JSONObject object = null;				
			try
			{
				object = this.holder.read();
				if(!JSONObject.NULL.equals(object))
				{
					String uuid = (String) object.remove("uuid");
					synchronized(this.requests)
					{
						if(uuid !=null)
						{
							this.requests.put(uuid, 
								object.optString("response"));
						}
					} 
				}
			} catch(SocketException e)
			{
				mediator.error(e);
				break;
				   
			} catch(IOException | JSONException e)
			{
				mediator.error(e);
				   
			} finally
			{
				if(!this.checkStatus())
				{
					break;
				}
			}
		} 
		this.close();
		if(running)
		{
			this.running = false;
			this.endpoint.clientDisconnected();
		}
	}
}