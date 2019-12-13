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
package org.eclipse.sensinact.gateway.app.manager.application;

import org.eclipse.sensinact.gateway.api.core.Core;
import org.eclipse.sensinact.gateway.api.message.MessageCallback;
import org.eclipse.sensinact.gateway.api.message.ErrorMessage;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.Session;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractSensiNactApplication implements SensiNactApplication {
    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//

    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//

    /**
     * @return
     */
    protected abstract ErrorMessage doStart();


    /**
     * @return
     */
    protected abstract ErrorMessage doStop();

    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//

    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

    /**
     * the {@link Mediator} allowing to interact with
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
     * @param mediator   the {@link Mediator} allowing the {@link
     *                   SensiNactApplication} to be built to interact with
     *                   the OSGi host environment
     * @param privateKey the private String identifier of the
     *                   {@link SensiNactApplication} to be built
     */
    protected AbstractSensiNactApplication(final Mediator mediator, String name, final String privateKey) {
        this.session = mediator.callService(Core.class, new Executable<Core, Session>() {
            @Override
            public Session execute(Core core) throws Exception {
                return core.getApplicationSession(mediator, privateKey);
            }
        });
        this.mediator = mediator;
        this.name = name;
        this.privateKey = privateKey;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.app.manager.application.SensiNactApplication#start()
     */
    @Override
    public ErrorMessage start() {
        //this.session = mediator.callService(Core.class,
        //		new Executable<Core,Session>()
        //{
        //	@Override
        //	public Session execute(Core core) throws Exception
        //	{
        //		return core.getApplicationSession(mediator,
        //				privateKey);
        //	}
        //});
        return doStart();
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.app.manager.application.SensiNactApplication#stop()
     */
    @Override
    public ErrorMessage stop() {
        ErrorMessage message = this.doStop();
        //this.session = null;
        return message;
    }

    /**
     * Returns the {@link Session} for this {@link SensiNactApplication}
     *
     * @return this {@link SensiNactApplication}'s {@link Session}
     */
    public Session getSession() {
        return this.session;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.common.primitive.Nameable#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.api.message.Recipient#getSnaCallBackType()
     */
    public MessageCallback.Type getSnaCallBackType() {
        return MessageCallback.Type.UNARY;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.api.message.Recipient#getLifetime()
     */
    public long getLifetime() {
        return MessageCallback.ENDLESS;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.api.message.Recipient#getBufferSize()
     */
    public int getBufferSize() {
        return 0;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.api.message.Recipient#getSchedulerDelay()
     */
    public int getSchedulerDelay() {
        return 0;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.common.primitive.JSONable#getJSON()
     */
    public String getJSON() {
        return null;
    }

}
