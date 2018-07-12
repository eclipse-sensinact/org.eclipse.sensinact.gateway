package org.eclipse.sensinact.gateway.core.test;

import org.eclipse.sensinact.gateway.core.Filtering;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class XFilter implements Filtering {
    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//
    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//
    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

    /**
     *
     */
    public XFilter() {
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.Filtering#handle(java.lang.String)
     */
    @Override
    public boolean handle(String type) {
        return "xfilter".equals(type);
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.Filtering#apply(java.lang.String, java.lang.Object)
     */
    @Override
    public String apply(String definition, Object result) {
        String str = String.valueOf(result);
        char flt = definition.charAt(0);
        return str.replace(flt, 'X');
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.Filtering#getLDAPComponent()
     */
    @Override
    public String getLDAPComponent(String definition) {
        return null;
    }
}
