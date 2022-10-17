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

import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskUrlConfigurator;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;
import org.eclipse.sensinact.gateway.util.UriUtils;

import aQute.bnd.annotation.Resolution;
import aQute.bnd.annotation.spi.ServiceProvider;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@ServiceProvider(value = HttpTaskUrlConfigurator.class, resolution = Resolution.OPTIONAL)
public class UrlBuilderImpl implements HttpTaskUrlConfigurator
{
	//********************************************************************//
	//						NESTED DECLARATIONS			  			      //
	//********************************************************************//

	//********************************************************************//
	//						ABSTRACT DECLARATIONS						  //
	//********************************************************************//

	//********************************************************************//
	//						STATIC DECLARATIONS							  //
	//********************************************************************//

	//********************************************************************//
	//						INSTANCE DECLARATIONS						  //
	//********************************************************************//
	
	/**
	 * @inheritDoc
	 *
	 * @see HttpTaskUrlConfigurator#configure(HttpTask)
	 */
	@Override
	public <T extends HttpTask<?,?>> void configure(T task)
	{
		if(Task.CommandType.SERVICES_ENUMERATION.equals(task.getCommand()))
		{
			StringBuilder uriBuilder = new StringBuilder();
			uriBuilder.append(task.getUri()
					).append(UriUtils.getRoot(task.getPath())
					).append("/services");
			
			task.setUri(uriBuilder.toString());
		}
	}

	@Override
	public Task.CommandType[] handled()
	{
		return new Task.CommandType[]{Task.CommandType.SERVICES_ENUMERATION};
	}
}
