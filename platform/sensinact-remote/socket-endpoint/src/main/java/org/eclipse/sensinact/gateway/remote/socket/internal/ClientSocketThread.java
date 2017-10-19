package org.eclipse.sensinact.gateway.remote.socket.internal;

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
	
	private Map<String, JSONObject> requests;
	protected Mediator mediator;
	private InetAddress remoteAddress;
	private int remotePort;
	
	ClientSocketThread(Mediator mediator)
			throws IOException
	{
		this.mediator = mediator;
		this.requests = new HashMap<String, JSONObject>();

		String rportProp = (String) mediator.getProperty(
				"fr.cea.sna.gateway.endpoint.remote.port");
		String raddressProp = (String) mediator.getProperty(
				"fr.cea.sna.gateway.endpoint.remote.address");
	
		int port = 0;		
		try
		{
			port = rportProp==null?80:Integer.parseInt(rportProp);
			
		} catch(NumberFormatException e)
		{
			port = 80;
		}
		InetAddress address = null;
		if(raddressProp != null)
		{
			address = InetAddress.getByName(raddressProp);
		}
		this.remoteAddress = address;
		this.remotePort = port;
	}
	
	Socket createSocket() throws IOException
	{
		socket = new Socket(remoteAddress,remotePort);
		return socket;
	}
	
	void stop()
	{
		this.running = false;
	}

	public boolean running()
	{
		return this.running;
	}	
	
	JSONObject request(JSONObject object)
	{
		if(!running)
		{
			return null;
		}
		long timestamp = 
			System.currentTimeMillis() 
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
			this.mediator.error(e.getMessage(),e);
		} 
		JSONObject result = null;
		long wait = 5000;
		while(wait > 0)
		{
			synchronized( this.requests)
			{
				if((result = this.requests.get(uuid))!=null)
				{
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
			mediator.error(e.getMessage(),e);
			throw new RuntimeException(e);
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
				if(content.length > 0)
				{
					object = new JSONObject(new String(content));
					
				} else if(!socket.isConnected())
				{
					break;
				}
			} catch(SocketException e)
			{
				if("Broken pipe".equals(e.getMessage()))
				{
					break;
				}
			} catch(IOException e)
			{
			    this.mediator.error(e.getMessage(),e);

			} catch(JSONException e)
			{
			    this.mediator.error("Unable to parse JSON formated data :"
			    		+ new String(content));
			}
			if(object != null)
			{
				String uuid = null;
				synchronized( this.requests)
				{
					if((uuid = (String) object.remove("uuid"))!=null)
					{
						this.requests.put(uuid, object);
					}
				}
			}
		} 
		if(this.running)
		{
			this.running = false;
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
	}
}