package org.eclipse.sensinact.gateway.app.manager.application.dependency;

import org.eclipse.sensinact.gateway.app.manager.application.Application;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessage;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.DescribeResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Monitors an Application (At application manager context) in order to notify a proper manager to start or stop the application according to the dependencies available
 */
public class DependencyManager extends DependencyManagerAbstract {
    private static Logger LOG = LoggerFactory.getLogger(DependencyManager.class);
    private final Application application;
    private final Mediator mediator;
    private final Map<String, Boolean> dependenciesURIMap = new HashMap<String, Boolean>();
    private final Map<String, String> agentsIdDependency = new HashMap<String, String>();
    private final DependencyManagerCallback callback;
    private Boolean active = true;
    protected Core core;

    public DependencyManager(Application application, Mediator mediator, Collection<String> dependenciesURI, DependencyManagerCallback callback) {
        super(String.format("%s-dependencies", application.getName()));
        this.application = application;
        this.mediator = mediator;
        this.callback = callback;
        for (final String resourceUri : dependenciesURI) {
            mediator.callService(Core.class, new Executable<Core, Void>() {
                @Override
                public Void execute(Core core) {
                    DependencyManager.this.core = core;
                    final String[] uriSplit = resourceUri.split("/");
                    final String provider = uriSplit[1];
                    final String service = uriSplit[2];
                    final String resource = uriSplit[3];
                    final DescribeResponse response = DependencyManager.this.application.getSession().getResource(provider, service, resource);
                    dependenciesURIMap.put(resourceUri, response.getStatus() == AccessMethodResponse.Status.SUCCESS);
                    evaluateDependencySatisfied();
                    return null;
                }
            });
        }
    }

    private Boolean isAllDependenciesAvailable() {
        for (Map.Entry<String, Boolean> entryDependency : dependenciesURIMap.entrySet()) {
            if (!entryDependency.getValue()) return false;
        }
        return true;
    }

    protected void evaluateDependencySatisfied() {
        if (active) {
            if (isAllDependenciesAvailable()) {
                try {
                    LOG.debug("Application '{}', all dependencies satisfied, notifying manager to start Application", application.getName());
                    callback.ready(this.application.getName());
                } catch (Exception e) {
                    LOG.warn("Application dependencies satistied, notification reception failed.", e);
                }
            } else {
                try {
                    LOG.debug("Application '{}', some dependencies are missing, notifying manager to stop Application", application.getName());
                    callback.unready(this.application.getName());
                } catch (Exception e) {
                    LOG.warn("Application dependencies NOT satistied any longer, notification reception failed.", e);
                }
            }
        }
    }

    @Override
    public void doHandle(SnaLifecycleMessageImpl message) {
        LOG.debug("Application deployed '{}' reading event {}", application.getName(), message.getJSON());
        JSONObject messageJson = new JSONObject(message.getJSON());
        final String messageType = messageJson.getString("type");
        if (messageType.equals(SnaLifecycleMessage.Lifecycle.RESOURCE_APPEARING.toString())) {
            LOG.debug("Application '{}' taking into account the availability of resource '{}'", application.getName(), message.getPath());
            dependenciesURIMap.put(message.getPath(), true);
            evaluateDependencySatisfied();
        } else if (messageType.equals(SnaLifecycleMessage.Lifecycle.RESOURCE_DISAPPEARING.toString())) {
            LOG.debug("Application '{}' taking into account the unavailability of resource '{}'", application.getName(), message.getPath());
            dependenciesURIMap.put(message.getPath(), false);
            evaluateDependencySatisfied();
        }
    }

    public void stop() {
        active = false;
        LOG.debug("Stopping Application Dependency Manager for application '{}'", application.getName());
        /*
        //The correct would be to remove the agents on stop, but the core does not allow does from perspective point of view,
        //thus, for now he is keeping the agent alive but does not generate new agents when the application is back alive
        for(Map.Entry<String,String> entry:agentsIdDependency.entrySet()){
            try {
                String agentId=entry.getValue();
                core.unregisterAgent(agentId);
            }catch(Exception e){
                e.printStackTrace();
            }finally {
                    DependencyManager.this.agentsIdDependency.clear();
            }
        }
        */
    }

    public void start() {
        active = true;
        LOG.debug("Starting to Application Dependency Manager for application '{}'", application.getName());
        for (String resourceUri : dependenciesURIMap.keySet()) {
            final SnaFilter filter = new SnaFilter(mediator, resourceUri, false, false);
            filter.addHandledType(SnaMessage.Type.LIFECYCLE);
            //The next line can be interesting for debug purposes
            //filter.addHandledType(SnaMessage.Type.UPDATE);
            if (agentsIdDependency.get(resourceUri) == null) {
                //If the agents to monitor this dependency does not exist, create one
                LOG.debug("Application '{}' creating agent to monitor resource {} availability", application.getName(), resourceUri);
                final String agentId = core.registerAgent(mediator, DependencyManager.this, filter);
                agentsIdDependency.put(resourceUri, agentId);
            } else {
                LOG.debug("Application '{}' agent to monitor resource {} availability already exist, skipping creation", application.getName(), resourceUri);
            }
        }
    }
}
