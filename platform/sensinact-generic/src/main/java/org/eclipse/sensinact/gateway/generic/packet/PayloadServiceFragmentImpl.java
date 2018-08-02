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
package org.eclipse.sensinact.gateway.generic.packet;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.TaskManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract implementation of a {@link PayloadServiceFragment}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class PayloadServiceFragmentImpl implements PayloadServiceFragment {
    protected Mediator mediator;
    private String name;
    private String taskIdentifier;

    protected String serviceId;
    protected String resourceId;
    protected Task.CommandType command;

    protected List<PayloadResourceFragment> payloadAttributeFragments;

    /**
     * Constructor
     */
    public PayloadServiceFragmentImpl(Mediator mediator) {
        this.mediator = mediator;
        this.payloadAttributeFragments = new ArrayList<PayloadResourceFragment>();
    }

    /**
     * @inheritDoc
     * @see PayloadServiceFragment#
     * getServiceId()
     */
    @Override
    public String getServiceId() {
        return this.serviceId;
    }

    /**
     * Defines the {@link Service}'s string identifier
     * targeted by this {@link PayloadServiceFragment}
     *
     * @param serviceId the targeted {@link Service}'s identifier
     */
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * @inheritDoc
     * @see PayloadServiceFragment#getCommand()
     */
    @Override
    public Task.CommandType getCommand() {
        return this.command;
    }

    /**
     * Defines the CommandType of the Task this {@link PayloadServiceFragment}
     * responds to
     *
     * @param command the {@link Task.CommandType} of the Task this {@link PayloadServiceFragment}
     *                responds to
     */
    public void setCommand(Task.CommandType command) {
        this.command = command;
    }

    /**
     * @inheritDoc)
     * @see PayloadServiceFragment#
     * getResourceId()
     */
    @Override
    public String getResourceId() {
        return this.resourceId;
    }

    /**
     * Defines the {@link Resource}'s string identifier
     * targeted by this {@link PayloadServiceFragment}
     *
     * @param resourceIdId the targeted {@link Resource}'s identifier
     */
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    /**
     * Adds the {@link PayloadResourceFragment} passed as parameter
     * to the list of those of this {@link PayloadServiceFragment}
     *
     * @param payloadAttributeFragment the {@link PayloadResourceFragment} to add
     */
    public void addPayloadAttributeFragment(PayloadResourceFragment payloadAttributeFragment) {
        if (payloadAttributeFragment != null) {
            this.payloadAttributeFragments.add(payloadAttributeFragment);
        }
    }

    /**
     * @inheritDoc
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<PayloadResourceFragment> iterator() {
        return Collections.unmodifiableList(this.payloadAttributeFragments).iterator();
    }

    /**
     * Creates the string identifier of the {@link Task} to which this
     * {@link PayloadServiceFragment} is the response of
     *
     * @link serviceProviderIdentifier
     * the string identifier of the {@link ServiceProvider}
     * targeted by the parent SubPacket of this PayloadFragment
     */
    private void buildTaskIdentifier(String serviceProviderIdentifier) {
        if (this.command != null) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(serviceProviderIdentifier);
            buffer.append(TaskManager.IDENTIFIER_SEP_CHAR);
            buffer.append(this.getName());
            this.taskIdentifier = buffer.toString();
        }
    }

    /**
     * @inheritDoc
     * @see PayloadServiceFragment#AsTaskIdValuePair()
     */
    @Override
    public List<TaskIdValuePair> AsTaskIdValuePair(String serviceProviderIdentifier) {
        this.buildTaskIdentifier(serviceProviderIdentifier);
        List<TaskIdValuePair> taskIdValuePairs = new ArrayList<TaskIdValuePair>();

        int index = 0;
        int length = this.payloadAttributeFragments == null ? 0 : this.payloadAttributeFragments.size();

        for (; index < length; index++) {
            TaskIdValuePair taskIdValuePair = this.payloadAttributeFragments.get(index).AsTaskIdValuePair(this.taskIdentifier);

            if (taskIdValuePair != null) {
                taskIdValuePairs.add(taskIdValuePair);
                break;
            }
        }
        return taskIdValuePairs;
    }

    /**
     * This {@link PayloadServiceFragment} is informed that the {@link Task}
     * whose identifier is passed as parameter has been treated. The
     * associated {@link PayloadResourceFragment} is removed to avoid
     * a redundant treatment
     *
     * @param taskIdentifier the String identifier of the treated {@link Task}
     * @return <ul>
     * <li>
     * true if the associated {@link PayloadResourceFragment}
     * has been deleted
     * </li>
     * <li>
     * false if no associated {@link PayloadResourceFragment}
     * can be found
     * </li>
     * </ul>
     */
    public boolean treated(String taskIdentifier) {
        if (taskIdentifier == null) {
            return false;
        }
        int index = 0;
        int length = this.payloadAttributeFragments == null ? 0 : this.payloadAttributeFragments.size();

        for (; index < length; index++) {
            if (taskIdentifier.equals(this.payloadAttributeFragments.get(index).getTaskIdentifier())) {
                this.payloadAttributeFragments.remove(index);
                return true;
            }
        }
        return false;
    }

    /**
     * @inheritDoc
     * @see PayloadServiceFragment#size()
     */
    @Override
    public int size() {
        return this.payloadAttributeFragments.size();
    }

    /**
     * @inheritDoc
     * @see Nameable#getName()
     */
    @Override
    public String getName() {
        if (this.name == null) {
            StringBuilder builder = new StringBuilder();
            if (this.command != null) {
                builder.append(this.command.name());
                if (this.serviceId != null) {
                    builder.append(TaskManager.IDENTIFIER_SEP_CHAR);
                }
            }
            if (this.serviceId != null) {
                builder.append(this.serviceId);
                if (this.resourceId != null) {
                    builder.append(TaskManager.IDENTIFIER_SEP_CHAR);
                }
            }
            if (this.resourceId != null) {
                builder.append(this.resourceId);
            }
            this.name = builder.toString();
        }
        return this.name;
    }
}
