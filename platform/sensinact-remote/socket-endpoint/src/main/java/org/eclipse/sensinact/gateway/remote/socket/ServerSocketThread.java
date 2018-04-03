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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * The ServerSocketThread is the recipient of the remote connected
 * SocketEndpoint's client requests
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
class ServerSocketThread implements Runnable
{
	private volatile boolean running;

	private ServerSocket server = null;
	protected Mediator mediator;
	private SocketEndpoint endpoint;
	
	private InetAddress localAddress;
	private int localPort;

	private SocketHolder holder;

	
	ServerSocketThread(Mediator mediator, SocketEndpoint endpoint)
			throws IOException
	{
		this.mediator = mediator;
		this.endpoint = endpoint;
		
		this.localAddress = InetAddress.getByName(
				endpoint.getLocalAddress());
		this.localPort = endpoint.getLocalPort();		
	}

	@Override
	public void run() 
	{
		try 
		{
			this.server = this.createServer();
			mediator.debug("ServerSocket initialized... wait for socket connection");
			
		} catch (IOException e)
		{
			mediator.error(e);
			
		} finally
		{
			if(checkServerStatus())
			{
				run(server);
			}
		}
	}	
	
	private void run(ServerSocket server) 
	{
		try 
		{
			Socket socket = server.accept();
			this.holder = new SocketHolder(mediator,socket);
			mediator.debug("%s: Socket initialized", this);
			
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
			JSONObject request = null;
			try
			{
				request = this.holder.read();			
				JSONObject response = null;
				if(request != null)
				{
					response = this.endpoint.incomingRequest(request);	
				}
				if(response == null)
				{
					response = new JSONObject();
					response.put("statusCode", 520);
					response.put("response" , 
						new JSONObject().put("message",
						"Unable to process the request"));
				}
				this.holder.write(response);
				
			} catch(SocketException e)
			{
				mediator.error(e);
				//the broken pipe exception does not conduct
				//into an invalid socket status - So we enforce
				//the loop exit
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
		boolean currentlyRunning = this.running;
		this.running = false;
		if(!checkServerStatus()||!currentlyRunning)
		{
			closeServer(server);
			this.endpoint.serverStopped();
			return;
		}
		this.endpoint.serverStopped();
		run(server);
	}
	
	private ServerSocket createServer() throws IOException
	{
		ServerSocket server = new ServerSocket();
		server.setReuseAddress(true);
		server.bind(new InetSocketAddress(localAddress,localPort));
		mediator.info("Binding server socket on %s:%s",
			endpoint.getLocalAddress(),endpoint.getLocalPort());
		return server;
	}

	protected boolean running()
	{
		return this.running;
	}

	public void stop()
	{
		this.running = false;
		if(this.holder!=null)
		{
			this.holder.close();
		}
	}

	private boolean checkServerStatus()
	{
		if(server==null || !server.isBound() || server.isClosed())
		{
			return false;
		}
		return true;
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
	
	private void closeServer(ServerSocket server)
	{
		if(server != null && !server.isClosed())
		{
			try 
			{
				server.close();
				
			} catch (IOException e)
			{
				mediator.error(e);
			}
		}
	}
}