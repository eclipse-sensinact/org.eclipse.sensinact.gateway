/**
 *
 */
package org.eclipse.sensinact.gateway.nthbnd.http.forward.test.bundle1;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.sensinact.gateway.nthbnd.http.forward.ForwardingService;

/**
 *
 */
public class ForwardingServiceImpl implements ForwardingService {
    /**
     *
     */
    public ForwardingServiceImpl() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.nthbnd.forward.http.ForwardingService#getPattern()
     */
    @Override
    public String getPattern() {
        return "/forwardingTest1/*";
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
	public String getQuery(HttpServletRequest request) {
		String query = null;
        String path = request.getRequestURI();
        String restPath = path.substring(17);

        try {
            int val = Integer.parseInt(restPath);
            switch (val) {
                case 0:
                    query = "rawDescribe=true";
                    break;
                case 1:
                    ;
                case 2:
                    ;
                case 3:
                    ;
                    break;
            }
        } catch (NumberFormatException e) {
        }
        return query;
	}

	@Override
	public String getUri(HttpServletRequest request) {
		String uri = null;
        String uriContext = "/sensinact";
        String path = request.getRequestURI();
        String restPath = path.substring(17);

        try {
            int val = Integer.parseInt(restPath);
            switch (val) {
                case 0:
                    uri = uriContext.concat("/providers");
                    break;
                case 1:
                    uri = uriContext.concat("/providers/slider");
                    break;
                case 2:
                    uri = uriContext.concat("/providers/light");
                    break;
                case 3:
                    uri = uriContext.concat("/providers/slider/cursor/position/GET");
                    break;
            }
        } catch (NumberFormatException e) {
            uri = "/sensinact";
        }
        return uri;
	}

	@Override
	public String getParam(HttpServletRequest baseRequest) {
		return null;
	}

	@Override
	public String getFragment(HttpServletRequest baseRequest) {
		return null;
	}
}
