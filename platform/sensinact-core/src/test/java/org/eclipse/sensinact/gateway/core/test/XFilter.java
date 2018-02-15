package org.eclipse.sensinact.gateway.core.test;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.Filtering;
import org.eclipse.sensinact.gateway.util.CastUtils;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class XFilter implements Filtering
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

	/**
	 * 
	 */
	public XFilter()
	{
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Filtering#handle(java.lang.String)
	 */
	@Override
	public boolean handle(String type)
	{
		return "xfilter".equals(type);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Filtering#apply(java.lang.String, java.lang.Object)
	 */
	@Override
	public <F> F apply(String filter, F result)
	{
		String str = String.valueOf(result);
		char flt = filter.charAt(0);
				
		F f = CastUtils.cast(Thread.currentThread().getContextClassLoader(), 
		 (Class<F>) result.getClass(), str.replace(flt, 'X'));
		return f;
	}
}
