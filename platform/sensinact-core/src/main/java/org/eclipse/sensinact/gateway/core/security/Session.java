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
package org.eclipse.sensinact.gateway.core.security;

import java.util.Set;

import org.eclipse.sensinact.gateway.core.SensiNactResourceModel;
import org.osgi.framework.ServiceRegistration;

import org.eclipse.sensinact.gateway.common.primitive.ElementsProxy;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.Service;
import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.util.CastUtils;


/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface Session
{	 
	//********************************************************************//
	//						NESTED DECLARATIONS		    				  //
	//********************************************************************//
	
	class Key implements UserKey
	{
		private long uid;
		private String token;
		private String publicKey;

		public Key(){}
		
		/**
		 * @return the uid
		 */
		public long getUid() {
			return uid;
		}
		/**
		 * @param uid the uid to set
		 */
		public void setUid(long uid) {
			this.uid = uid;
		}

		/**
		 * @return
		 */
		public void setPublicKey(String publicKey)
		{
			this.publicKey = publicKey;
		}
		
		/**
		 * @return
		 */
		public String getPublicKey()
		{
			return this.publicKey;
		}
		
		/**
		 * @return the token
		 */
		public String getToken() {
			return token;
		}
		/**
		 * @param token the token to set
		 */
		public void setToken(String token) {
			this.token = token;
		}
		
		/** 
		 * inheritDoc
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object object)
		{
			if(object == null)
			{
				return false;
			}
			if(object.getClass() == String.class)
			{
				return object.equals(this.token);
			}
			Double dble = CastUtils.primitiveNumberToDouble(object);
			if(dble != null)
			{
				return this.uid == dble.longValue();
			}
			if(Session.Key.class.isAssignableFrom(object.getClass()))
			{
				return this.equals(((Session.Key)object).getToken())
					|| this.equals(((Session.Key)object).getUid());
			}
			return false;
		}
	}

	//********************************************************************//
	//						ABSTRACT DECLARATIONS						  //
	//********************************************************************//
	

	//********************************************************************//
	//						STATIC DECLARATIONS		      				  //
	//********************************************************************//


	//********************************************************************//
	//						INSTANCE DECLARATIONS						  //
	//********************************************************************//

    /**
     * @return
     */
    Key getSessionKey();
    
	/**
	 * Returns the set of  {@link ServiceProvider}s accessible
	 * for this session's user
	 * 
	 * @return
	 * 		the set of accessible {@link ServiceProvider}s
	 */
	Set<ServiceProvider> getServiceProviders();
	
	/**
	 * @param uri
	 * @return
	 */
    <S extends ElementsProxy<?>> S  getFromUri(String uri);
    
    /**
     * @param serviceProviderName
     * @return
     */
    ServiceProvider getServiceProvider(String serviceProviderName);

    /**
     * @param serviceProviderName
     * @param serviceName
     * @return
     */
    Service getService(String serviceProviderName, String serviceName);

    /**
     * @param serviceProviderName
     * @param serviceName
     * @param resourceName
     * @return
     */
     Resource getResource(String serviceProviderName, String serviceName,
    		 String resourceName);  

	/**
	 * Registers the {@link SensiNactResourceModel} passed
	 * as parameter in the OSGi host environment
	 * 
	 * @param modelInstance the {@link SensiNactResourceModel}
	 * to be registered
	 *
	 * @return 
	 * @throws SecuredAccessException 
	 */
	ServiceRegistration<SensiNactResourceModel> register(
		SensiNactResourceModel<?> modelInstance) 
			throws SecuredAccessException;

	/**
	 * Unregisters the {@link SensiNactResourceModel} passed 
	 * as parameter from the OSGi host environment
	 * 
	 * @param modelInstance the {@link SensiNactResourceModel}
	 * to unregister
	 * 
	 * @throws SecuredAccessException 
	 */
	void unregister(ServiceRegistration<SensiNactResourceModel> registration) 
			throws SecuredAccessException;   
}
