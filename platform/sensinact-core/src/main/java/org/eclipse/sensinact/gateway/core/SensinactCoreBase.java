package org.eclipse.sensinact.gateway.core;

import org.eclipse.sensinact.gateway.core.api.Sensinact;
import org.eclipse.sensinact.gateway.core.api.SensinactCoreBaseIface;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;

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
    public String get(String identifier, String serviceProviderId, String serviceId, String resourceId, String attributeId) {
        SensiNact.SensiNactAnonymousSession session=(SensiNact.SensiNactAnonymousSession)sensinact.getAnonymousSession();
        JSONObject value=session.get(serviceProviderId,serviceId,resourceId,attributeId).getResponse();
        return value.toString();
    }

}
