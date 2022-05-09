/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.kodi.osgi;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.InvalidProtocolStackException;
import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.HttpTaskConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.HttpTasks;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.SimpleHttpTask;
import org.eclipse.sensinact.gateway.sthbnd.http.kodi.internal.KodiDevicesDiscovery;
import org.eclipse.sensinact.gateway.sthbnd.http.kodi.internal.KodiRemoteControlHttpListener;
import org.eclipse.sensinact.gateway.sthbnd.http.kodi.internal.KodiResponsePacket;
import org.eclipse.sensinact.gateway.sthbnd.http.kodi.internal.KodiTaskConfigurator;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.DefaultHttpTaskProcessingContext;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.DefaultHttpTaskProcessingContextFactory;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpActivator;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpMediator;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskConfigurator;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskProcessingContext;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskProcessingContextFactory;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;

import java.util.Hashtable;

@HttpTasks(tasks = {@SimpleHttpTask(commands = {CommandType.GET, CommandType.ACT}, configuration = @HttpTaskConfiguration(host = "@context[kodi.ip]", path = "/jsonrpc", contentType = "application/json", httpMethod = "POST", content = KodiTaskConfigurator.class), profile = "")})
@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class Activator extends HttpActivator {
    private static final String KODI_ENDPOINT = "/kodi/remote-control";

    private KodiDevicesDiscovery devicesDiscovery = null;

    /**
     * @inheritDoc
     * @see HttpActivator#
     * connect(ExtModelConfiguration)
     */
    @Override
    public void connect(ExtModelConfiguration configuration) throws InvalidProtocolStackException {
        devicesDiscovery = new KodiDevicesDiscovery(mediator, super.endpoint, (String) mediator.getProperty("kodi.regex"));

        super.mediator.attachOnServiceAppearing(HttpService.class, null, new Executable<HttpService, Void>() {
            @Override
            public Void execute(HttpService httpService) throws Exception {
                HttpContext hc = httpService.createDefaultHttpContext();

                httpService.registerServlet(KODI_ENDPOINT, new KodiRemoteControlHttpListener(Activator.super.endpoint, (KodiServiceMediator) Activator.super.mediator), new Hashtable(), hc);

                return null;
            }
        });
        super.mediator.attachOnServiceDisappearing(HttpService.class, null, new Executable<HttpService, Void>() {
            @Override
            public Void execute(HttpService httpService) throws Exception {
                if (httpService != null) {
                    httpService.unregister(KODI_ENDPOINT);
                }
                return null;
            }
        });
        super.connect(configuration);
    }

    /**
     * @inheritDoc
     * @see HttpActivator#doStop()
     */
    public void doStop() throws Exception {
        super.doStop();
        this.devicesDiscovery = null;
    }

    /**
     * @inheritDoc
     * @see HttpActivator#
     * doInstantiate(org.osgi.framework.BundleContext)
     */
    @Override
    public HttpMediator doInstantiate(BundleContext context) {
        return new KodiServiceMediator(context);
    }

    /**
     * @inheritDoc
     * @see HttpActivator#getPacketType()
     */
    public Class<? extends HttpPacket> getPacketType() {
        return KodiResponsePacket.class;
    }

    /**
     * @inheritDoc
     * @see HttpActivator#
     * getProcessingContextFactory()
     */
    @Override
    public HttpTaskProcessingContextFactory getTaskProcessingContextFactory() {
        return new DefaultHttpTaskProcessingContextFactory(mediator) {
            @Override
            public HttpTaskProcessingContext newInstance(HttpTaskConfigurator httpTaskConfigurator, String endpointId, HttpTask<?, ?> task) {
                return new KodiTaskProcessingContext(super.mediator, httpTaskConfigurator, endpointId, task);
            }
        };
    }

    /**
     * Extended {@link DefaultHttpTaskProcessingContext} dedicated
     * to Kodi tasks processing context
     *
     * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
     */
    private class KodiTaskProcessingContext extends DefaultHttpTaskProcessingContext {
        /**
         * @param mediator
         * @param task
         */
        public KodiTaskProcessingContext(Mediator mediator, HttpTaskConfigurator httpTaskConfigurator, final String endpointId, final HttpTask<?, ?> task) {
            super(mediator, httpTaskConfigurator, endpointId, task);

            super.properties.put("kodi.ip", new Executable<Void, String>() {
                @Override
                public String execute(Void parameter) throws Exception {
                    String serviceProvider = UriUtils.getRoot(task.getUri()).substring(1);
                    return ((KodiServiceMediator) Activator.super.mediator).getKodiIP(serviceProvider);
                }
            });
        }
    }
}
