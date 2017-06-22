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
package org.eclipse.sensinact.gateway.nthbnd.rest.internal;

public class AccountingEntry
{
	public final String timestamp;
	public final String client;
	public final String uri;
	public final String method;
	public final String status;
	
	public AccountingEntry(String timestamp, 
			String client,String uri,String method,
			String status)
	{
		this.timestamp = timestamp;
		this.client = client;
		this.uri = uri;
		this.method = method;
		this.status = status;
	}
}
