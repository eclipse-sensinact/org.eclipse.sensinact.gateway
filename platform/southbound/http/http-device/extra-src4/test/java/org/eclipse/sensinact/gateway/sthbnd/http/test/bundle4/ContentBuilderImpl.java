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
