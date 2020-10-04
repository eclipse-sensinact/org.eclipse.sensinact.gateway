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
package org.eclipse.sensinact.gateway.sthbnd.http.free.internal;

import org.eclipse.sensinact.gateway.generic.packet.annotation.AttributeID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.Data;
import org.eclipse.sensinact.gateway.generic.packet.annotation.GoodbyeMessage;
import org.eclipse.sensinact.gateway.generic.packet.annotation.HelloMessage;
import org.eclipse.sensinact.gateway.generic.packet.annotation.Iteration;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ResourceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceProviderID;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class FreeSmsPacket extends HttpPacket {
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
    private String name;
    private String user;
    private String pass;
    private boolean appearance;
    private int position = 0;

    /**
     *
     */
    public FreeSmsPacket(String name, String user, String pass, boolean appearance) {
        super(new byte[0]);
        this.setName(name);
        this.setAppearance(appearance);
        this.setUser(user);
        this.setPass(pass);
    }

    /**
     *
     */
    @Iteration
    public boolean iteration() {
        return (++position) >= 2;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the name
     */
    @ServiceProviderID
    public String getName() {
        return name;
    }

    @ServiceID
    public String getServiceID() {
        if (!isAppearance()) {
            return null;
        }
        return "SMS";
    }

    @ResourceID
    public String getResourceID() {
        if (!isAppearance()) {
            return null;
        }
        return "send";
    }

    @AttributeID
    public String getAttributeID() {
        if (!isAppearance()) {
            return null;
        }
        switch (position) {
            case 0:
                return "user";
            case 1:
                return "pass";
            default:
                return null;
        }
    }

    @Data
    public Object getData() {
        if (!isAppearance()) {
            return null;
        }
        switch (position) {
            case 0:
                return getUser();
            case 1:
                return getPass();
            default:
                return null;
        }
    }

    @HelloMessage
    public boolean isHelloMessage() {
        return this.isAppearance();
    }

    @GoodbyeMessage
    public boolean isGoodbyeMessage() {
        return !this.isAppearance();
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the pass
     */
    public String getPass() {
        return pass;
    }

    /**
     * @param pass the pass to set
     */
    public void setPass(String pass) {
        this.pass = pass;
    }

    /**
     * @return the appearance
     */
    public boolean isAppearance() {
        return appearance;
    }

    /**
     * @param appearance the appearance to set
     */
    public void setAppearance(boolean appearance) {
        this.appearance = appearance;
    }
}
