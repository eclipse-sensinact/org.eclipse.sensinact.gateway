package org.eclipse.sensinact.gateway.nthbnd.http.callback.test;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequestWrapper;

import org.eclipse.sensinact.gateway.nthbnd.http.callback.CallbackContext;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.CallbackService;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.ServletCallbackContext;

/**
 *
 */
public class CallbackServiceImpl implements CallbackService {
	
    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.nthbnd.forward.http.ForwardingService#getPattern()
     */
    @Override
    public String getPattern() {
        return "/callbackTest1/*";
    }

    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.nthbnd.forward.http.ForwardingService#getProperties()
     */
    @Override
    public Dictionary getProperties() {
        return new Hashtable() {{
            this.put("pattern", getPattern());
        }};
    }

	@Override
	public void process(CallbackContext context) {
		if(context instanceof ServletCallbackContext) {			
            context.getResponse().setContent(("[" + ((HttpServletRequestWrapper)context.getRequest()).getMethod() + "]" + context.getRequest().getRequestURI()).getBytes());
            context.getResponse().setResponseStatus(200);
            context.getResponse().flush();
		} else {			
            context.getResponse().setContent(("[WEBSOCKET]" + context.getRequest().getRequestURI()).getBytes());
            context.getResponse().flush();			
		}
	}

	@Override
	public int getCallbackType() {
		return CallbackService.CALLBACK_SERVLET |  CallbackService.CALLBACK_WEBSOCKET ;
	}
}
