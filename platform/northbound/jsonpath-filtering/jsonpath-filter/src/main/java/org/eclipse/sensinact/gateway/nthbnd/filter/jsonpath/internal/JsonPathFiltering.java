package org.eclipse.sensinact.gateway.nthbnd.filter.jsonpath.internal;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.Filtering;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.json.JSONObject;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class JsonPathFiltering implements Filtering
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

    private static final Deque<DocumentContext> stack = new ArrayDeque<DocumentContext>();

	//********************************************************************//
	//						INSTANCE DECLARATIONS						  //
	//********************************************************************//

    private final ExecutorService ex = Executors.newFixedThreadPool(1);

	private Mediator mediator;

	/**
	 * @param mediator
	 */
	public JsonPathFiltering(Mediator mediator)
	{
		this.mediator = mediator;
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Filtering#handle(java.lang.String)
	 */
	@Override
	public boolean handle(String type)
	{
		return "jsonpath".equals(type);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Filtering#apply(java.lang.String, java.lang.Object)
	 */
	@Override
	public <F> F apply(String filter, F result)
	{
		if(filter == null)
		{
			return result;
		}
		final String json = String.valueOf(result);
	    try
	    {
	    	DocumentContext dc = null;
	        mediator.debug("Looking up for JsonPath string %s", filter);
	
	        if(stack.size() == 0)
	        {
	        	dc = JsonPath.parse(json);
	            stack.addFirst(dc);
	            
	        } else
	        {
	            ex.submit(new Runnable()
	            {
	                @Override
	                public void run() 
	                {
	                    DocumentContext dc = JsonPath.parse(json);
	                    stack.addFirst(dc);
	                    stack.removeLast();
	                }
	            });
	        }
	        dc = stack.getFirst();
	        Object object = dc.read(filter);
	        return CastUtils.cast(mediator.getClassLoader(),
	        	(Class<F>)result.getClass(), object);
	        
	    } catch(Exception e)
	    {
	        mediator.error("Failed to process JsonPath", e);
	        throw e;
	    }
	}
}
