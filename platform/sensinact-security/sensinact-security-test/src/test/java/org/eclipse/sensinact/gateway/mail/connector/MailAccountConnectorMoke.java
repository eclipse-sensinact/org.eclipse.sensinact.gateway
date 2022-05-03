/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.mail.connector;

import java.util.Hashtable;
import java.util.Properties;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.security.AccountConnector;
import org.eclipse.sensinact.gateway.core.security.UserUpdater;
import org.eclipse.sensinact.gateway.core.security.test.api.MailAccountConnectorMailReplacement;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.CallbackContext;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.CallbackService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@Component(property = "org.eclipse.sensinact.security.account.type=" + MailAccountConnectorMoke.ACCOUNT_TYPE)
public class MailAccountConnectorMoke implements AccountConnector {
    
	private static final Logger LOG = LoggerFactory.getLogger(MailAccountConnectorMoke.class);
	public static final String ACCOUNT_TYPE = "MAIL";
	
	private Mediator mediator;
	
	/**
	 * Constructor
	 * 
	 * @param mediator
	 * 		The {@link Mediator} allowing the EmailAccountConnector 
	 * 		to be instantiated to interact with the OSGi host environment
	 */
	@Activate
	public MailAccountConnectorMoke(BundleContext context){
		this.mediator = new Mediator(context);
	}

	@Override
	public void connect(final String token, final UserUpdater userUpdater){
	  // Recipient's email ID needs to be mentioned.
      String to = userUpdater.getAccount();
      String host = (String)mediator.getProperty("mail.account.connector.host");
      String port = (String)mediator.getProperty("mail.account.connector.port");

      // Sender's email ID needs to be mentioned
      String from = (String)mediator.getProperty("mail.account.connector.from");
      // Get system properties
      Properties properties = System.getProperties();

      // Setup mail server
      properties.setProperty("mail.smtp.host", host);
      properties.setProperty("mail.smtp.port", port);
      properties.put("mail.transport.protocol", "smtps");
      properties.setProperty("mail.smtp.auth","true"); 
      properties.setProperty("mail.smtp.ssl.enable","true");
      properties.put("mail.smtp.auth.mechanisms","LOGIN PLAIN DIGEST-MD5 AUTH");

      try {
    	  
         Object hostIp = mediator.getProperty("org.osgi.service.http.host");
         Object hostPort = mediator.getProperty("org.osgi.service.http.port.secure");
         String scheme = "https";
         if(hostPort == null) {
             hostPort = mediator.getProperty("org.osgi.service.http.port");
             scheme = "http";
         }
         String linkHost = hostIp == null?"127.0.0.1":String.valueOf(hostIp);
         String linkPort = hostPort == null?"80":String.valueOf(hostPort);
         
         final String path = new StringBuilder().append("/").append(userUpdater.getUpdateType()).append(
                System.currentTimeMillis()).append(userUpdater.hashCode()).append(
                		this.hashCode()).toString();
         
         final String link =  new StringBuilder().append(scheme).append("://").append(linkHost).append(":"
        	).append(linkPort).append(path).toString();
          
         MailAccountCallbackMoke callback = new MailAccountCallbackMoke(path, new Hashtable(), 
    		new Executable<CallbackContext, Void>() {
				@Override
				public Void execute(CallbackContext context) throws Exception {
					String validation = userUpdater.validate(token);
					context.getResponse().setContent(validation.getBytes());
					context.getResponse().setResponseStatus(200);
					context.getResponse().flush();
					return null;
				}
			}
         );

         this.mediator.register(new MailAccountConnectorMailReplacement(){
			@Override
			public String getMailDetails() {
				return new StringBuilder().append(userUpdater.getMessage()
		        		 ).append(":\n").append(link).toString();
			}}, MailAccountConnectorMailReplacement.class, null);         
         
         ServiceRegistration<?> registration = this.mediator.getContext().registerService(
        	 new String[] {CallbackService.class.getCanonicalName()},	 callback, new Hashtable<String, Object>() );

         userUpdater.setRegistration(registration);

      } catch (Exception mex) {
    	  mex.printStackTrace();
          LOG.error(mex.getMessage(),mex);
      }
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.security.AccountConnector#handle(java.lang.String)
	 */
	@Override
	public boolean handle(String accountType) {
		if(accountType == null){
			return false;
		}
		return ACCOUNT_TYPE.equalsIgnoreCase(accountType);
	}
}
