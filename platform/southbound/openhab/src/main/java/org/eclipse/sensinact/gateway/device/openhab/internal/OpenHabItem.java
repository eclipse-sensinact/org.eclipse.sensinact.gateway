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
package org.eclipse.sensinact.gateway.device.openhab.internal;

import org.apache.felix.ipojo.ComponentInstance;

/**
 * @Author Jander Nascimento<Jander.BotelhodoNascimento@cea.fr>
 */
public class OpenHabItem {

    String type;
    String name;
    String link;
    String state;
    ComponentInstance instance;

    public OpenHabItem(String type,String name,String link,String state){
        this.type=type;
        this.name=name;
        this.link=link;
        this.state=state;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object obj) {

        if(!(obj instanceof OpenHabItem )) return false;

        return ((OpenHabItem) obj).getName().equals(this.getName());
    }

    public void setInstance(ComponentInstance instance) {
        this.instance = instance;
    }

    public ComponentInstance getInstance() {
        return instance;
    }

    @Override
    public int hashCode() {
        int result;
        result = name.hashCode();
        return result;
    }
}
