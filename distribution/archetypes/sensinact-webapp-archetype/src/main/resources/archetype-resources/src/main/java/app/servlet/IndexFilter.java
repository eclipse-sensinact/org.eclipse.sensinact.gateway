package  ${package}.app.servlet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import ${package}.WebAppConstants;

/**
 * Redirect to index.html page
 */
@WebFilter( urlPatterns= {WebAppConstants.WEBAPP_ROOT}, asyncSupported=false)
public class IndexFilter implements Filter {
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {}
	
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ((HttpServletResponse) response).sendRedirect(WebAppConstants.WEBAPP_ROOT + "/index.html");
    }
    
    @Override
    public void destroy() {}
}
