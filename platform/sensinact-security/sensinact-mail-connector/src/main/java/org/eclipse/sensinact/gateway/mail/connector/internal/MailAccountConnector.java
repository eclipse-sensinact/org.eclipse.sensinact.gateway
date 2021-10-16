/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.mail.connector.internal;

import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.security.AccountConnector;
import org.eclipse.sensinact.gateway.core.security.SecurityAccountType;
import org.eclipse.sensinact.gateway.core.security.UserUpdater;
import org.eclipse.sensinact.gateway.mail.connector.MailAccountCallback;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.CallbackContext;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.CallbackService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.util.converter.Converters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@SecurityAccountType(MailAccountConnector.ACCOUNT_TYPE)
@Component(immediate = true, service = AccountConnector.class,configurationPid = MailAccountConnector.PID,configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = MailAccountConnector.Config.class)
public class MailAccountConnector implements AccountConnector {

	Logger LOGGER = LoggerFactory.getLogger(MailAccountConnector.class);
	public static final String PID = "sensinact.mail.account.connector";

	@ObjectClassDefinition()
	interface Config {
		
		@AttributeDefinition(required = true)
		String host();

		@AttributeDefinition(required = true)
		int port();

		@AttributeDefinition(required = true)
		String from();
		
		@AttributeDefinition(defaultValue = "smtps")
		default String protocol() {
			return "smtps";
		}

		@AttributeDefinition(defaultValue = "true")
		default boolean ssl_enable() {
			return true;
		}

		@AttributeDefinition(defaultValue = "true")
		default boolean auth_enable() {
			return true;
		}

		@AttributeDefinition(defaultValue = "LOGIN PLAIN DIGEST-MD5 AUTH")
		default String auth_mechanisms() {
			return "LOGIN PLAIN DIGEST-MD5 AUTH";
		}

		@AttributeDefinition()
		default String auth_login() {
			return null;
		}

		@AttributeDefinition(type = AttributeType.PASSWORD)
		default String _auth_password() {
			return null;
		}

	}

	public static final String ACCOUNT_TYPE = "MAIL";

	private Config config = null;

	@Activate
	private BundleContext bc;
	private Session session;

	@Activate
	@Modified
	public void activate(Map<String, Object> map) {
		this.config = Converters.standardConverter().convert(map).to(Config.class);
		// Sender's email ID needs to be mentioned
		// Get system properties
		Properties	properties = System.getProperties();
		
		// Setup mail server
		properties.setProperty("mail.smtp.host", config.host());
		properties.setProperty("mail.smtp.port", Integer.toString(config.port()));
		properties.put("mail.transport.protocol", config.protocol());
		properties.setProperty("mail.smtp.auth", Boolean.toString(config.auth_enable()));
		properties.setProperty("mail.smtp.ssl.enable", Boolean.toString(config.ssl_enable()));
		properties.put("mail.smtp.auth.mechanisms", config.auth_mechanisms());
		// Get the default Session object.
		session = Session.getDefaultInstance(properties);
	}
	
	@Deactivate
	public void deactivate(Map<String, Object> map) {
		this.config = null;
		session=null;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.security.AccountConnector#connect(org.eclipse.sensinact.gateway.core.security.UserUpdater)
	 */
	@Override
	public void connect(final String token, final UserUpdater userUpdater) {

		String from = config.from();
		// Recipient's email ID needs to be mentioned.
		String to = userUpdater.getAccount();

		try {
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));

			// Set To: header field of the header.
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

			// Set Subject: header field
			message.setSubject(
					String.format("no reply: sensiNact - %s request validation", userUpdater.getUpdateType()));

			Object hostIp = bc.getProperty("org.osgi.service.http.host");
			Object hostPort = bc.getProperty("org.osgi.service.http.port.secure");
			String scheme = "https";
			if (hostPort == null) {
				hostPort = bc.getProperty("org.osgi.service.http.port");
				scheme = "http";
			}
			String linkHost = hostIp == null ? "127.0.0.1" : String.valueOf(hostIp);
			String linkPort = hostPort == null ? "80" : String.valueOf(hostPort);

			final String path = new StringBuilder().append("/").append(userUpdater.getUpdateType())
					.append(System.currentTimeMillis()).append(userUpdater.hashCode()).append(this.hashCode())
					.toString();

			final String link = new StringBuilder().append(scheme).append("://").append(linkHost).append(":")
					.append(linkPort).append(path).toString();

			// Now set the actual message
			message.setText(new StringBuilder().append(userUpdater.getMessage()).append(":\n").append(link).toString());

			// Send message
			Transport.send(message, config.auth_login(), config._auth_password());

			MailAccountCallback callback = new MailAccountCallback(path, session.getProperties(),
					new Executable<CallbackContext, Void>() {
						@Override
						public Void execute(CallbackContext context) throws Exception {
							String validation = userUpdater.validate(token);
							context.getResponse().setContent(validation.getBytes());
							context.getResponse().setResponseStatus(200);
							return null;
						}
					});
			ServiceRegistration<CallbackService> registration = bc.registerService(CallbackService.class, callback, null);
			userUpdater.setRegistration(registration);

		} catch (MessagingException mex) {
			LOGGER.error("Could not Connect",mex);
		}
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.security.AccountConnector#handle(java.lang.String)
	 */
	@Override
	public boolean handle(String accountType) {
		if (accountType == null) {
			return false;
		}
		return ACCOUNT_TYPE.equalsIgnoreCase(accountType);
	}
}
