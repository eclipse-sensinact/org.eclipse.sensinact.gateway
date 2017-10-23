package org.eclipse.sensinact.gateway.remote.socket.sample.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

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
	public void socketEndpointManagerTest() throws Exception
	{
		List<MidOSGiTestExtended> instances = new ArrayList<MidOSGiTestExtended>();
		
		for(int  n = 1;n <= INSTANCES_COUNT; n++)
		{
			MidOSGiTestExtended t = new MidOSGiTestExtended(n);
			instances.add(t);
			t.init();
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
		Thread.sleep(60*1000);

		String s = instances.get(0).providers();
		System.out.println(s);
		
		JSONObject j = new JSONObject(s);
		JSONAssert.assertEquals(new JSONArray(
		"[\"slider\",\"light\",\"sna3:slider\",\"sna3:light\",\"sna2:slider\",\"sna2:light\"]"),
		j.getJSONArray("providers"), false);
		
		instances.get(1).moveSlider(0);		
		s = instances.get(0).get("sna2:slider", "cursor", "position");
		System.out.println(s);
		j = new JSONObject(s);
		
		assertEquals(0,j.getJSONObject("response").getInt("value"));
		
		instances.get(1).moveSlider(150);		
		s = instances.get(0).get("sna2:slider", "cursor", "position");
		System.out.println(s);
		j = new JSONObject(s);
		
		assertEquals(150,j.getJSONObject("response").getInt("value"));
		
	    while(instances.size() > 0)
	    {
		   instances.remove(0).tearDown();
	    }
	}
}
