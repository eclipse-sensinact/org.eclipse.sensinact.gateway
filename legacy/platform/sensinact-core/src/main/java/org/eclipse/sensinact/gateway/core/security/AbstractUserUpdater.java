/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.security;

import java.util.Timer;
import java.util.TimerTask;

import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link UserUpdater} abstract implementation
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractUserUpdater implements UserUpdater{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractUserUpdater.class);

	/**
	 * @return
	 * @throws SecuredAccessException
	 */
	protected abstract String doUpdate() throws SecuredAccessException;
	
	private final String token;
	private final String updateType;
	
	private ServiceRegistration<?> registration;
	private Timer timer;

	/**
	 * Constructor 
	 * @param token 
	 * 		the String token validating the operation held by the {@link UserUpdater}
	 * 		to be created
	 * @param updateType 
	 * 		the String operation held by the {@link UserUpdater} to be created
	 */
	protected AbstractUserUpdater(String token, String updateType){
		this.token = token;
		if(this.token == null) {
			throw new NullPointerException("Token required");
		}
		this.updateType = updateType;
		if(this.updateType == null) {
			throw new NullPointerException("Operation required");
		}
	}

	@Override
	public void setRegistration(ServiceRegistration<?> registration) {
		this.registration = registration;
		if(this.registration == null) {
			return;
		}
		this.timer = new Timer();
		this.timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if(AbstractUserUpdater.this.registration == null) {
					return;
				}
				try {
					AbstractUserUpdater.this.registration.unregister();
				}catch(IllegalStateException e) {
					LOG.error(e.getMessage(),e);
				}
			}
		}, 1000*60*60);		
	}

	@Override
	public String validate(String token) throws SecuredAccessException {
		if(this.timer != null) {
			this.timer.cancel();
			this.timer.purge();
			this.timer = null;
		}		
		StringBuilder builder = new StringBuilder();
		builder.append("'");
		builder.append(this.getUpdateType());
		builder.append("'");
		builder.append(" operation ");
		if(this.token.equals(token)) {
			String update = doUpdate();
			if(registration != null) {
				try{
					registration.unregister();
				} catch(IllegalStateException e) {
					LOG.error(e.getMessage(),e);
				}
			}		
			builder.append("validated");
			builder.append("\n");
			builder.append(update);
			return builder.toString();
		}
		builder.append("not validated");
		return builder.toString();
	}

	@Override
	public String getMessage() {
		StringBuilder builder = new StringBuilder();
		builder.append("Follow the link below to validate the '");
		builder.append(this.getUpdateType());
		builder.append("'");
		builder.append(" operation ");
		return builder.toString();
	}

	@Override
	public String getUpdateType() {
		return this.updateType;
	}		
}