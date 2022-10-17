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
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskConfigurator;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ContentBuilderImpl implements HttpTaskConfigurator
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
	
	public ContentBuilderImpl()
	{}
	
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskUrlConfigurator#configure(HttpTask)
	 */
	@Override
	public <T extends HttpTask<?,?>> void configure(T task)
	{
		if(Task.CommandType.SET.equals(task.getCommand()))
		{
			task.setContent("{\"value\": "+task.getParameters()[1]+"}");
		}
	}
}
