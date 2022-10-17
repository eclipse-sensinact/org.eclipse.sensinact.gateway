/*********************************************************************
* Copyright (c) 2020 Kentyou
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package ${package}.app.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.util.IOUtils;
import org.osgi.framework.Bundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ${package}.WebAppConstants;

/**
 * Serves the web resources 
 */
@WebFilter( urlPatterns= {WebAppConstants.WEBAPP_ALIAS}, asyncSupported=false)
public class ResourceFilter implements Filter {

	private static final Logger LOG = LoggerFactory.getLogger(ResourceFilter.class);
	
    @SuppressWarnings("serial")
	private static final Map<String,String> MIME = new HashMap<String,String>() {{
		put(".aac","audio/aac");
		put(".abw","application/x-abiword");
		put(".arc","application/octet-stream");
		put(".avi","video/x-msvideo");
		put(".azw","application/vnd.amazon.ebook");
		put(".bin","application/octet-stream");
		put(".bz","application/x-bzip");
		put(".bz2","application/x-bzip2");
		put(".csh","application/x-csh");
		put(".css","text/css");
		put(".csv","text/csv");
		put(".doc","application/msword");
		put(".docx","application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		put(".eot","application/vnd.ms-fontobject");
		put(".epub","application/epub+zip");
		put(".gif","image/gif");
		put(".htm","text/html");
		put(".html","text/html");
		put(".ico" ,"image/x-icon");
		put(".ics","text/calendar");
		put(".jar","application/java-archive");
		put(".jpeg","image/jpeg");
		put(".jpg","image/jpeg");
		put(".js","application/javascript");
		put(".json","application/json");
		put(".mid","audio/midi");
		put(".midi","audio/midi");
		put(".mpeg","video/mpeg");
		put(".mpkg","application/vnd.apple.installer+xml");
		put(".odp","application/vnd.oasis.opendocument.presentation");
		put(".ods","application/vnd.oasis.opendocument.spreadsheet");
		put(".odt","application/vnd.oasis.opendocument.text");
		put(".oga","audio/ogg");
		put(".ogv","video/ogg");
		put(".ogx","application/ogg");
		put(".otf","font/otf");
		put(".png","image/png");
		put(".pdf","application/pdf");
		put(".ppt","application/vnd.ms-powerpoint");
		put(".pptx","application/vnd.openxmlformats-officedocument.presentationml.presentation");
		put(".rar","application/x-rar-compressed");
		put(".rtf","application/rtf");
		put(".sh","application/x-sh");
		put(".svg","image/svg+xml");
		put(".swf","application/x-shockwave-flash");
		put(".tar","application/x-tar");
		put(".tif","image/tiff");
		put(".tiff","image/tiff");
		put(".ts","application/typescript");
		put(".ttf","font/ttf");
		put(".vsd","application/vnd.visio");
		put(".wav","audio/x-wav");
		put(".weba","audio/webm");
		put(".webm","video/webm");
		put(".webp","image/webp");
		put(".woff","font/woff");
		put(".woff2","font/woff2");
		put(".xhtml","application/xhtml+xml");
		put(".xls","application/vnd.ms-excel");
		put(".xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		put(".xml","application/xml");
		put(".xul","application/vnd.mozilla.xul+xml");
		put(".zip","application/zip");
		put(".7z","application/x-7z-compressed");
	}};
	
	private Bundle bundle;

	/**
	 * Constructor
	 * 
	 * @param mediator the {@link Mediator} allowing the ResourceFilter to be 
	 * instantiated to interact with the OSGi host environment 
	 */
    public ResourceFilter(Mediator mediator) {
    	this.bundle = mediator.getContext().getBundle();
    }

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {}
	
	@Override
    public void doFilter(ServletRequest req, ServletResponse res, final FilterChain chain) throws IOException, ServletException {
    	 if (res.isCommitted()) {
             return;
         }
         String target = ((HttpServletRequest)req).getRequestURI();                 
         if (target == null) {
             target = "";
         }
         if (!target.startsWith("/")) {
             target = "/" + target;
         }
         String resName = target;
         
         int index = resName.lastIndexOf('.');                 
         String contentType = MIME.get(resName.substring(index<0?0:index));
         
         final URL url;
         synchronized(ResourceFilter.this.bundle) {
         	url = ResourceFilter.this.bundle.getEntry(resName); 
         }

         try {
	         if(url != null) {
	        	 if(contentType != null) {
	        		 ((HttpServletResponse)res).setHeader("Content-Type", contentType);
	        	 }
                 final ServletOutputStream output = res.getOutputStream();
            	 byte[] resourceBytes = IOUtils.read(url.openStream());
    	         output.write(resourceBytes);
            	 ((HttpServletResponse)res).setStatus(HttpServletResponse.SC_OK);                         
	         } else {
	        	 ((HttpServletResponse)res).setStatus(HttpServletResponse.SC_NOT_FOUND);
	         }
         } catch (Exception e) {
        	 LOG.error(e.getMessage(),e);
         }
    }
    
    @Override
    public void destroy() {}
}
