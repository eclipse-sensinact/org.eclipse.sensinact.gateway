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
package org.eclipse.sensinact.gateway.generic.local;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.core.method.Signature;
import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.annotation.TaskCommand;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;

import java.util.Arrays;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AnnotationExecutor implements Executable<Task, Object>, Nameable {
	
    private final String target;
    private final Task.CommandType commandType;
    private final TaskCommand.SynchronizationPolicy synchronization;
    private final String profile;
    private final Class<?>[] parameterTypes;

    private final boolean allProfiles;
    private boolean isVarArg;

    /**
     * Constructor
     */
    AnnotationExecutor(String target, Task.CommandType commandType, TaskCommand.SynchronizationPolicy synchronization, boolean isVarArg, Class<?>[] parameterTypes, String profile) {
        this.target = target;
        this.commandType = commandType;
        this.synchronization = synchronization;
        this.isVarArg = isVarArg;
        this.parameterTypes = parameterTypes == null ? new Class[0] : parameterTypes;

        this.profile = profile;

        if (profile != null) 
            this.allProfiles = profile.equals(ResourceConfig.ALL_PROFILES);
        else 
            this.allProfiles = true;
    }

    public boolean isSynchronous() {
        return TaskCommand.SynchronizationPolicy.SYNCHRONOUS.equals(this.synchronization);
    }

    public Task.CommandType getCommandType() {
        return this.commandType;
    }

    public int length() {
        return this.parameterTypes.length;
    }

    /**
     * Returns true if this BasisExecutor has been
     * defined for the profile passed as parameter;
     * returns false otherwise
     *
     * @param profile the profile for which to test if this
     *                BasisExecutor is registered
     * @return true if this BasisExecutor has been
     * defined for the profile passed as parameter;
     * returns false otherwise
     */
    public boolean isProfile(String profile) {
        if (this.allProfiles) 
            return true;
        return this.profile.equals(profile);
    }

    /**
     * Returns true if this BasisExecutor has been
     * defined for one of the profiles defined in the
     * array passed as parameter; returns false otherwise
     *
     * @param profile the array profiles for which to test if this
     *                BasisExecutor is registered
     * @return true if this BasisExecutor has been
     * defined for one of the profiles defined in
     * the array passed as parameter; false otherwise
     */
    public boolean isProfile(String[] profile) {
        if (profile == null) {
            return false;
        }
        int index = 0;
        int length = profile.length;

        for (; index < length && !this.isProfile(profile[index]); index++) ;

        return index < length;
    }
    
    public boolean isAllTargets() {
    	switch(getName()) {
	    	case "/":
	    	case "*":
	    	case "/*":
	    	case ResourceConfig.ALL_TARGETS:
	    		return true;
    	}
    	return false;
    }

    @Override
    public String getName() {
        return this.target;
    }
    
    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (object.getClass() == String.class) {
            return object.equals(this.target);
        }
        if (Signature.class.isAssignableFrom(object.getClass())) {
            if (((Signature) object).getName().equals(this.commandType.name())) {
                return this.equals(((Signature) object).getParameterTypes());
            }
        }
        if (object.getClass().isArray() && Class.class == object.getClass().getComponentType()) {
            if (this.isVarArg) {
                return true;
            }
            Class<?>[] parameterTypes = (Class<?>[]) object;
            int index = 0;
            int length = parameterTypes == null ? 0 : parameterTypes.length;
            if (this.parameterTypes.length != length) {
                return false;
            }
            for (; index < length && CastUtils.isAssignableFrom(this.parameterTypes[index], parameterTypes[index]); index++)
                ;

            return (index == length);
        }
        if (object.getClass() == Task.CommandType.class) {
            return (this.commandType.equals(object));
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n*************************************");
        builder.append("TARGET : ");
        builder.append(target);
        builder.append("\n");
        builder.append("COMMAND : ");
        builder.append(commandType.name());
        builder.append("\n");
        builder.append("IS SYNCHRONIZED : ");
        builder.append(synchronization.name());
        builder.append("\n");
        builder.append("PROFILE : ");
        builder.append(profile);
        builder.append("\n");
        builder.append("PARAMETERS : ");
        builder.append(parameterTypes == null ? "null" : Arrays.toString(parameterTypes));
        builder.append("\n");
        builder.append("HANDLE ALL PROFILES : ");
        builder.append(allProfiles);
        builder.append("\n");
        builder.append("IS VAR ARGS : ");
        builder.append(isVarArg);
        builder.append("\n");
        builder.append("*************************************\n");
        return builder.toString();
    }
}
