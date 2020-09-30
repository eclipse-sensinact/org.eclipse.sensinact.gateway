package ${package}.app.component;

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.CallbackContext;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.CallbackService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ${package}.app.service.WebAppRelay;
import ${package}.WebAppConstants;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CallbackService} implementation
 */
@Component(immediate=true, service={CallbackService.class, WebAppRelay.class})
public class CallbackServiceImpl implements CallbackService, WebAppRelay {

	private static final Logger LOG = LoggerFactory.getLogger(CallbackServiceImpl.class);
	
	public static final String UUID_KEY="uuid";
	
	private Map<String,CallbackHandler> handlers ;
	
	/**
     * Constructor
     */
    public CallbackServiceImpl() {
    	this.handlers = new HashMap<String,CallbackHandler>();
    }

    @Override
    public String getPattern() {
        return WebAppConstants.WEBAPP_CALLBACK;
    }

	@Override
	public int getCallbackType() {
		return CallbackService.CALLBACK_WEBSOCKET;
	}
	
    @Override
    public Dictionary getProperties() {
        return new Hashtable() {{
            this.put("pattern", WebAppConstants.WEBAPP_CALLBACK);
        }};
    }

	@Override
	public void process(CallbackContext context) {
		Map<String, List<String>> attributes = context.getRequest().getAttributes();		
		List<String> uuids = attributes.get(UUID_KEY);
		String uuid = (uuids==null || uuids.size()==0)?null:uuids.get(0);
		if(uuid == null) {
			new CallbackHandler().process(context);
			return;
		}
		CallbackHandler handler = handlers.get(uuid);
		if(handler == null) {
			handler = new CallbackHandler();
			handlers.put(uuid,handler);	
		}
		handler.process(context);
	}
	
	@Override
	public void relay(SnaMessage<?> message) {
		if(handlers.isEmpty())
			return;
		handlers.values().parallelStream().forEach(h -> h.doRelay(message));
	}
}
