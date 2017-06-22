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
package org.eclipse.sensinact.gateway.common.bundle;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.osgi.service.log.LogService;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
final class LogExecutor implements Executable<LogService, Void>
{
	private int level;
	private Throwable throwable;
	private String message;
	
	public static final int LOG_DEBUG = 4;
	public static final int LOG_INFO = 3;
	public static final int LOG_WARNING = 2;
	public static final int LOG_ERROR = 1;
	//Log levels
	public static final int NO_LOG = 0;
	//Default log level
	public static final int DEFAULT_LOG_LEVEL = LOG_ERROR;
	
	/**
	 * @param level
	 * @param message
	 */
	LogExecutor(int level, String message)
	{
		this.level = level;
		this.message = message;
	}

	/**
	 * @param level
	 * @param message
	 * @param throwable
	 */
	LogExecutor(int level, String message, Throwable  throwable)
	{
		this(level,message);
		this.throwable = throwable;
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see Executable#execute(java.lang.Object)
	 */
	@Override
	public Void execute(LogService logService) 
			throws Exception
	{
		if(this.throwable == null)
		{
			logService.log(level, message);
			
		} else
		{
			logService.log(level, message, throwable);
		}
		return null;
	}
}