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
package org.eclipse.sensinact.gateway.sthbnd.http.kodi.osgi;

import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpMediator;
import org.osgi.framework.BundleContext;

import java.util.HashMap;
import java.util.Map;

public class KodiServiceMediator extends HttpMediator
{
    private Map<String, String> kodiIPMap;

    public KodiServiceMediator(BundleContext context)  
    {
        super(context);
        this.kodiIPMap = new HashMap<String, String>();
    }
    
    public void putKodiIP(String friendlyName, String ip) {
        kodiIPMap.put(friendlyName, ip);
    }

    public void removeKodiIP(String friendlyName) {
        kodiIPMap.remove(friendlyName);
    }

    public String getKodiIP(String friendlyName) {
        return kodiIPMap.get(friendlyName);
    }

    public String getKodiFriendlyName(String ip)
    {
        for (Map.Entry<String, String> map : kodiIPMap.entrySet()) {
            if(map.getValue().equals(ip)) {
                return map.getKey();
            }
        }

        return null;
    }
}