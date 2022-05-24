package org.eclipse.sensinact.gateway.nthbnd.endpoint.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.sensinact.gateway.core.ActionResource;
import org.eclipse.sensinact.gateway.core.InvalidServiceProviderException;
import org.eclipse.sensinact.gateway.core.ResourceImpl;
import org.eclipse.sensinact.gateway.core.ServiceImpl;
import org.eclipse.sensinact.gateway.core.StateVariableResource;
import org.eclipse.sensinact.gateway.core.filtering.FilteringDefinition;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.core.method.Parameter;
import org.eclipse.sensinact.gateway.core.security.AccessProfileOption;
import org.eclipse.sensinact.gateway.core.security.AccessTree;
import org.eclipse.sensinact.gateway.core.security.AccessTreeImpl;
import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.core.security.SecuredAccessException;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.DefaultNorthboundRequestHandler;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundEndpoint;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRecipient;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequest;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestBuilder;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestHandler;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestWrapper;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.skyscreamer.jsonassert.JSONAssert;

import jakarta.json.spi.JsonProvider;

/**
 *
 */
public class NorthboundRequestHandlerTest {

    protected TestContext testContext;
    protected Dictionary<String, Object> props;
    protected AccessTree tree;

    private final BundleContext context = Mockito.mock(BundleContext.class);
    private final Bundle bundle = Mockito.mock(Bundle.class);
    
    private JsonProvider provider = JsonProviderFactory.getProvider();

    @BeforeEach
    public void init() throws InvalidServiceProviderException, InvalidSyntaxException, SecuredAccessException, BundleException, DataStoreException {
        this.testContext = new TestContext();
        this.tree = new AccessTreeImpl<>().withAccessProfile(AccessProfileOption.ALL_ANONYMOUS);
    }


    @AfterEach
    public void tearDown() {
        this.testContext.stop();
    }

    @Test
    public void testFilteringAvailable() throws Throwable {
        
    	ServiceImpl service = this.testContext.getModelInstance().getRootElement().addService("testService");
        
        ResourceImpl r1impl = service.addActionResource("TestAction", ActionResource.class);
        ResourceImpl r2impl = service.addDataResource(StateVariableResource.class, "TestVariable", String.class, "untriggered");

        Thread.sleep(1000);
        NorthboundRequestHandler handler = new DefaultNorthboundRequestHandler();
        NorthboundRequestWrapper wrapper = getRequestWrapper("/sensinact", null, Arrays.asList("a"),1, Arrays.asList("a"),0);
        handler.init(wrapper,Arrays.stream(AccessMethod.Type.values()
        		).collect(HashSet::new,Set::add,Set::addAll));
        assertTrue(handler.processRequestURI());
        NorthboundRequestBuilder builder = handler.handle();
        NorthboundRequest request = builder.build();
        NorthboundEndpoint endpoint = new NorthboundEndpoint((NorthboundMediator) testContext.getMediator(), null);
        AccessMethodResponse<?> response = endpoint.execute(request);

        String obj = response.getJSON();
        System.out.println(obj);
        assertEquals(provider.createReader(new StringReader("{\"providers\":" 
        		+ "[{\"locYtion\":\"45.19334890078532:5.706474781036377\"," 
        		+ "\"services\":[{\"resources\":" 
        		+ "[{\"type\":\"PROPERTY\",\"nYme\":\"friendlyNYme\"}," 
        		+ "{\"type\":\"PROPERTY\",\"nYme\":\"locYtion\"}," 
        		+ "{\"type\":\"PROPERTY\",\"nYme\":\"bridge\"}," 
        		+ "{\"type\":\"PROPERTY\",\"nYme\":\"icon\"}]," 
        		+ "\"nYme\":\"Ydmin\"}," + "{\"resources\":" 
        		+ "[{\"type\":\"ACTION\",\"nYme\":\"TestAction\"}," 
        		+ "{\"type\":\"STATE_VARIABLE\",\"nYme\":\"TestVYriYble\"}]," 
        		+ "\"nYme\":\"testService\"}],\"nYme\":\"serviceProvider\"}]," 
        		+ "\"filters\":[{\"definition\":\"a\",\"type\":\"xfilter\"}," 
        		+ "{\"definition\":\"a\",\"type\":\"yfilter\"}]" 
        		+ ",\"statusCode\":200,\"type\":\"COMPLETE_LIST\"}")).readObject(), 
        		provider.createReader(new StringReader(obj)).readObject());

        wrapper = getRequestWrapper("/sensinact", null, Arrays.asList("a"),0, Arrays.asList("a"),1);
        handler.init(wrapper,Arrays.stream(AccessMethod.Type.values()
        		).collect(HashSet::new,Set::add,Set::addAll));
        assertTrue(handler.processRequestURI());
        builder = handler.handle();
        request = builder.build();
        response = endpoint.execute(request);

        obj = response.getJSON();
        System.out.println(obj);

        assertEquals(provider.createReader(new StringReader("{\"providers\":" 
        		+ "[{\"locXtion\":\"45.19334890078532:5.706474781036377\"," 
        		+ "\"services\":[{\"resources\":" 
        		+ "[{\"type\":\"PROPERTY\",\"nXme\":\"friendlyNXme\"}," 
        		+ "{\"type\":\"PROPERTY\",\"nXme\":\"locXtion\"}," 
        		+ "{\"type\":\"PROPERTY\",\"nXme\":\"bridge\"}," 
        		+ "{\"type\":\"PROPERTY\",\"nXme\":\"icon\"}]," 
        		+ "\"nXme\":\"Xdmin\"}," + "{\"resources\":" 
        		+ "[{\"type\":\"ACTION\",\"nXme\":\"TestAction\"}," 
        		+ "{\"type\":\"STATE_VARIABLE\",\"nXme\":\"TestVXriXble\"}]," 
        		+ "\"nXme\":\"testService\"}],\"nXme\":\"serviceProvider\"}]," 
        		+ "\"filters\":[{\"definition\":\"a\",\"type\":\"xfilter\"}," 
        		+ "{\"definition\":\"a\",\"type\":\"yfilter\"}]" 
        		+ ",\"statusCode\":200,\"type\":\"COMPLETE_LIST\"}")).readObject(), 
        		provider.createReader(new StringReader(obj)).readObject());

        wrapper = getRequestWrapper("/sensinact", null, Arrays.asList("a"),FilteringDefinition.UNRANKED, Arrays.asList("f"),FilteringDefinition.UNRANKED);
        handler.init(wrapper,Arrays.stream(AccessMethod.Type.values()
        		).collect(HashSet::new,Set::add,Set::addAll));
        assertTrue(handler.processRequestURI());
        builder = handler.handle();
        request = builder.build();
        response = endpoint.execute(request);

        obj = response.getJSON();
        System.out.println(obj);

        assertEquals(provider.createReader(new StringReader("{\"providers\":" 
        		+ "[{\"locXtion\":\"45.19334890078532:5.706474781036377\"," 
        		+ "\"services\":[{\"resources\":" 
        		+ "[{\"type\":\"PROPERTY\",\"nXme\":\"YriendlyNXme\"}," 
        		+ "{\"type\":\"PROPERTY\",\"nXme\":\"locXtion\"}," 
        		+ "{\"type\":\"PROPERTY\",\"nXme\":\"bridge\"}," 
        		+ "{\"type\":\"PROPERTY\",\"nXme\":\"icon\"}]," 
        		+ "\"nXme\":\"Xdmin\"}," + "{\"resources\":" 
        		+ "[{\"type\":\"ACTION\",\"nXme\":\"TestAction\"}," 
        		+ "{\"type\":\"STATE_VARIABLE\",\"nXme\":\"TestVXriXble\"}]," 
        		+ "\"nXme\":\"testService\"}],\"nXme\":\"serviceProvider\"}]," 
        		+ "\"filters\":[{\"definition\":\"a\",\"type\":\"xfilter\"}," 
        		+ "{\"definition\":\"f\",\"type\":\"yfilter\"}]" 
        		+ ",\"statusCode\":200,\"type\":\"COMPLETE_LIST\"}")).readObject(), 
        		provider.createReader(new StringReader(obj)).readObject());
    }

    @Test
    public void testFilteringUnavailable() throws Throwable {
        ServiceImpl service = this.testContext.getModelInstance().getRootElement().addService("testService");
        ResourceImpl r1impl = service.addActionResource("TestAction", ActionResource.class);
        ResourceImpl r2impl = service.addDataResource(StateVariableResource.class, "TestVariable", String.class, "untriggered");

        Thread.sleep(1000);
        testContext.setXFilterAvailable(false);
        testContext.setYFilterAvailable(false);

        NorthboundRequestHandler handler = new DefaultNorthboundRequestHandler();
        NorthboundRequestWrapper wrapper = getRequestWrapper("/sensinact", null, Arrays.asList("a"),FilteringDefinition.UNRANKED, Arrays.asList("a"),FilteringDefinition.UNRANKED);
        handler.init(wrapper,Arrays.stream(AccessMethod.Type.values()
        		).collect(HashSet::new,Set::add,Set::addAll));
        assertTrue(handler.processRequestURI());
        NorthboundRequestBuilder builder = handler.handle();
        NorthboundRequest request = builder.build();
        NorthboundEndpoint endpoint = new NorthboundEndpoint((NorthboundMediator) testContext.getMediator(), null);
        AccessMethodResponse response = endpoint.execute(request);

        String obj = response.getJSON();
        System.out.println(obj);
        assertEquals(provider.createReader(new StringReader("{\"providers\":" 
        		+ "[{\"location\":\"45.19334890078532:5.706474781036377\"," 
        		+ "\"services\":[{\"resources\":" 
        		+ "[{\"type\":\"PROPERTY\",\"name\":\"friendlyName\"}," 
        		+ "{\"type\":\"PROPERTY\",\"name\":\"location\"}," 
        		+ "{\"type\":\"PROPERTY\",\"name\":\"bridge\"}," 
        		+ "{\"type\":\"PROPERTY\",\"name\":\"icon\"}]," 
        		+ "\"name\":\"admin\"}," + "{\"resources\":" 
        		+ "[{\"type\":\"ACTION\",\"name\":\"TestAction\"}," 
        		+ "{\"type\":\"STATE_VARIABLE\",\"name\":\"TestVariable\"}]," 
        		+ "\"name\":\"testService\"}],\"name\":\"serviceProvider\"}],"
                // + "\"filters\":[{\"definition\":\"a\",\"type\":\"xfilter\"},"
                // + "{\"definition\":\"f\",\"type\":\"yfilter\"}],"
                + "\"statusCode\":200,\"type\":\"COMPLETE_LIST\"}")).readObject(), 
        		provider.createReader(new StringReader(obj)).readObject());

        testContext.setYFilterAvailable(true);
        wrapper = getRequestWrapper("/sensinact", null, Arrays.asList("a"), FilteringDefinition.UNRANKED, Arrays.asList("f"), FilteringDefinition.UNRANKED);
        handler.init(wrapper,Arrays.stream(AccessMethod.Type.values()
        		).collect(HashSet::new,Set::add,Set::addAll));
        assertTrue(handler.processRequestURI());
        builder = handler.handle();
        request = builder.build();
        response = endpoint.execute(request);

        obj = response.getJSON();
        System.out.println(obj);

        assertEquals(provider.createReader(new StringReader("{\"providers\":" 
        		+ "[{\"location\":\"45.19334890078532:5.706474781036377\"," 
        		+ "\"services\":[{\"resources\":" 
        		+ "[{\"type\":\"PROPERTY\",\"name\":\"YriendlyName\"}," 
        		+ "{\"type\":\"PROPERTY\",\"name\":\"location\"}," 
        		+ "{\"type\":\"PROPERTY\",\"name\":\"bridge\"}," 
        		+ "{\"type\":\"PROPERTY\",\"name\":\"icon\"}]," 
        		+ "\"name\":\"admin\"}," + "{\"resources\":" 
        		+ "[{\"type\":\"ACTION\",\"name\":\"TestAction\"}," 
        		+ "{\"type\":\"STATE_VARIABLE\",\"name\":\"TestVariable\"}]," 
        		+ "\"name\":\"testService\"}],\"name\":\"serviceProvider\"}]," 
        		+ "\"filters\":[{\"definition\":\"f\",\"type\":\"yfilter\"}]" 
        		+ ",\"statusCode\":200,\"type\":\"COMPLETE_LIST\"}")).readObject(), 
        		provider.createReader(new StringReader(obj)).readObject());
        testContext.setYFilterAvailable(false);
        testContext.setXFilterAvailable(true);

        wrapper = getRequestWrapper("/sensinact", null, Arrays.asList("a"), FilteringDefinition.UNRANKED, Arrays.asList("f"), FilteringDefinition.UNRANKED);
        handler.init(wrapper,Arrays.stream(AccessMethod.Type.values()
        		).collect(HashSet::new,Set::add,Set::addAll));
        assertTrue(handler.processRequestURI());
        builder = handler.handle();
        request = builder.build();
        response = endpoint.execute(request);

        obj = response.getJSON();
        System.out.println(obj);

        assertEquals(provider.createReader(new StringReader("{\"providers\":" 
        		+ "[{\"locXtion\":\"45.19334890078532:5.706474781036377\"," 
        		+ "\"services\":[{\"resources\":" 
        		+ "[{\"type\":\"PROPERTY\",\"nXme\":\"friendlyNXme\"}," 
        		+ "{\"type\":\"PROPERTY\",\"nXme\":\"locXtion\"}," 
        		+ "{\"type\":\"PROPERTY\",\"nXme\":\"bridge\"}," 
        		+ "{\"type\":\"PROPERTY\",\"nXme\":\"icon\"}]," + "\"nXme\":\"Xdmin\"}," 
        		+ "{\"resources\":" + "[{\"type\":\"ACTION\",\"nXme\":\"TestAction\"}," 
        		+ "{\"type\":\"STATE_VARIABLE\",\"nXme\":\"TestVXriXble\"}]," 
        		+ "\"nXme\":\"testService\"}],\"nXme\":\"serviceProvider\"}]," 
        		+ "\"filters\":[{\"definition\":\"a\",\"type\":\"xfilter\"}]" 
        		+ ",\"statusCode\":200,\"type\":\"COMPLETE_LIST\"}")).readObject(), 
        		provider.createReader(new StringReader(obj)).readObject());
    }

    private final NorthboundRequestWrapper getRequestWrapper(final String uri, final String requestId, final List<String> xfilter, int rankx, final List<String> yfilter, int ranky) {
        return new NorthboundRequestWrapper() {
            @Override
            public NorthboundMediator getMediator() {
                return (NorthboundMediator) testContext.getMediator();
            }

            @Override
            public String getRequestURI() {
                return uri;
            }

            @Override
            public String getRequestId() {
                return requestId;
            }

            @Override
            public Map<QueryKey, List<String>> getQueryMap() {
                return new HashMap<QueryKey, List<String>>() {{
                	QueryKey k0 = new QueryKey();
                	k0.index = 0;
                	k0.name = "yfilter."+ranky;
                    put(k0 , yfilter);
                	QueryKey k1 = new QueryKey();
                	k1.index = 1;
                	k1.name = "xfilter."+rankx;
                    put(k1, xfilter);
                    /*put("hideFilter",Arrays.asList("true"));	*/
                }};
            }

            @Override
            public String getContent() {
                return null;
            }

            @Override
            public NorthboundRecipient createRecipient(List<Parameter> parameters) {
                return null;
            }

            @Override
            public Authentication<?> getAuthentication() {
                return null;
            }

			@Override
			public String getRequestIdProperty() {
				return null;
			}
        };
    }
}
