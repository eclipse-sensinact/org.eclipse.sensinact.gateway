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
package org.eclipse.sensinact.gateway.nthbnd.http.forward.internal;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.Attributes;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;

/**
 * {@link Filter} Implementation in charge of forwarding a request
 * in an asynchronous context 
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 *
 */
@WebFilter(asyncSupported=true)
public class ForwardingFilter implements Filter 
{ 
	public static final java.lang.String __INCLUDE_PREFIX = "javax.servlet.include.";
	
	public static final java.lang.String __FORWARD_PREFIX = "javax.servlet.forward.";
	  
	/**
	 * Extended {@link Attributes}
	 */
	private class ForwardAttributes implements Attributes
	{
	    final Attributes _attr;
	
	    String _requestURI;
	    String _contextPath;
	    String _servletPath;
	    String _pathInfo;
	    String _query;
	
	    ForwardAttributes(Attributes attributes)
	    {
	        _attr=attributes;
	    }

	    /**
	     * @inheritDoc
	     * 
	     * @see org.eclipse.jetty.util.Attributes#getAttribute(java.lang.String)
	     */
	    @Override
	    public Object getAttribute(String key)
	    {
            if (key.equals(RequestDispatcher.FORWARD_PATH_INFO))
                return _pathInfo;
            if (key.equals(RequestDispatcher.FORWARD_REQUEST_URI))
                return _requestURI;
            if (key.equals(RequestDispatcher.FORWARD_SERVLET_PATH))
                return _servletPath;
            if (key.equals(RequestDispatcher.FORWARD_CONTEXT_PATH))
                return _contextPath;
            if (key.equals(RequestDispatcher.FORWARD_QUERY_STRING))
                return _query;

            if (key.startsWith(__INCLUDE_PREFIX))
                return null;

            return _attr.getAttribute(key);
	    }
	
	    /**
	     * @inheritDoc
	     * 
	     * @see org.eclipse.jetty.util.Attributes#getAttributeNames()
	     */
	    @Override
	    public Enumeration<String> getAttributeNames()
	    {
	        HashSet<String> set=new HashSet<>();
	        Enumeration<String> e=_attr.getAttributeNames();
	        while(e.hasMoreElements())
	        {
	            String name=e.nextElement();
	            if (!name.startsWith(__INCLUDE_PREFIX) &&
	                !name.startsWith(__FORWARD_PREFIX))
	                set.add(name);
	        }
            if (_pathInfo!=null)
                set.add(RequestDispatcher.FORWARD_PATH_INFO);
            else
                set.remove(RequestDispatcher.FORWARD_PATH_INFO);
            set.add(RequestDispatcher.FORWARD_REQUEST_URI);
            set.add(RequestDispatcher.FORWARD_SERVLET_PATH);
            set.add(RequestDispatcher.FORWARD_CONTEXT_PATH);
            if (_query!=null)
                set.add(RequestDispatcher.FORWARD_QUERY_STRING);
            else
                set.remove(RequestDispatcher.FORWARD_QUERY_STRING);
	
	        return Collections.enumeration(set);
	    }

	    /**
	     * @inheritDoc
	     * 
	     * @see org.eclipse.jetty.util.Attributes#setAttribute(java.lang.String, java.lang.Object)
	     */
	    @Override
	    public void setAttribute(String key, Object value)
	    {
	        if (key.startsWith("javax.servlet."))
	        {
	            if (key.equals(RequestDispatcher.FORWARD_PATH_INFO))
	                _pathInfo=(String)value;
	            else if (key.equals(RequestDispatcher.FORWARD_REQUEST_URI))
	                _requestURI=(String)value;
	            else if (key.equals(RequestDispatcher.FORWARD_SERVLET_PATH))
	                _servletPath=(String)value;
	            else if (key.equals(RequestDispatcher.FORWARD_CONTEXT_PATH))
	                _contextPath=(String)value;
	            else if (key.equals(RequestDispatcher.FORWARD_QUERY_STRING))
	                _query=(String)value;
	
	            else if (value==null)
	                _attr.removeAttribute(key);
	            else
	                _attr.setAttribute(key,value);
	        }
	        else if (value==null)
	            _attr.removeAttribute(key);
	        else
	            _attr.setAttribute(key,value);
	    }

	    /**
	     * @inheritDoc
	     * 
	     * @see java.lang.Object#toString()
	     */
	    @Override
	    public String toString()
	    {
	        return "FORWARD+"+_attr.toString();
	    }

	    /**
	     * @inheritDoc
	     * 
	     * @see org.eclipse.jetty.util.Attributes#clearAttributes()
	     */
	    @Override
	    public void clearAttributes()
	    {
	    	_attr.clearAttributes();
	    	_requestURI = null;
		    _contextPath = null;
		    _servletPath = null;
		    _pathInfo = null;
		    _query = null;
	    }

	    /**
	     * @inheritDoc
	     * 
	     * @see org.eclipse.jetty.util.Attributes#removeAttribute(java.lang.String)
	     */
	    @Override
	    public void removeAttribute(String name)
	    {
	        setAttribute(name,null);
	    }
	}
	
	/**
	 * the {@link Mediator} allowing to interact with the OSGi 
	 * host environment
	 */
	private Mediator mediator;

	/**
	 * the {@link Executable} allowing to build the target URI
	 * of the forwarding process
	 */
	private Executable<Request,String> uriBuilder; 
	
	/**
	 * the {@link Executable} allowing to build the query String
	 * to be forwarded
	 */
	private Executable<Request,String> queryBuilder; 
	
    /**
     * Constructor
     * 
     * @param uriBuilder the {@link Executable} allowing the ForwardingFilter to
     * be instantiated to build the forwarding String URI according to the {@link Request} 
     * parameterizing its execution
     * @param queryBuilder the {@link Executable} allowing the ForwardingFilter to
     * be instantiated to build the forwarding query according to the {@link Request} 
     * parameterizing its execution
     */
    public ForwardingFilter(Mediator mediator, Executable<Request,String> uriBuilder, 
    		Executable<Request,String> queryBuilder) 
    {
    	this.mediator = mediator;
        this.uriBuilder = uriBuilder;
        this.queryBuilder = queryBuilder;
    }
   
    /**
     * @inheritDoc
     *
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig config) throws ServletException
    {
    	mediator.debug("Init with config [" + config + "]");
    }

    /**
     * @inheritDoc
     *
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, 
     * javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse res, 
    	FilterChain chain) throws IOException, ServletException 
    {   
    	ServletRequest sr = req;
    	while(ServletRequestWrapper.class.isAssignableFrom(sr.getClass()))
    	{
    		sr = ((ServletRequestWrapper)sr).getRequest();
    	}
    	final org.eclipse.jetty.server.Request baseRequest =
    		(org.eclipse.jetty.server.Request) sr;

        final String old_uri = baseRequest.getRequestURI();
        final String old_context_path = baseRequest.getContextPath();
        final String old_servlet_path = baseRequest.getServletPath();
        final String old_path_info = baseRequest.getPathInfo();
        final String old_query = baseRequest.getQueryString();
        final Attributes old_attr = baseRequest.getAttributes();
    	     	    	
    	baseRequest.getResponse().resetForForward(); 
        String uri = null;
    	String query = null; 

		if(this.uriBuilder!=null)
		{
			try
			{
				uri = this.uriBuilder.execute(baseRequest);
			} catch (Exception e) 
			{
				throw new IOException(e);
			}
		}
		if(uri == null)
		{
			uri = old_uri;
		}
        baseRequest.setHandled(false);
        baseRequest.setDispatcherType(DispatcherType.REQUEST);

    	ForwardAttributes attr = new ForwardAttributes(old_attr);
        attr._pathInfo=old_path_info;
        attr._query=old_query;
        attr._requestURI=old_uri;
        attr._contextPath=old_context_path;
        attr._servletPath=old_servlet_path;

        baseRequest.setRequestURI(uri);
        baseRequest.setContextPath(baseRequest.getContextPath());
        baseRequest.setServletPath(null);
        baseRequest.setPathInfo(uri);
        if(this.queryBuilder!=null) 
        {
	        try
			{
				query = this.queryBuilder.execute(baseRequest);
			} catch (Exception e) 
			{
				throw new IOException(e);
			}
        }
        if(query == null)
        {
        	query = old_query;
        }
        if (query!=null)
        {
            baseRequest.mergeQueryParameters(query, true);
        }
        baseRequest.setAttributes(attr);
        chain.doFilter(req, res);
     }

    /**
     * @inheritDoc
     *
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() 
    {
    	mediator.debug("Destroyed filter");
    }
}
