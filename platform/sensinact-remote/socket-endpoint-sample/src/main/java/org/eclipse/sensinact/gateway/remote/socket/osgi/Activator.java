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
package org.eclipse.sensinact.gateway.remote.socket.osgi;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.remote.socket.sample.SocketEndpointManager;
import org.osgi.framework.BundleContext;

import java.util.*;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Activator extends AbstractActivator<Mediator> {
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
    private SocketEndpointManager manager;

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator#doStart()
     */
    @Override
    public void doStart() throws Exception {
        this.manager = new SocketEndpointManager(super.mediator);
        this.manager.updated(valueOf(super.mediator.getProperties()));
        super.mediator.addListener(this.manager);

    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator#doStop()
     */
    @Override
    public void doStop() throws Exception {
        super.mediator.deleteListener(this.manager);

        this.manager.stop();
        this.manager = null;
    }

    public static <K, V> Dictionary<K, V> valueOf(Map<K, V> dictionary) {
        if (dictionary == null) {
            return null;
        }
        Hashtable<K,V> re=new Hashtable<>();
        for(Map.Entry<K,V> entry:dictionary.entrySet()){
            re.put(entry.getKey(),entry.getValue());
        }
        return re;
    }


    public static <K, V> Map<K, V> valueOf(Dictionary<K, V> dictionary) {
      if (dictionary == null) {
        return null;
      }
      Map<K, V> map = new HashMap<K, V>(dictionary.size());
      Enumeration<K> keys = dictionary.keys();
      while (keys.hasMoreElements()) {
        K key = keys.nextElement();
        map.put(key, dictionary.get(key));
      }
      return map;
    }
    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator#doInstantiate(org.osgi.framework.BundleContext)
     */
    @Override
    public Mediator doInstantiate(BundleContext context) {
        return new Mediator(context);
    }
}
