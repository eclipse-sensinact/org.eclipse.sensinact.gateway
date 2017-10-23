package org.eclipse.sensinact.gateway.remote.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

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
	private boolean running;
	private Socket socket = null;
    private ServerSocket server;

	private OutputStream output;
	private InputStream input;
	
	protected Mediator mediator;
	private SocketEndpoint endpoint;

	
	ServerSocketThread(Mediator mediator, SocketEndpoint endpoint)
			throws IOException
	{
		this.mediator = mediator;
		this.endpoint = endpoint;
		
		InetAddress localAddress = InetAddress.getByName(endpoint.getLocalAddress());
		int localPort = endpoint.getLocalPort();
		
		System.out.println(String.format("Binding server socket on %s:%s",
			endpoint.getLocalAddress(),endpoint.getLocalPort()));
		
		this.server = new ServerSocket(localPort,0,localAddress);
	}
	
	Socket createSocket() throws IOException
	{
		socket = server.accept();
		return socket;	
	}
	
	void stop()
	{
		this.running = false;
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
			mediator.error(e.getMessage(),e);
			return;
		}
		this.running = true;
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
			    this.mediator.error(e.getMessage(),e);
			    if(!socket.isConnected())
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
				response.put("response" , 
					new JSONObject().put("message",
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
			} catch(SocketException e)
			{
				if("Broken pipe".equals(e.getMessage()))
				{
					break;
				}
			}
			catch(IOException e)
			{
				this.mediator.error(e.getMessage(),e);
			} 
		} 
		if(socket!=null && socket.isConnected())
		{
			try 
			{
				socket.shutdownInput();
				socket.shutdownOutput();
				socket.close();
				
			} catch (IOException e)
			{
				mediator.error(e.getMessage(),e);
			}
		}
		if(server != null && !server.isClosed())
		{
			try 
			{
				server.close();
				
			} catch (IOException e)
			{
				mediator.error(e.getMessage(),e);
			}
		}
	}	
}