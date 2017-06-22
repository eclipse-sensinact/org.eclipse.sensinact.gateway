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
package org.eclipse.sensinact.gateway.sthbnd.android.internal.http;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.sensinact.gateway.util.UriUtils;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;

public class AndroidFilter implements Filter
{
	private Mediator mediator;

	public AndroidFilter(Mediator mediator)
	{
		this.mediator = mediator;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException 
	{
		System.out.println("initialize the Android filter");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException 
	{
		HttpServletRequest req = (HttpServletRequest) request;
		String leaf = UriUtils.getLeaf(req.getRequestURI());
		System.out.println("REQUESTED LEAF = " + leaf);
		request.setAttribute("android-device",leaf);
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() 
	{
		System.out.println("destroy the Android filter");
	}

}
