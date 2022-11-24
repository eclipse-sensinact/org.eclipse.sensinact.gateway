/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Data In Motion - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.prototype.model.nexus.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

class ModelTransaction {

    enum ModelTransactionState {
        NONE, NEW
    }

    private List<EStructuralFeature> featurePath = new ArrayList<>();
    private ModelTransaction.ModelTransactionState serviceState = ModelTransactionState.NONE;
    private ModelTransaction.ModelTransactionState resourceState = ModelTransactionState.NONE;
    private EClass service;

    public void addFeature(EStructuralFeature feature) {
        featurePath.add(feature);
    }

    /**
     * Returns the featurePath.
     *
     * @return the featurePath
     */
    public List<EStructuralFeature> getFeaturePath() {
        return featurePath;
    }

    /**
     * Returns the service.
     *
     * @return the service
     */
    public EClass getService() {
        return service;
    }

    /**
     * Sets the service.
     *
     * @param service the service to set
     */
    public void setService(EClass service) {
        this.service = service;
    }

    /**
     * Returns the serviceState.
     *
     * @return the serviceState
     */
    public ModelTransaction.ModelTransactionState getServiceState() {
        return serviceState;
    }

    /**
     * Sets the serviceState.
     *
     * @param serviceState the serviceState to set
     */
    public void setServiceState(ModelTransaction.ModelTransactionState serviceState) {
        this.serviceState = serviceState;
    }

    /**
     * Returns the resourceState.
     *
     * @return the resourceState
     */
    public ModelTransaction.ModelTransactionState getResourceState() {
        return resourceState;
    }

    /**
     * Sets the resourceState.
     *
     * @param resourceState the resourceState to set
     */
    public void setResourceState(ModelTransaction.ModelTransactionState resourceState) {
        this.resourceState = resourceState;
    }
}
