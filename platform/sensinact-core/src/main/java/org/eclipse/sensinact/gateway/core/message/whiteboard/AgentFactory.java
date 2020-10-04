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
package org.eclipse.sensinact.gateway.core.message.whiteboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.constraint.ConstraintFactory;
import org.eclipse.sensinact.gateway.common.constraint.InvalidConstraintDefinitionException;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.message.AbstractMidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.AgentRelay;
import org.eclipse.sensinact.gateway.core.message.MessageFilterDefinition;
import org.eclipse.sensinact.gateway.core.message.MidCallbackException;
import org.eclipse.sensinact.gateway.core.message.SnaErrorMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.core.message.SnaRemoteMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaResponseMessage;
import org.eclipse.sensinact.gateway.core.message.SnaUpdateMessageImpl;
import org.eclipse.sensinact.gateway.core.message.annotation.Filter;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * A AgentFactory is in charge of asking for the registration of an {@link Agent}  
 * to the sensiNact {@link Core} service, and configured by the {@link AgentRelay}
 * registered in the OSGi host environment
 *
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
@Component(immediate=true)
public class AgentFactory {
	
	private final class EmptyFilterDefinition implements MessageFilterDefinition {

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.api.message.FilterDefinition#handledTypes()
		 */
		@Override
		public SnaMessage.Type[] handledTypes() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.api.message.FilterDefinition#conditions()
		 */
		@Override
		public List<Constraint> conditions() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.api.message.FilterDefinition#sender()
		 */
		@Override
		public String sender() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.api.message.FilterDefinition#isPattern()
		 */
		@Override
		public boolean isPattern() {
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.api.message.FilterDefinition#isComplement()
		 */
		@Override
		public boolean isComplement() {
			return false;
		}
	}

	private final class AnnotationFilterDefinition implements MessageFilterDefinition {

		private Filter filter;

		AnnotationFilterDefinition(Filter filter){
			this.filter = filter;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.api.message.FilterDefinition#handledTypes()
		 */
		@Override
		public SnaMessage.Type[] handledTypes() {
			SnaMessage.Type[] arr = filter.handled();
			if(arr == null || arr.length == 0) {
				arr = SnaMessage.Type.values();
			}
			return arr;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.api.message.FilterDefinition#conditions()
		 */
		@Override
		public List<Constraint> conditions() {
			String[] cds = filter.conditions();
			if(cds != null && cds.length > 0) {
				List<Constraint> constraints = new ArrayList<>();
				ClassLoader cl = mediator.getClassLoader();
				for(String s : cds) {
					try {
					    JSONObject constraint = new JSONObject(s);
					    constraints.add(ConstraintFactory.Loader.load(cl, constraint));
					} catch(JSONException | InvalidConstraintDefinitionException e) {
						mediator.error("Unable to read attached constraints", e);
						constraints.clear();
						break;
					}
				}
				if(!constraints.isEmpty()) {
					return constraints;
				}
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.api.message.FilterDefinition#sender()
		 */
		@Override
		public String sender() {
			return filter.sender();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.api.message.FilterDefinition#isPattern()
		 */
		@Override
		public boolean isPattern() {
			return filter.isPattern();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.api.message.FilterDefinition#isComplement()
		 */
		@Override
		public boolean isComplement() {
			return filter.isComplement();
		}		
	}
	
	private final class AgentRelayWrapper implements AgentRelay, MessageFilterDefinition {
		
		private AgentRelay agentRelay;
		private MessageFilterDefinition filterDefinition;
		
		/**
		 * Constructor
		 * 
		 * @param agentRelay the {@link AgentRelay} wrapped by the
		 * AgentRelayWrapper to be instantiated
		 * 
		 */
		protected AgentRelayWrapper(AgentRelay agentRelay) {
			this.agentRelay = agentRelay;
			if(this.agentRelay instanceof MessageFilterDefinition) {
				this.filterDefinition = (MessageFilterDefinition) this.agentRelay ;
			} else {
				final Filter filter = this.agentRelay.getClass().getAnnotation(Filter.class);
				if(filter == null) {
					this.filterDefinition = new EmptyFilterDefinition();
				} else {
					this.filterDefinition =  new AnnotationFilterDefinition(filter);
				}
			}
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.core.message.AbstractMidAgentCallback#propagate()
		 */
		@Override
		public boolean propagate() {
			return agentRelay.propagate();
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.core.message.AbstractMidAgentCallback#doHandle(org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessageImpl)
		 */
		@Override
		public void doHandle(SnaLifecycleMessageImpl message) throws MidCallbackException {
			agentRelay.doHandle(message);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.core.message.AbstractMidAgentCallback#doHandle(org.eclipse.sensinact.gateway.core.message.SnaUpdateMessageImpl)
		 */
		@Override
		public void doHandle(SnaUpdateMessageImpl message) throws MidCallbackException {
			agentRelay.doHandle(message);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.core.message.AbstractMidAgentCallback#doHandle(org.eclipse.sensinact.gateway.core.message.SnaRemoteMessageImpl)
		 */
		@Override
		public void doHandle(SnaRemoteMessageImpl message) throws MidCallbackException {
			agentRelay.doHandle(message);	
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.core.message.AbstractMidAgentCallback#doHandle(org.eclipse.sensinact.gateway.core.message.SnaErrorMessageImpl)
		 */
		@Override
		public void doHandle(SnaErrorMessageImpl message) throws MidCallbackException {
			agentRelay.doHandle(message);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.core.message.AbstractMidAgentCallback#doHandle(org.eclipse.sensinact.gateway.core.message.SnaResponseMessage)
		 */
		@Override
		public void doHandle(SnaResponseMessage<?, ?> message) throws MidCallbackException {
			agentRelay.doHandle(message);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.core.message.AgentRelay#getRelayIdentifier()
		 */
		@Override
		public String getRelayIdentifier() {
			return agentRelay.getRelayIdentifier();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.api.message.AgentRelay#lifetime()
		 */
		@Override
		public long lifetime() {
			return agentRelay.lifetime();
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.api.message.FilterDefinition#handledTypes()
		 */
		@Override
		public SnaMessage.Type[] handledTypes() {
			return this.filterDefinition.handledTypes();
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.api.message.FilterDefinition#conditions()
		 */
		@Override
		public List<Constraint> conditions() {
			return this.filterDefinition.conditions();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.api.message.FilterDefinition#sender()
		 */
		@Override
		public String sender() {
			return this.filterDefinition.sender();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.api.message.FilterDefinition#isPattern()
		 */
		@Override
		public boolean isPattern() {
			return this.filterDefinition.isPattern();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.api.message.FilterDefinition#isComplement()
		 */
		@Override
		public boolean isComplement() {
			return this.filterDefinition.isComplement();
		}
	}

	private final class AgentCallback extends AbstractMidAgentCallback  {
		
		AgentRelay agentRelay;
		
		/**
		 * Constructor
		 * 
		 * @param agentRelay the {@link AgentRelay} wrapped by the
		 * AgentRelayWrapper to be instantiated
		 * 
		 */
		protected AgentCallback(AgentRelay agentRelay) {
			super(true,true,ID_GENERATOR(agentRelay));
			this.agentRelay = agentRelay;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.core.message.AbstractMidAgentCallback#propagate()
		 */
		@Override
		public boolean propagate() {
			return agentRelay.propagate();
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.core.message.AbstractMidAgentCallback#doHandle(org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessageImpl)
		 */
		@Override
		public void doHandle(SnaLifecycleMessageImpl message) throws MidCallbackException {
			agentRelay.doHandle(message);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.core.message.AbstractMidAgentCallback#doHandle(org.eclipse.sensinact.gateway.core.message.SnaUpdateMessageImpl)
		 */
		@Override
		public void doHandle(SnaUpdateMessageImpl message) throws MidCallbackException {
			agentRelay.doHandle(message);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.core.message.AbstractMidAgentCallback#doHandle(org.eclipse.sensinact.gateway.core.message.SnaRemoteMessageImpl)
		 */
		@Override
		public void doHandle(SnaRemoteMessageImpl message) throws MidCallbackException {
			agentRelay.doHandle(message);	
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.core.message.AbstractMidAgentCallback#doHandle(org.eclipse.sensinact.gateway.core.message.SnaErrorMessageImpl)
		 */
		@Override
		public void doHandle(SnaErrorMessageImpl message) throws MidCallbackException {
			agentRelay.doHandle(message);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.core.message.AbstractMidAgentCallback#doHandle(org.eclipse.sensinact.gateway.core.message.SnaResponseMessage)
		 */
		@Override
		public void doHandle(SnaResponseMessage<?, ?> message) throws MidCallbackException {
			agentRelay.doHandle(message);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.core.message.AbstractMidCallback#setTimeout(long)
		 */
		@Override
		protected void setTimeout(long timeout) {
			super.setTimeout(timeout);
		}
	}
	
	private static final String ID_GENERATOR(AgentRelay agentRelay) {
		String ari = null;
		if(agentRelay.getRelayIdentifier() == null) {
			ari = new StringBuilder().append("ARI_").append(
			System.currentTimeMillis()).append(agentRelay.hashCode()).toString();			
		} else {
			ari = agentRelay.getRelayIdentifier();
		}
		return ari;
	}
	
    private Mediator mediator;
    private String appearingKey;
    private String disappearingKey;
    private Map<String, AgentCallback> registrations;

    private final AtomicBoolean running;

    /**
     * Constructor
     */
    public AgentFactory() {
        this.registrations = Collections.synchronizedMap(new HashMap<String, AgentCallback>());
        this.running = new AtomicBoolean(false);
    }

    /**
     * Starts this AgentFactory and starts to observe the registration and
     * the unregistration of the {@link AgentRelay}s
     */
    @Activate
    public void activate(ComponentContext context) {

        this.mediator = new Mediator(context.getBundleContext());
        if (this.running.get()) {
            return;
        }
        this.running.set(true);
        attachAll();
        this.appearingKey = mediator.attachOnServiceAppearing(AgentRelay.class, (String) null, new Executable<AgentRelay, Void>() {
            @Override
            public Void execute(AgentRelay agentRelay) throws Exception {
                attach(agentRelay);
                return null;
            }
        });
        this.disappearingKey = mediator.attachOnServiceDisappearing(AgentRelay.class, (String) null, new Executable<AgentRelay, Void>() {
            @Override
            public Void execute(AgentRelay agentRelay) throws Exception {
                detach(agentRelay);
                return null;
            }
        });
    }

    /**
     * Stops this AgentFactory and stops to observe the registration and
     * the unregistration of the {@link AgentRelay}s
     */
    @Deactivate
    public void deactivate() {
        if (!this.running.get()) {
            return;
        }
        this.running.set(false);
        mediator.detachOnServiceAppearing(AgentRelay.class, (String) null, appearingKey);
        mediator.detachOnServiceDisappearing(AgentRelay.class, (String) null, disappearingKey);
        detachAll();
    }

    /**
     * Detaches all the {@link AgentRelay}s registered into the
     * OSGi host environment
     */
    public void detachAll() {
        mediator.callServices(AgentRelay.class, new Executable<AgentRelay, Void>() {
            /* (non-Javadoc)
             * @see org.eclipse.sensinact.gateway.common.execution.Executable#execute(java.lang.Object)
             */
            @Override
            public Void execute(AgentRelay agentRelay) throws Exception {
                detach(agentRelay);
                return null;
            }
        });
    }

    /**
     * Attaches all the {@link AgentRelay}s registered into the
     * OSGi host environment
     */
    public void attachAll() {
        mediator.callServices(AgentRelay.class, new Executable<AgentRelay, Void>() {            
            /* (non-Javadoc)
             * @see org.eclipse.sensinact.gateway.common.execution.Executable#execute(java.lang.Object)
             */
            @Override
            public Void execute(AgentRelay agentRelay) throws Exception {
                attach(agentRelay);
                return null;
            }
        });
    }

    /**
     * Attaches the {@link AgentRelay} passed as parameter by
     * registering a newly created {@link Agent} based on it
     *
     * @param agentRelay the {@link AgentRelay} to be attached
     */
	public final void attach(AgentRelay agentRelay) {
        if (agentRelay == null || !this.running.get()) {
            return;
        }     
        String id = new StringBuilder().append("relay_").append(this.hashCode()
        	).append(agentRelay.hashCode()).toString();
        
        
        if (registrations.containsKey(id)) {
            mediator.error("An AgentRelay is already registered with ID '%s'", id);
            return;
        }
        AgentRelayWrapper wrapper = new AgentRelayWrapper(agentRelay);   
        //retrieve filtering data 
	    boolean defined = false;
	    boolean isPattern = wrapper.isPattern();
	    boolean isComplement = wrapper.isComplement();
	    String sender = wrapper.sender();
	    if(sender == null) {
	    	sender = "(/[^/]+)+";
	    	isPattern = true;
	    } else {
	    	defined = true;
	    }
	    List<Constraint> constraints = wrapper.conditions();
	    if(constraints == null) {
	    	constraints = new ArrayList<>();
	    } else {
	    	defined = true;
	    }
	    SnaMessage.Type[] handled = wrapper.handledTypes();
	    if(handled == null) {
	    	handled = SnaMessage.Type.values();
	    } else {
	    	defined = true;
	    }	    
	    final SnaFilter filter;
	    if(defined) {
	    	filter = new SnaFilter(mediator, sender, isPattern, isComplement); 
	    	for(Constraint cn: constraints) {
	    		filter.addCondition(cn);
	    	}
	    	filter.addHandledType(handled);
	    } else {
	    	filter = null;
	    }
        final AgentCallback callback = new AgentCallback(wrapper);
        if(wrapper.lifetime() > 0) {
        	callback.setTimeout(System.currentTimeMillis()+wrapper.lifetime());
        }
        mediator.callService(Core.class, new Executable<Core,String>(){
			@Override
			public String execute(Core core) throws Exception {		
				return core.registerAgent(mediator, callback, filter);
			}
        });
	    this.registrations.put(id, callback);
    }

    /**
     * Detaches the {@link AgentRelay} passed as parameter by
     * unregistering the {@link Agent} that is based on it
     *
     * @param agentRelay the {@link AgentRelay} to be detached
     */
    public final void detach(AgentRelay agentRelay) {
        if (agentRelay == null) {
            return;
        }
        String id = new StringBuilder().append("relay_").append(this.hashCode()
            ).append(agentRelay.hashCode()).toString();
        
        AgentCallback callback = this.registrations.remove(id);
    	if(callback != null) {
    		try {
    			callback.stop();
                mediator.info("Agent callback '%s' unregistered", id);
    		}catch(IllegalStateException e) {
    			//do nothing
    		}
    		callback = null;
    	}
    }
}
