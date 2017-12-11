/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */

package org.eclipse.sensinact.gateway.sthbnd.http.test.bundle4;

import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration;
import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.HttpTaskConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.HttpTasks;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.SimpleHttpTask;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpActivator;

@HttpTasks( tasks =
{
	@SimpleHttpTask(
		commands = {Task.CommandType.GET, Task.CommandType.SERVICES_ENUMERATION},
		configuration = @HttpTaskConfiguration(
			host = "127.0.0.1", 
			port="8893", 
			path = "/get")
			),
//  another way to do it
//	@SimpleHttpTask(
//		commands = CommandType.SERVICES_ENUMERATION ,
//		configuration = @HttpTaskConfiguration(
//			host = "127.0.0.1", 
//			port=8893, 
//			path = "/get/@context[task.serviceProvider]/services")
//			),
	@SimpleHttpTask(
		commands = Task.CommandType.SET,
		configuration = @HttpTaskConfiguration(
			host = "127.0.0.1", 
			port="8893",
			httpMethod = "POST",
			path = "/set",
			content=ContentBuilderImpl.class)
			)
})
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
