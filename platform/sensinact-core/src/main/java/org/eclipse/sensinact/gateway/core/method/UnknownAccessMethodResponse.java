package org.eclipse.sensinact.gateway.core.method;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class UnknownAccessMethodResponse
extends AccessMethodResponse<String>
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
	 * @param mediator
	 * @param uri
	 * @param typeName
	 * @param status
	 */
	public UnknownAccessMethodResponse(Mediator mediator, String uri)
	{
		this(mediator, uri, Response.UNKNOWN_METHOD_RESPONSE, 
				Status.ERROR, 404);
	}

	/**
	 * @param mediator
	 * @param uri
	 * @param type
	 * @param status
	 * @param statusCode
	 */
	public UnknownAccessMethodResponse(Mediator mediator, String uri,
	        Response type, Status status, int statusCode)
	{
		super(mediator, uri, type, status, statusCode);
	}
	
}
