package org.eclipse.sensinact.gateway.core;

/**
 * Result wrapper for Core's and Session's methods call 
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ResultHolder<R>
{
	/**
	 * the integer value defining the response 
	 * status (based on HTTP status)
	 */
	protected int statusCode;
	
	/**
	 * the effective response object
	 */
	protected R result;
	
	/**
	 * Constructor
	 *  
	 * @param statusCode the integer status of the response
	 * that will be wrapped by the ResultCap to be
	 * instantiated		
	 * @param result the object result that will be wrapped 
	 * by the ResultCap to be instantiated
	 */
	ResultHolder(int statusCode, R result)
	{
		this.statusCode = statusCode;
		this.result = result;
	}
	
	/**
	 * Returns the integer value defining the wrapped 
	 * response status
	 * 
	 * @return the wrapped response status
	 */
	public int getStatusCode()
	{
		return this.statusCode;
	}
	
	/**
	 * Returns the effective wrapped response object
	 *  
	 * @return the response object
	 */
	public R getResult()
	{
		return this.result;
	}
}