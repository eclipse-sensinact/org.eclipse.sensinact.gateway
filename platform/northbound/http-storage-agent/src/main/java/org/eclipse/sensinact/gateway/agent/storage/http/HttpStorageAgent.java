package org.eclipse.sensinact.gateway.agent.storage.http;

import java.io.IOException;
import java.util.regex.Pattern;

import org.eclipse.sensinact.gateway.agent.storage.http.internal.HttpStorageConnection;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.message.AgentRelay;
import org.eclipse.sensinact.gateway.historic.storage.agent.generic.StorageAgent;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.Logger;
import org.osgi.service.log.LoggerFactory;

@Component(immediate=true, service = {AgentRelay.class})
public class HttpStorageAgent extends StorageAgent {
	
	@Reference(service = LoggerFactory.class)
	private Logger logger;

	private static final Pattern PATTERN = Pattern.compile("^http[s]*://.*/write/measure$");

    private String login;
    private String password;
    private String broker;

	private Mediator mediator;

	@Activate
	public void activate(ComponentContext context) {
		BundleContext bc = context.getBundleContext();
		this.mediator = new Mediator(bc);
				
		this.login =  (String) mediator.getProperty("login");		
		this.password = (String) mediator.getProperty("password");
		this.broker =  (String) mediator.getProperty("broker");
		
		if(!PATTERN.matcher(this.broker).matches()) {
			context.getComponentInstance().dispose();
			return;
		}
		
		try {
			super.setStorageConnection(new HttpStorageConnection(broker, login, password));
		} catch (IOException e) {
			logger.error(l -> l.error("Could not create HttpStorageConnection", e));
			context.getComponentInstance().dispose();
		}
	}

	@Deactivate
	public void deactivate() {
		super.stop();
	}

	@Override
	protected String[] getKeyProcessorProviderIdentifiers() {
		return null;
	}
}
