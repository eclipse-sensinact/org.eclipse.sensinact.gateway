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

package org.eclipse.sensinact.gateway.sthbnd.http.test.bundle3;

import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.ChainedHttpTask;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.ChainedHttpTasks;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.HttpChildTaskConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.HttpTaskConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpActivator;

@ChainedHttpTasks(tasks = {
	@ChainedHttpTask(
		configuration = 
			@HttpTaskConfiguration(
				host = "127.0.0.1", 
				port="8899"),
		chain = { 
			@HttpChildTaskConfiguration(
				identifier="serviceProviderId",
				path = "/json1"),
			@HttpChildTaskConfiguration(
				identifier="serviceId",
				path = "/json2"),
			@HttpChildTaskConfiguration(
				identifier="resourceId",
				path = "/json3")
		}
	)
})
public class Activator extends HttpActivator
{

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
