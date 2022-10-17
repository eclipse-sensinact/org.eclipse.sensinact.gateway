/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/

package org.eclipse.sensinact.gateway.sthbnd.http.test.bundle4;

import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration;
import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.HttpTaskConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.HttpTasks;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.SimpleHttpTask;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpActivator;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.Constants;

@HttpTasks( tasks =
{
	@SimpleHttpTask(
		commands = {Task.CommandType.GET, Task.CommandType.SERVICES_ENUMERATION},
		configuration = @HttpTaskConfiguration(
			host = "127.0.0.1", 
			port="8899", 
			path = "/get")
			),
//  another way to do it
//	@SimpleHttpTask(
//		commands = CommandType.SERVICES_ENUMERATION ,
//		configuration = @HttpTaskConfiguration(
//			host = "127.0.0.1", 
//			port=8899, 
//			path = "/get/@context[task.serviceProvider]/services")
//			),
	@SimpleHttpTask(
		commands = Task.CommandType.SET,
		configuration = @HttpTaskConfiguration(
			host = "127.0.0.1", 
			port="8899",
			httpMethod = "POST",
			path = "/set",
			content=ContentBuilderImpl.class)
			)
})
@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class Activator extends HttpActivator
{
	/**
	 * @inheritDoc
	 *
	 * @see HttpActivator#getServiceBuildPolicy()
	 */
	protected byte getServiceBuildPolicy()
	{
		return (byte) (SensiNactResourceModelConfiguration.BuildPolicy.BUILD_NON_DESCRIBED.getPolicy()|
			SensiNactResourceModelConfiguration.BuildPolicy.BUILD_APPEARING_ON_DESCRIPTION.getPolicy()|
			SensiNactResourceModelConfiguration.BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION.getPolicy());
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see HttpActivator#getPacketType()
	 */
	@Override
	protected Class<? extends HttpPacket> getPacketType()
	{
		return HttpTestPacket.class;
	}
}
