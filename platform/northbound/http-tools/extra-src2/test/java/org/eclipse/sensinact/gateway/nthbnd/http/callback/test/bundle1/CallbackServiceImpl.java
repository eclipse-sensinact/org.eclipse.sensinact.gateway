package org.eclipse.sensinact.gateway.nthbnd.http.callback.test.bundle1;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.sensinact.gateway.nthbnd.http.callback.CallbackContext;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.CallbackService;

/**
 *
 */
public class CallbackServiceImpl implements CallbackService {
    /**
     *
     */
    public CallbackServiceImpl() {
    }

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
            this.put("pattern", "/callbackTest1/*");
        }};
    }

	@Override
	public void process(CallbackContext context) {
		try {
            context.setResponseContent(("[" + context.getMethod() + "]" + context.getRequest().getRequestURI()).getBytes());
            context.setResponseStatus(200);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
