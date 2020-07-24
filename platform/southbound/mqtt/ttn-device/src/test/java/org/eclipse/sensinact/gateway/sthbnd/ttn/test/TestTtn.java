package org.eclipse.sensinact.gateway.sthbnd.ttn.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.sensinact.gateway.common.primitive.Describable;
import org.eclipse.sensinact.gateway.core.ActionResource;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.Service;
import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfigurationImpl;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleRequest;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleResponse;
import org.eclipse.sensinact.gateway.test.MidOSGiTest;
import org.eclipse.sensinact.gateway.test.MidProxy;
import org.eclipse.sensinact.gateway.util.IOUtils;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class TestTtn extends MidOSGiTest {
	
	//********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//
    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//
    public static int HTTP_PORT = 8893;
    public static String HTTP_ROOTURL = "http://127.0.0.1:" + HTTP_PORT;

    public static String newRequest(String configuration) throws IOException {
        SimpleResponse response;

        ConnectionConfigurationImpl<SimpleResponse, SimpleRequest> builder = new ConnectionConfigurationImpl<SimpleResponse, SimpleRequest>(configuration);
        SimpleRequest request = new SimpleRequest(builder);
        response = request.send();
        byte[] responseContent = response.getContent();
        String contentStr = (responseContent == null ? null : new String(responseContent));
        return contentStr;
    }

    public static String newRequest(String url, String content, String method) {
        SimpleResponse response;
        ConnectionConfigurationImpl<SimpleResponse, SimpleRequest> builder = new ConnectionConfigurationImpl<SimpleResponse, SimpleRequest>();
        builder.setUri(url);
        try {
            if (method.equals("GET")) {
                builder.setHttpMethod("GET");

            } else if (method.equals("POST")) {
                builder.setContentType("application/json");
                builder.setHttpMethod("POST");
                if (content != null && content.length() > 0) {
                    JSONObject jsonData = new JSONObject(content);
                    builder.setContent(jsonData.toString());
                }
            } else {
                return null;
            }
            builder.setAccept("application/json");
            SimpleRequest request = new SimpleRequest(builder);
            response = request.send();
            byte[] responseContent = response.getContent();
            String contentStr = (responseContent == null ? null : new String(responseContent));
            return contentStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

    Method getDescription = null;
    Method getMethod = null;
    Method setMethod = null;
    Method actMethod = null;
    MqttClient client;

    public TestTtn() throws Exception {
        super();
        getDescription = Describable.class.getDeclaredMethod("getDescription");
        getMethod = Resource.class.getDeclaredMethod("get", new Class<?>[]{String.class});
        setMethod = Resource.class.getDeclaredMethod("set", new Class<?>[]{String.class, Object.class});
        actMethod = ActionResource.class.getDeclaredMethod("act", new Class<?>[]{Object[].class});
    }

    /**
     * @inheritDoc
     * @see MidOSGiTest#isExcluded(java.lang.String)
     */
    public boolean isExcluded(String fileName) {
        if ("org.apache.felix.framework.security.jar".equals(fileName)) {
            return true;
        }
        return false;
    }

    @Ignore
    @Test
    public void testHttpTask() throws Throwable {
        
        MidProxy<Core> mid = new MidProxy<Core>(classloader, this, Core.class);
        Core core = mid.buildProxy();

        Session session = core.getAnonymousSession();

        ServiceProvider provider = session.serviceProvider("TestForSensiNactGateway");
        Service service = provider.getService("service1");
        Resource variable = service.getResource("temperature");
        MidProxy midVariable = (MidProxy) Proxy.getInvocationHandler(variable);

        SnaMessage response = (SnaMessage) midVariable.toOSGi(getMethod, new Object[]{DataResource.VALUE});

        JSONObject jsonObject = new JSONObject(response.getJSON());
        assertEquals(24, (int) jsonObject.getJSONObject("response").getInt("value"));

        response = (SnaMessage) midVariable.toOSGi(setMethod, new Object[]{DataResource.VALUE, 25});
        jsonObject = new JSONObject(response.getJSON());
        assertEquals(25, (int) jsonObject.getJSONObject("response").getInt("value"));

        response = (SnaMessage) midVariable.toOSGi(getMethod, new Object[]{DataResource.VALUE});
        jsonObject = new JSONObject(response.getJSON());
        assertEquals(25, (int) jsonObject.getJSONObject("response").getInt("value"));
    }

    /**
     * @inheritDoc
     * @see MidOSGiTest#doInit(java.util.Map)
     */
	@Override
    @SuppressWarnings("unchecked")
    protected void doInit(Map configuration) {
		configuration.put("felix.auto.start.1",  
                "file:target/felix/bundle/org.osgi.service.component.jar "+  
                "file:target/felix/bundle/org.osgi.service.condpermadmin.jar "+ 
                "file:target/felix/bundle/org.osgi.service.cm.jar "+  
                "file:target/felix/bundle/org.osgi.service.metatype.jar "+  
                "file:target/felix/bundle/org.osgi.namespace.extender.jar "+  
                "file:target/felix/bundle/org.osgi.util.promise.jar "+  
                "file:target/felix/bundle/org.osgi.util.function.jar "+  
                "file:target/felix/bundle/org.osgi.util.pushstream.jar "+
                "file:target/felix/bundle/org.osgi.service.log.jar "  +
                "file:target/felix/bundle/org.apache.felix.log.jar " + 
                "file:target/felix/bundle/org.apache.felix.scr.jar " +
        		"file:target/felix/bundle/org.apache.felix.fileinstall.jar " +
        		"file:target/felix/bundle/org.apache.felix.configadmin.jar " + 
        		"file:target/felix/bundle/org.apache.felix.framework.security.jar ");
        configuration.put("felix.auto.install.2",  
        	    "file:target/felix/bundle/sensinact-utils.jar " + 
                "file:target/felix/bundle/sensinact-common.jar " + 
        	    "file:target/felix/bundle/sensinact-datastore-api.jar " + 
                "file:target/felix/bundle/sensinact-security-none.jar " + 
                "file:target/felix/bundle/sensinact-generic.jar " +
        		"file:target/felix/bundle/http.jar " +
        	    "file:target/felix/bundle/mqtt-utils.jar " + 
        	    "file:target/felix/bundle/mqtt-device.jar " + 
        	    "file:target/felix/bundle/org.eclipse.paho.client.mqttv3.jar " +
                "file:target/felix/bundle/slf4j-api.jar " + 
                "file:target/felix/bundle/slf4j-simple.jar");
        configuration.put("felix.auto.start.2", 
        		"file:target/felix/bundle/sensinact-signature-validator.jar " + 
        		"file:target/felix/bundle/sensinact-core.jar");
        configuration.put("felix.auto.start.3", 
                "file:target/felix/bundle/mqtt-server.jar " +
                "file:target/felix/bundle/dynamicBundle.jar");
        
        configuration.put("org.eclipse.sensinact.gateway.security.jks.filename", "target/felix/bundle/keystore.jks");
        configuration.put("org.eclipse.sensinact.gateway.security.jks.password", "sensiNact_team");
        configuration.put("the.things.network.application.id","test");
        configuration.put("the.things.network.application.key","test");
        configuration.put("the.things.network.broker.host","eu.thethings.network");
        configuration.put("the.things.network.broker.port","8893");
        configuration.put("the.things.network.broker.protocol","SSL");
        try{
			this.client = new MqttClient(
					String.format("%s://%s:%s", "tcp", "127.0.0.1", "1883"),
					UUID.randomUUID().toString(), 
					new MemoryPersistence());
			this.client.connect();
			this.client.publish("", new MqttMessage());
		}
		catch (MqttException e)
		{
			e.printStackTrace();
		}
    }

    private void initializeMoke(URL resource, File manifestFile, File... sourceDirectories) throws Exception {
        File tmpDirectory = new File("./target/felix/tmp");
        new File(tmpDirectory, "resources.xml").delete();
        new File(tmpDirectory, "dynamicBundle.jar").delete();

        FileOutputStream output = null;
        byte[] resourcesBytes = IOUtils.read(resource.openStream());
        output = new FileOutputStream(new File(tmpDirectory, "resources.xml"));
        IOUtils.write(resourcesBytes, output);

        int length = (sourceDirectories == null ? 0 : sourceDirectories.length);
        File[] sources = new File[length + 1];
        int index = 0;
        if (length > 0) {
            for (; index < length; index++) {
                sources[index] = sourceDirectories[index];
            }
        }
        sources[index] = new File(tmpDirectory, "resources.xml");
        super.createDynamicBundle(manifestFile, tmpDirectory, sources);

        Bundle bundle = super.installDynamicBundle(new File(tmpDirectory, "dynamicBundle.jar").toURI().toURL());

        ClassLoader current = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(super.classloader);
        try {
            bundle.start();

        } finally {
            Thread.currentThread().setContextClassLoader(current);
        }
        Thread.sleep(10 * 1000);
    }
}
