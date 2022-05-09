/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.free.osgi;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.HttpTaskConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.HttpTasks;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.KeyValuePair;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.SimpleHttpTask;
import org.eclipse.sensinact.gateway.sthbnd.http.free.internal.FreeSmsProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.DefaultHttpTaskProcessingContext;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.DefaultHttpTaskProcessingContextFactory;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpActivator;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskConfigurator;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskProcessingContext;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskProcessingContextFactory;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.SimpleHttpProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.Constants;

/**
 * Extended {@link HttpActivator}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@HttpTasks(tasks = {@SimpleHttpTask(commands = {CommandType.ACT}, 
configuration = @HttpTaskConfiguration(
		direct = true,
		scheme = "https", 
		port = "443",
		host = "smsapi.free-mobile.fr", 
		path = "/sendmsg", 
		query = {
			@KeyValuePair(key = "msg", value = "@context[task.msg]"),
			@KeyValuePair(key = "user", value = "@context[task.user]"), 
			@KeyValuePair(key = "pass", value = "@context[task.pass]")
		}
	)
)})
@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class Activator extends HttpActivator {

	@Override
	public Class<? extends SimpleHttpProtocolStackEndpoint> getEndpointType(){
		return FreeSmsProtocolStackEndpoint.class;		
	}
	
    @Override
    public HttpTaskProcessingContextFactory getTaskProcessingContextFactory() {
        return new DefaultHttpTaskProcessingContextFactory(mediator) {
            @Override
            public HttpTaskProcessingContext newInstance(HttpTaskConfigurator httpTaskConfigurator, String endpointId, HttpTask<?, ?> task) {
                return new FreeSmsTaskProcessingContext(super.mediator, httpTaskConfigurator, endpointId, task);
            }
        };
    }

    /**
     * Extended {@link DefaultHttpTaskProcessingContext} dedicated
     * to Free SMS tasks processing context
     *
     * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
     */
    private class FreeSmsTaskProcessingContext extends DefaultHttpTaskProcessingContext {
        /**
         * @param mediator
         * @param task
         */
        public FreeSmsTaskProcessingContext(Mediator mediator, HttpTaskConfigurator httpTaskConfigurator, final String endpointId, final HttpTask<?, ?> task) {
            super(mediator, httpTaskConfigurator, endpointId, task);

            final FreeSmsProtocolStackEndpoint.FreeSmsResourceConfig config = ((FreeSmsProtocolStackEndpoint) Activator.super.endpoint).getFreeSmsResourceConfig(UriUtils.getRoot(task.getPath()).substring(1));

            super.properties.put("task.msg", new Executable<Void, String>() {
                @Override
                public String execute(Void parameter) throws Exception {
                    return (String) task.getParameters()[0];
                }
            });
            super.properties.put("task.user", new Executable<Void, String>() {
                @Override
                public String execute(Void parameter) throws Exception {
                    if (config == null) {
                        return null;
                    }
                    return config.getUser();
                }
            });
            super.properties.put("task.pass", new Executable<Void, String>() {
                @Override
                public String execute(Void parameter) throws Exception {
                    if (config == null) {
                        return null;
                    }
                    return config.getPass();
                }
            });
        }

    }
}
