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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.json.JSONObject;


/**
 * The ServerSocketThread is the recipient of the remote connected
 * SocketEndpoint's client requests
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
class ServerSocketThread implements Runnable
{
	private static boolean checkSocketStatus(
			Socket socket)
	{
		if(socket==null || !socket.isConnected())
		{
			return false;
		}
		return (!socket.isClosed() && 
				!socket.isInputShutdown() && 
				!socket.isOutputShutdown());
	}

	private static boolean checkServerStatus(ServerSocket server)
	{
		if(server==null || !server.isBound() || server.isClosed())
		{
			return false;
		}
		return true;
	}
	
	private volatile boolean running;
	
	protected Mediator mediator;
	private SocketEndpoint endpoint;
	
	private InetAddress localAddress;
	private int localPort;

	
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
		ServerSocket server = null;
		try 
		{
			server = this.createServer();
			mediator.debug("ServerSocket initialized... wait for socket connection");
			
		} catch (IOException e)
		{
			mediator.error(e);
			
		} finally
		{
			if(checkServerStatus(server))
			{
				run(server);
			}
		}
	}	
	
	private void run(ServerSocket server) 
	{
		Socket socket = null; 
		InputStream input = null;
		OutputStream output = null;		
		try 
		{
			socket = server.accept();
			output = socket.getOutputStream();
			input = socket.getInputStream();
			mediator.debug("%s: Socket initialized", this);
			
		} catch (NullPointerException |IOException e)
		{
			mediator.error(e);
			
		} finally
		{
			if(checkSocketStatus(socket))
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
				String strContent = new String(content);
				object = new JSONObject(strContent);
				
			} catch(Exception e)
			{
				mediator.error(e);
				if(!checkSocketStatus(socket))
				{
					break;
				}
			} 
			JSONObject response = null;
			if(object != null)
			{
				response = this.endpoint.incomingRequest(object);	
			}
			if(response == null)
			{
				response = new JSONObject();
				response.put("statusCode", 520);
				response.put("response" , new JSONObject().put("message",
					"Unable to process the request"));
			}
			int block = SocketEndpoint.BUFFER_SIZE;
			byte[] data = response.toString().getBytes();
			int written = 0;
			length = data==null?0:data.length;			
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
				mediator.error(e);
				if(!checkSocketStatus(socket))
				{
					break;
				}
			}
		}
		closeSocket(socket);
		if(!this.running || !checkServerStatus(server))
		{
			closeServer(server);
			this.running = false;
			this.endpoint.serverStopped();
			return;
		}
		this.running = false;
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
	
	protected void stop()
	{
		this.running = false;
	}
	
	private void closeSocket(Socket socket)
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