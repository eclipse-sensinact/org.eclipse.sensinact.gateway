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
package org.eclipse.sensinact.gateway.nthbnd.endpoint.test;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.AccessNode;
import org.eclipse.sensinact.gateway.core.security.AccessProfileOption;
import org.eclipse.sensinact.gateway.core.security.AccessTree;
import org.eclipse.sensinact.gateway.core.security.AccessTreeImpl;
import org.eclipse.sensinact.gateway.core.security.ImmutableAccessNode;
import org.eclipse.sensinact.gateway.core.security.ImmutableAccessTree;
import org.eclipse.sensinact.gateway.core.security.MutableAccessNode;
import org.eclipse.sensinact.gateway.core.security.MutableAccessTree;
import org.eclipse.sensinact.gateway.core.security.SecuredAccess;
import org.eclipse.sensinact.gateway.core.security.SecuredAccessException;

class MySecuredAccess implements SecuredAccess {
    private Mediator mediator;

    /**
     * @param mediator
     */
    public MySecuredAccess(Mediator mediator) {
        this.mediator = mediator;
    }

    /**
     * @inheritDoc
     * @see SecuredAccess#buildAccessNodesHierarchy(String, String, AccessTreeImpl)
     */
    @Override
    public void buildAccessNodesHierarchy(String identifier, String name, MutableAccessTree<? extends MutableAccessNode> accessTree) throws SecuredAccessException {
        accessTree.add(name);

        if (!"serviceProvider".equals(name)) {
            accessTree.add("serviceProvider");
        }
        accessTree.add("serviceProvider/testService");
        accessTree.add("serviceProvider/testService/location").withAccessProfile(AccessProfileOption.DEFAULT.getAccessProfile());
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.api.security.SecuredAccess#
     * getAccessTree(java.lang.String)
     */
    @Override
    public MutableAccessTree<? extends MutableAccessNode> getAccessTree(String identifier) throws SecuredAccessException {
        MutableAccessTree<? extends MutableAccessNode> accessTree = new AccessTreeImpl<>(mediator).withAccessProfile(AccessProfileOption.ALL_ANONYMOUS);
        return accessTree;
    }

    @Override
    public String getAgentPublicKey(String bundleIdentifier) throws SecuredAccessException {
        return null;//"xxxxxxxxxxxxxx000001";
    }

    @Override
    public AccessTree<? extends AccessNode> getUserAccessTree(String publicKey) throws SecuredAccessException {
        ImmutableAccessNode root = new ImmutableAccessNode(null, "/", false, null, AccessProfileOption.DEFAULT.getAccessProfile());
        ImmutableAccessTree accessTree = new ImmutableAccessTree(root);
        return accessTree;
    }

    @Override
    public String getApplicationPublicKey(String privateKey) throws SecuredAccessException {
        return null;
    }

    @Override
    public AccessTree<? extends AccessNode> getApplicationAccessTree(String publicKey) throws SecuredAccessException {
        ImmutableAccessNode root = new ImmutableAccessNode(null, "/", false, null, AccessProfileOption.DEFAULT.getAccessProfile());
        ImmutableAccessTree accessTree = new ImmutableAccessTree(root);
        return accessTree;
    }

    /**
     * @inheritDoc
     * @see SecuredAccess#createAuthorizationService()
     */
    @Override
    public void createAuthorizationService() {
        //nothing to implement here
        //AuthorizationService already registered in the
        //moked BundleContext
    }

    /**
     * @inheritDoc
     * @see SecuredAccess#close()
     */
    @Override
    public void close() {
        System.out.println("Closing the SecuredAccess");
    }
}