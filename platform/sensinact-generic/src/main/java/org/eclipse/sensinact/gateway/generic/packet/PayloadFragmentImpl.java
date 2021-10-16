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
package org.eclipse.sensinact.gateway.generic.packet;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.Name;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of a {@link PayloadFragment}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class PayloadFragmentImpl implements PayloadFragment {
    /**
     * the List of {@link PayloadServiceFragment}s of this
     * {@link PayloadFragment}
     */
    protected List<PayloadServiceFragment> payloadFragments;

    protected boolean isHelloMessage;
    protected boolean isGoodbyeMessage;
    protected String serviceProviderIdentifier;
    protected String profileId;

    private Mediator mediator;

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} used to interact with
     *                 the OSGi host environment
     */
    public PayloadFragmentImpl(Mediator mediator) {
        this.mediator = mediator;
        this.payloadFragments = new ArrayList<PayloadServiceFragment>();
    }

    /**
     * Constructor
     *
     * @param payloadFragments the initial set of held {@link PayloadServiceFragment}s of the
     *                         SubPacket to instantiate
     */
    public PayloadFragmentImpl(Mediator mediator, List<PayloadServiceFragment> payloadFragments) {
        this(mediator);
        if (payloadFragments != null) {
            Iterator<PayloadServiceFragment> iterator = payloadFragments.iterator();

            while (iterator.hasNext()) {
                this.addPayloadFragment(iterator.next());
            }
        }
    }

    /**
     * Adds the {@link PayloadServiceFragment} passed as parameter to the
     * list of ones of this SubPacket
     *
     * @param payloadFragment the {@link PayloadServiceFragment} to add
     */
    public void addPayloadFragment(PayloadServiceFragment payloadFragment) {
        if (payloadFragment != null) {
            this.payloadFragments.add(payloadFragment);
        }
    }

    @Override
    public Iterator<PayloadServiceFragment> iterator() {
        return this.payloadFragments.iterator();
    }

    @Override
    public String getProfileId() {
        return this.profileId;
    }

    /**
     * Defines the string identifier of the profile of the {@link
     * ServiceProvider} targeted by this SubPacket
     *
     * @param the targeted {@link ServiceProvider}'s profile
     */
    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    @Override
    public String getServiceProviderIdentifier() {
        return this.serviceProviderIdentifier;
    }

    /**
     * Defines the string identifier of a {@link ServiceProvider} targeted by
     * this SubPacket
     *
     * @param the targeted {@link ServiceProvider} by this SubPacket
     */
    public void setServiceProviderIdentifier(String serviceProviderIdentifier) {
        this.serviceProviderIdentifier = serviceProviderIdentifier;
    }

    @Override
    public boolean isHelloMessage() {
        return this.isHelloMessage;
    }

    /**
     * Defines whether this SubPacket is an "Hello" message, meaning that
     * the targeted {@link ServiceProvider} is connecting to the network
     * or not
     *
     * @param isHelloMessage <ul>
     *                       <li>true if this SubPacket is an "Hello" message</li>
     *                       <li>false otherwise</li>
     *                       </ul>
     */
    public void isHelloMessage(boolean isHelloMessage) {
        this.isHelloMessage = isHelloMessage;
    }

    /**
     * @inheritDoc
     * @see PayloadFragment#isGoodByeMessage()
     */
    @Override
    public boolean isGoodByeMessage() {
        return this.isGoodbyeMessage;
    }

    /**
     * Defines whether this SubPacket is a "Goodbye" message, meaning that
     * the targeted {@link ServiceProvider} is disconnecting from the network
     * or not
     *
     * @param isGoobyeMessage <ul>
     *                        <li>true if this SubPacket is an "Goodbye" message</li>
     *                        <li>false otherwise</li>
     *                        </ul>
     */
    public void isGoodbyeMessage(boolean isGoodbyeMessage) {
        this.isGoodbyeMessage = isGoodbyeMessage;
    }

    @Override
    public List<TaskIdValuePair> getTaskIdValuePairs() {
        List<TaskIdValuePair> list = new ArrayList<TaskIdValuePair>();

        int index = 0;
        int length = this.payloadFragments == null ? 0 : this.payloadFragments.size();

        for (; index < length; index++) {
            List<TaskIdValuePair> taskIdValuePairs = this.payloadFragments.get(index).AsTaskIdValuePair(this.getServiceProviderIdentifier());

            if (taskIdValuePairs != null && !taskIdValuePairs.isEmpty()) {
                list.addAll(taskIdValuePairs);
            }
        }
        return list;
    }

    @Override
    public boolean treated(String taskIdentifier) {
        boolean treated = false;
        int index = 0;
        int length = this.payloadFragments == null ? 0 : this.payloadFragments.size();

        for (; index < length; index++) {
            if ((treated = this.payloadFragments.get(index).treated(taskIdentifier))) {
                if (this.payloadFragments.get(index).size() == 0) {
                    this.payloadFragments.remove(index);
                }
                break;
            }
        }
        return treated;
    }

    @Override
    public int size() {
        return this.payloadFragments.size();
    }

    @Override
    public String getName() {
        return this.getServiceProviderIdentifier();
    }

    /**
     * Returns the index of the {@link PayloadServiceFragment} of
     * this PayloadFragment, whose name is passed as parameter  
     * 
     * @param name the name of the {@link PayloadServiceFragment}
     * 
     * @return the index of the {@link PayloadServiceFragment} with
     * the specified name
     */
    public int indexOf(Name<PayloadServiceFragment> name) {
        return this.payloadFragments.indexOf(name);
    }

    /**
     * Returns the {@link PayloadServiceFragment} of this {@link PayloadFragment}
     * at the index passed as parameter  
     * 
     * @param index the index of the {@link PayloadServiceFragment}
     * 
     * @return the {@link PayloadServiceFragment} with
     * the specified index
     */
    public PayloadServiceFragmentImpl get(int index) {
        return (PayloadServiceFragmentImpl) this.payloadFragments.get(index);
    }
}
