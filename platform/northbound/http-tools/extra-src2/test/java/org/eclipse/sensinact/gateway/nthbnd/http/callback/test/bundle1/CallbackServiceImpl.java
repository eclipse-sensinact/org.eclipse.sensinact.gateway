package org.eclipse.sensinact.gateway.nthbnd.http.callback.test.bundle1;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.CallbackContext;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.CallbackService;

import java.util.Dictionary;
import java.util.Hashtable;

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

        return "/callbackTest1";
    }

    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.nthbnd.forward.http.ForwardingService#getProperties()
     */
    @Override
    public Dictionary getProperties() {
        return new Hashtable() {{
            this.put("pattern", "/callbackTest1");
        }};
    }

    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.nthbnd.forward.http.ForwardingService#getUriBuilder()
     */
    @Override
    public Executable<CallbackContext, Void> getCallbackProcessor() {
        return new Executable<CallbackContext, Void>() {
            @Override
            public Void execute(CallbackContext context) throws Exception {
                try {
                    context.setResponseContent(("[" + context.getMethod() + "]" + context.getRequest().getRequestURI()).getBytes());

                    context.setResponseStatus(200);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }
}
