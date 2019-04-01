package org.eclipse.sensinact.gateway.core;

import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.core.api.Sensinact;
import org.eclipse.sensinact.gateway.core.api.SensinactCoreBaseIface;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;

import java.util.Iterator;
import java.util.Set;

@Component(
        immediate = false,
        servicefactory = false,

        configurationPid ="org.eclipse.sensinact.gateway.core.SensinactCoreBase",
        property = {
                "service.exported.interfaces=*",//org.eclipse.sensinact.gateway.core.api.SensinactCoreBaseIface
                "service.exported.configs=aries.fastbin",
        }
)
public class SensinactCoreBase implements SensinactCoreBaseIface {


    volatile private Sensinact sensinact;

    ConfigurationAdmin admin;
/*
    @Reference
    void setConfigurationAdmin(ConfigurationAdmin admin) {
        this.admin = admin;
    }
*/
    @Reference(cardinality = ReferenceCardinality.AT_LEAST_ONE,policy = ReferencePolicy.STATIC,policyOption = ReferencePolicyOption.RELUCTANT)
    public void setSensiNact(Sensinact sensinact) {
        this.sensinact = sensinact;
    }


    BundleContext bc;

    @Activate
    //public void act(BundleContext bc){
    public void act(ComponentContext cc){
        this.bc=cc.getBundleContext();
      /*
        cc.getProperties().put("custom",namespace());
        System.out.println("Activating SensinactCorebase:"+bc.toString()+" -- "+namespace());//+sensinact.namespace()

        try {
            admin.getConfiguration("org.eclipse.sensinact.gateway.core.SensinactCoreBase").getProperties().put("snanamespace2",sensinact.namespace());

        } catch (IOException e) {
            e.printStackTrace();
        }
        */

    }

    @Override
    public String namespace() {
/*
        try {
            Collection<ServiceReference<Sensinact>> se=bc.getServiceReferences(Sensinact.class,null);
            if(!se.iterator().hasNext()) return "thisname";
            ServiceReference sr= (ServiceReference) se.iterator().next();

            Sensinact s=(Sensinact)bc.getService(sr);

            return s.namespace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "none";
        */
        return sensinact.namespace();
    }

    @Override
    public String getAll(String identifier, String filter) {
        SensiNact.SensiNactAnonymousSession session=(SensiNact.SensiNactAnonymousSession)sensinact.getAnonymousSession();
        return sensinact.getAll(session.identifier,filter);
    }

    @Override
    public String getProviders(String identifier, String filter) {

        StringBuilder sb=new StringBuilder();

        //sb.append("\""+namespace()+":temps\"");

        SensiNact.SensiNactAnonymousSession session=(SensiNact.SensiNactAnonymousSession)sensinact.getAnonymousSession();

        //String localProv=sensinact.getProvidersLocal(identifier,filter);
        //if(localProv.contains(","))
        for(String prov:sensinact.getProvidersLocal(session.identifier,filter).split(",")){
            String providerNameRemote=prov.replace("\"","");
            sb.append(",\""+namespace()+":"+providerNameRemote+"\"");
        }

        System.out.println("******** "+sb.toString());


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
        String resultResponse=session.set(serviceProviderId,serviceId,resourceId,attributeId,createObjectArrayParamFromJSON(parameter)).getJSON();
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
