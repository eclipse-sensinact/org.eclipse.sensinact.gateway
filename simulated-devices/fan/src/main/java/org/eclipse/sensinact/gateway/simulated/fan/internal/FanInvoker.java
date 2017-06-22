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

package org.eclipse.sensinact.gateway.simulated.fan.internal;

import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.generic.annotation.TaskCommand;
import org.eclipse.sensinact.gateway.generic.annotation.TaskInject;
import org.eclipse.sensinact.gateway.generic.annotation.TaskExecution;

/**
 * 
 */
@TaskExecution
public class FanInvoker
{
	@TaskInject
	FanConfig config;
	
	@TaskCommand(method = CommandType.ACT, target="/fan/switch/dim")
	public void dim(String uri, int speed)
	{
		config.setSpeed(speed);		
	}

	@TaskCommand(method = CommandType.ACT, target="/fan/switch/turn_on")
	public void on(String uri)
	{
		config.turnOn();
	}

	@TaskCommand(method = CommandType.ACT, target="/fan/switch/turn_off")
	public void off(String uri)
	{
		config.turnOff();
	}

}
