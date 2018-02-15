package org.eclipse.sensinact.gateway.app.manager.application;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.message.MidCallback;
import org.eclipse.sensinact.gateway.core.message.SnaErrorMessage;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractSensiNactApplication implements SensiNactApplication
{
	//********************************************************************//
	//						NESTED DECLARATIONS			  			      //
	//********************************************************************//

	//********************************************************************//
	//						ABSTRACT DECLARATIONS						  //
	//********************************************************************//
   
	/**
     * @return
     */
    protected abstract SnaErrorMessage doStart();
     
     
     /**
     * @return
     */
    protected abstract SnaErrorMessage doStop();
     
	//********************************************************************//
	//						STATIC DECLARATIONS							  //
	//********************************************************************//

	//********************************************************************//
	//						INSTANCE DECLARATIONS						  //
	//********************************************************************//

	/**
	 *  the {@link Mediator} allowing to interact with
	 * the OSGi host environment
	 */
	protected Mediator mediator;
	
	/**
	 * this {@link SensiNactApplication}'s private String identifier 
	 */
	private final String privateKey;

	/**
	 * this {@link SensiNactApplication}'s name
	 */
	private final String name;

	/**
	 * this SensiNactApplication's execution Session
	 */
	private Session session;
	
	/**
	 * Constructor
	 * 
	 * @param mediator the {@link Mediator} allowing the {@link 
	 * SensiNactApplication} to be built to interact with
	 * the OSGi host environment
	 * @param privateKey the private String identifier of the  
	 * {@link SensiNactApplication} to be built
	 */
	protected AbstractSensiNactApplication(
			Mediator mediator, 
			String name, 
			String privateKey)
	{
		this.mediator = mediator;
		this.name = name;
		this.privateKey = privateKey;
	}
	
	/**
	 * 
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.app.manager.application.SensiNactApplication#start()
	 */
	@Override
	public SnaErrorMessage start()
	{
		this.session = mediator.callService(Core.class, 
				new Executable<Core,Session>() 
		{
			@Override
			public Session execute(Core core) throws Exception
			{
				return core.getApplicationSession(mediator, 
						privateKey);
			}
		});
		return doStart();
	}
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.app.manager.application.SensiNactApplication#stop()
	 */
	@Override
	public SnaErrorMessage stop()
	{
		SnaErrorMessage message = this.doStop();
		this.session = null;
		return message;
	}	
	
    /**
     * Returns the {@link Session} for this {@link SensiNactApplication}
     * 
     * @return this {@link SensiNactApplication}'s {@link Session}
     */
    public Session getSession()
    {
    	return this.session;
    }
    
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.common.primitive.Nameable#getName()
	 */
	@Override
	public String getName()
	{
		return this.name;
	}

    /**
     * @inheritDoc
     *
     * @see org.eclipse.sensinact.gateway.core.message.Recipient#getSnaCallBackType()
     */
    public MidCallback.Type getSnaCallBackType() {
        return MidCallback.Type.UNARY;
    }

    /**
     * @inheritDoc
     *
     * @see org.eclipse.sensinact.gateway.core.message.Recipient#getLifetime()
     */
    public long getLifetime() 
    {
        return MidCallback.ENDLESS;
    }

    /**
     * @inheritDoc
     *
     * @see org.eclipse.sensinact.gateway.core.message.Recipient#getBufferSize()
     */
    public int getBufferSize() 
    {
        return 0;
    }

    /**
     * @inheritDoc
     *
     * @see org.eclipse.sensinact.gateway.core.message.Recipient#getSchedulerDelay()
     */
    public int getSchedulerDelay() 
    {
        return 0;
    }
    
    /**
     * @inheritDoc
     *
     * @see org.eclipse.sensinact.gateway.common.primitive.JSONable#getJSON()
     */
    public String getJSON() 
    {
        return null;
    }

}
