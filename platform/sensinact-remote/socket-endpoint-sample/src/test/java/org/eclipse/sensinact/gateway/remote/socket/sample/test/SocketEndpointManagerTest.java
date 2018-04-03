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

package org.eclipse.sensinact.gateway.remote.socket.sample.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.util.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class SocketEndpointManagerTest
{
	//********************************************************************//
	//						NESTED DECLARATIONS			  			      //
	//********************************************************************//
	
	//********************************************************************//
	//						ABSTRACT DECLARATIONS						  //
	//********************************************************************//

	//********************************************************************//
	//						STATIC DECLARATIONS							  //
	//********************************************************************//

	private static final int INSTANCES_COUNT = 3;
	
	//********************************************************************//
	//						INSTANCE DECLARATIONS						  //
	//********************************************************************//

	/**
	 * @throws Exception
	 */
	@Test
	public void socketEndpointManagerTest() throws Throwable
	{
		List<MidOSGiTestExtended> instances = new ArrayList<MidOSGiTestExtended>();
		
		for(int  n = 1;n <= INSTANCES_COUNT; n++)
		{
			MidOSGiTestExtended t = new MidOSGiTestExtended(n);
			instances.add(t);
			t.init();
			if(n==3)
			{
				t.registerAgent();
			}
		}
		for(int  n = 1;n <= INSTANCES_COUNT; n++)
		{
		    Thread.sleep(2*1000);
		    
		    FileInputStream input = new FileInputStream(
		    	new File(String.format("src/test/resources/conf%s/socket.endpoint.sample.cfg",
		    			n)));
		    
		    byte[] content = IOUtils.read(input);
		    byte[] contentPlus = new byte[content.length+1];
		    
		    System.arraycopy(content, 0, contentPlus, 0, content.length);
		    contentPlus[content.length] = '\n';
		    
		    FileOutputStream output = new FileOutputStream(
			    new File(String.format("target/felix/conf%s/socket.endpoint.sample.config",
			    		n)));		    
		    IOUtils.write(contentPlus, output);
		}		
		Thread.sleep(20*1000);

		String s = instances.get(0).providers();
		System.out.println(s);
		
		JSONObject j = new JSONObject(s);
		JSONAssert.assertEquals(new JSONArray(
		"[\"slider\",\"light\",\"sna3:slider\",\"sna3:light\",\"sna2:slider\",\"sna2:light\"]"),
		j.getJSONArray("providers"), false);
		
		instances.get(1).moveSlider(0);		
		s = instances.get(0).get("sna2:slider", "cursor", "position");
		j = new JSONObject(s);
		
		assertEquals(0,j.getJSONObject("response").getInt("value"));
		
		final Stack<SnaMessage<?>> stack = new Stack<SnaMessage<?>>();
		
		s = instances.get(0).subscribe("sna2:slider", "cursor", "position", 
		new Recipient()
		{
			@Override
			public String getJSON()
			{
				return null;
			}
			
			@Override
			public void callback(String callbackId, 
				SnaMessage[] messages) throws Exception
			{
				synchronized(stack)
				{
					stack.push(messages[0]);
				}
			}
		});			
		Thread.sleep(2000);
		
		instances.get(1).moveSlider(45);		
		s = instances.get(0).get("sna2:slider", "cursor", "position");
		j = new JSONObject(s);
		
		assertEquals(45,j.getJSONObject("response").getInt("value"));
		JSONObject message = null;
		
		Thread.sleep(2000);
		synchronized(stack)
		{
			assertEquals(1, stack.size());
			message= new JSONObject(((SnaMessage)stack.peek()).getJSON());
			assertEquals(45, message.getJSONObject("notification").getInt("value"));
		}		
		instances.get(1).moveSlider(150);		
		s = instances.get(0).get("sna2:slider", "cursor", "position");
		j = new JSONObject(s);
		
		assertEquals(150,j.getJSONObject("response").getInt("value"));

		Thread.sleep(2000);
		synchronized(stack)
		{
			assertEquals(2, stack.size());
			message = new JSONObject(((SnaMessage)stack.peek()).getJSON());
			assertEquals(150, message.getJSONObject("notification").getInt("value"));
		}		
		int count = 0;
		for(String ms:instances.get(2).listAgentMessages())
		{
			if(ms.contains("ATTRIBUTE_VALUE_UPDATED"))
			{
				count++;
			}
		}
		assertEquals(3,count);
		instances.get(2).stop();
		
		//check that the remote core is no more registered
		s = instances.get(0).providers();
		System.out.println(s);
		
		j = new JSONObject(s);
		JSONAssert.assertEquals(new JSONArray(
		"[\"slider\",\"light\",\"sna2:slider\",\"sna2:light\"]"),
		j.getJSONArray("providers"), false);
		
		//check that the agent is unregistered
		instances.get(1).moveSlider(375);		
		s = instances.get(0).get("sna2:slider", "cursor", "position");	
		
		j = new JSONObject(s);		
		assertEquals(375,j.getJSONObject("response").getInt("value"));
		
		Thread.sleep(5000);		
		count = 0;
		for(String ms:instances.get(2).listAgentMessages())
		{
			if(ms.contains("ATTRIBUTE_VALUE_UPDATED"))
			{
				count++;
			}
		}
		assertEquals(0,count);
		
		
		File f = new File(String.format(
			"target/felix/conf%s/socket.endpoint.sample.config",3));
		
		f.delete();
	    Thread.sleep(2000);
	    
	    //now lets try to reconnect
	    FileInputStream input = new FileInputStream(new File(
	    String.format("src/test/resources/conf%s/socket.endpoint.sample.cfg",3)));
	    
	    byte[] content = IOUtils.read(input);
	    byte[] contentPlus = new byte[content.length+1];
	    
	    System.arraycopy(content, 0, contentPlus, 0, content.length);
	    contentPlus[content.length] = '\n';
	    
	    FileOutputStream output = new FileOutputStream(f);		    
	    IOUtils.write(contentPlus, output);

		Thread.sleep(20*1000);
				
		s = instances.get(0).providers();
		System.out.println(s);
		
		j = new JSONObject(s);
		JSONAssert.assertEquals(new JSONArray(
		"[\"slider\",\"light\",\"sna2:slider\",\"sna2:light\",\"sna3:slider\",\"sna3:light\"]"),
		j.getJSONArray("providers"), false);

		instances.get(1).moveSlider(350);		
		s = instances.get(0).get("sna2:slider", "cursor", "position");	
		
		j = new JSONObject(s);		
		assertEquals(350,j.getJSONObject("response").getInt("value"));
		
		Thread.sleep(5000);		
		count = 0;
		for(String ms:instances.get(2).listAgentMessages())
		{
			if(ms.contains("ATTRIBUTE_VALUE_UPDATED"))
			{
				count++;
			}
		}
		assertEquals(1,count);
		
	    while(instances.size() > 0)
	    {
		   instances.remove(0).tearDown();
	    }
	}
}
