package org.eclipse.sensinact.gateway.core;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.core.api.MQTTURLExtract;
import org.eclipse.sensinact.gateway.core.api.Sensinact;
import org.eclipse.sensinact.gateway.core.api.SensinactCoreBaseIface;
import org.eclipse.sensinact.gateway.core.message.*;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.util.api.MqttBroker;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.Set;

@Component(
        immediate = false,
        servicefactory = false,
        configurationPid ="org.eclipse.sensinact.gateway.core.SensinactCoreBase",
        property = {
                "service.exported.interfaces=*",
                "service.exported.configs=aries.fastbin",
        }
)
public class SensinactCoreBase implements SensinactCoreBaseIface {

    private static final Logger LOG= LoggerFactory.getLogger(SensinactCoreBase.class);

    volatile private Sensinact sensinact;
    volatile private ConfigurationAdmin ca;

    @Reference(cardinality = ReferenceCardinality.AT_LEAST_ONE,policy = ReferencePolicy.STATIC,policyOption = ReferencePolicyOption.RELUCTANT)
    public void setSensiNact(Sensinact sensinact) {
        this.sensinact = sensinact;
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public void setCa(ConfigurationAdmin ca) {
        this.ca = ca;
    }

    private BundleContext bc;

    private MqttBroker mb;

    @Activate
    public void act(ComponentContext cc){
        LOG.info("Activating Sensinact Remote object");
        this.bc=cc.getBundleContext();

        try {

            Dictionary<String,Object> properties=ca.getConfiguration("sensinact").getProperties();
            final String brokerAddr = properties.get("broker").toString();
            final String brokerTopicPrefix=properties.get("broker.topic.prefix").toString();
            MQTTURLExtract mqttURL=new MQTTURLExtract(brokerAddr);

            mb=new MqttBroker.Builder().host(mqttURL.getHost()).port(mqttURL.getPort()).protocol(MqttBroker.Protocol.valueOf(mqttURL.getProtocol().toUpperCase())).build();

            sensinact.registerAgent(new Mediator(this.bc), new AbstractMidAgentCallback(){
                @Override
                public void doHandle(SnaLifecycleMessageImpl message) throws MidCallbackException {
                    publicRawMessage(message);
                }

                @Override
                public void doHandle(SnaUpdateMessageImpl message) throws MidCallbackException {
                    publicRawMessage(message);
                }

                @Override
                public boolean propagate() {
                    return false;
                }

                @Override
                public void stop() {

                }

                private void publicRawMessage(SnaMessage event){
                    try {

                        String namespace=sensinact.namespace();
                        if(!new JSONObject(event.getJSON()).getString("uri").contains(":")){
                            mb.publish(String.format("%s%s",brokerTopicPrefix,namespace),event.getJSON().toString());
                            LOG.debug("Sending from namespace {} the message {}",namespace,event.getJSON().toString());
                        }else {
                            LOG.debug("Not propagating message to remote Sensinact instance {} ",namespace,event.getJSON().toString());
                        }

                    } catch (Exception e) {
                        LOG.error("Failed",e);
                    }
                }

            },null);

            mb.connect();

        } catch (IOException e) {
            LOG.error("Failed",e);
        } catch (Exception e) {
            LOG.error("Failed",e);
        }
    }

    @Deactivate
    public void deactivate(){
        try {
            mb.disconnect();
        } catch (Exception e) {
            //Doesnt matter the result of this disconnection
        }
    }


    @Override
    public String namespace() {
        return sensinact.namespace();
    }

    @Override
    public String getAll(String identifier, String filter) {
        SensiNact.SensiNactAnonymousSession session=(SensiNact.SensiNactAnonymousSession)sensinact.getAnonymousSession();
        return sensinact.getAll(session.identifier,filter,true);
    }

    @Override
    public String getProviders(String identifier, String filter) {

        StringBuilder sb=new StringBuilder();

        SensiNact.SensiNactAnonymousSession session=(SensiNact.SensiNactAnonymousSession)sensinact.getAnonymousSession();

        for(String prov:sensinact.getProvidersLocal(session.identifier,filter).split(",")){
            String providerNameRemote=prov.replace("\"","");
            sb.append(",\""+namespace()+":"+providerNameRemote+"\"");
        }

        return sb.toString();
    }

    @Override
    public String getServices(String identifier, String serviceProviderId) {
        SensiNact.SensiNactAnonymousSession session=(SensiNact.SensiNactAnonymousSession)sensinact.getAnonymousSession();
        String value=sensinact.getServices(session.identifier,serviceProviderId);
        return value;
    }

    @Override
    public String getService(String identifier, String serviceProviderId, String serviceId) {

        SensiNact.SensiNactAnonymousSession session=(SensiNact.SensiNactAnonymousSession)sensinact.getAnonymousSession();
        JSONObject value=sensinact.getService(session.identifier,serviceProviderId,serviceId);
        return value.toString();
    }

    @Override
    public String getResources(String identifier, String serviceProviderId, String serviceId) {
        SensiNact.SensiNactAnonymousSession session=(SensiNact.SensiNactAnonymousSession)sensinact.getAnonymousSession();
        String value=sensinact.getResources(session.identifier,serviceProviderId,serviceId);
        return value;
    }

    @Override
    public String getProvider(String identifier, String serviceProviderId) {
        SensiNact.SensiNactAnonymousSession session=(SensiNact.SensiNactAnonymousSession)sensinact.getAnonymousSession();
        JSONObject value=sensinact.getProvider(session.identifier,serviceProviderId);
        return value.toString();
    }

    @Override
    public String getResource(String identifier, String serviceProviderId, String serviceId, String resourceId) {
        SensiNact.SensiNactAnonymousSession session=(SensiNact.SensiNactAnonymousSession)sensinact.getAnonymousSession();

        String value=session.getResource(identifier,serviceProviderId,serviceId,resourceId).getJSON();

        return value.toString();
    }

    @Override
    public String get(String identifier, String serviceProviderId, String serviceId, String resourceId, String attributeId) {
        SensiNact.SensiNactAnonymousSession session=(SensiNact.SensiNactAnonymousSession)sensinact.getAnonymousSession();
        String value=session.get(serviceProviderId,serviceId,resourceId,attributeId).getJSON();
        return value;
    }

    @Override
    public String subscribe(String providerName, String serviceName, String resourceName, Recipient recipient, Set<Constraint> conditions, String policy) {
        SensiNact.SensiNactAnonymousSession session=(SensiNact.SensiNactAnonymousSession)sensinact.getAnonymousSession();
        //Resource resource = session.getResource(providerName,serviceName,resourceName);
        return null;
    }

    @Override
    public String set(String requestId, String serviceProviderId, String serviceId, String resourceId, String attributeId, String parameter) {
        SensiNact.SensiNactAnonymousSession session=(SensiNact.SensiNactAnonymousSession)sensinact.getAnonymousSession();
        String resultResponse=session.set(serviceProviderId,serviceId,resourceId,attributeId,parameter).getJSON();
        return resultResponse;
    }

    @Override
    public String act(String requestId, String serviceProviderId, String serviceId, String resourceId, String parameters) {
        SensiNact.SensiNactAnonymousSession session=(SensiNact.SensiNactAnonymousSession)sensinact.getAnonymousSession();
        String resultResponse=session.act(requestId,serviceProviderId,serviceId,resourceId,createObjectArrayParamFromJSON(parameters)).getJSON();
        return resultResponse;
    }

    private Object[] createObjectArrayParamFromJSON(String parameters){
        JSONArray parameterJSONArray=new JSONArray(parameters);
        String[] parametersObjectArray=new String[parameterJSONArray.length()];
        Iterator it=parameterJSONArray.iterator();
        for(int x=0;parameters!=null&&(x<parameters.length())&&it.hasNext();x++){
            parametersObjectArray[x]=it.next().toString();
        }
        return parametersObjectArray;
    }

}
