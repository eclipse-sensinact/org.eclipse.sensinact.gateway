/**
 *
 */
package org.eclipse.sensinact.gateway.nthbnd.http.forward.test.bundle1;

import org.eclipse.jetty.server.Request;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.nthbnd.http.forward.ForwardingService;

import java.util.Dictionary;
import java.util.Hashtable;

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

        return "/forwardingTest1/.*";
    }

    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.nthbnd.forward.http.ForwardingService#getProperties()
     */
    @Override
    public Dictionary getProperties() {
        return new Hashtable() {{
            this.put("pattern", "/forwardingTest1/.*");
        }};
    }

    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.nthbnd.forward.http.ForwardingService#getUriBuilder()
     */
    @Override
    public Executable<Request, String> getUriBuilder() {
        return new Executable<Request, String>() {
            @Override
            public String execute(Request request) throws Exception {
                String uri = null;
                String uriContext = "/sensinact";
                String path = request.getUri().getPath();
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
        };
    }

    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.nthbnd.forward.http.ForwardingService#getUriBuilder()
     */
    @Override
    public Executable<Request, String> getQueryBuilder() {
        return new Executable<Request, String>() {
            @Override
            public String execute(Request request) throws Exception {
                String query = null;
                String path = request.getUri().getPath();
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
        };
    }
}
