/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.http.forward.test.bundle1;

import java.util.Dictionary;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.sensinact.gateway.nthbnd.http.forward.ForwardingService;
import org.osgi.test.common.dictionary.Dictionaries;


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
    public Dictionary<String, Object> getProperties() {
        return Dictionaries.dictionaryOf("pattern", getPattern());
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
