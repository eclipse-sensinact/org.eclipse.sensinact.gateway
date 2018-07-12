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
package org.eclipse.sensinact.gateway.core;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.Elements;
import org.eclipse.sensinact.gateway.common.primitive.ElementsProxy;
import org.eclipse.sensinact.gateway.common.primitive.Name;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.common.primitive.ProcessableData;
import org.eclipse.sensinact.gateway.core.message.SnaConstants;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessage;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessage.Lifecycle;
import org.eclipse.sensinact.gateway.core.message.SnaNotificationMessageImpl;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.security.AccessLevel;
import org.eclipse.sensinact.gateway.core.security.AccessLevelOption;
import org.eclipse.sensinact.gateway.core.security.AccessNode;
import org.eclipse.sensinact.gateway.core.security.AccessTree;
import org.eclipse.sensinact.gateway.core.security.ImmutableAccessTree;
import org.eclipse.sensinact.gateway.core.security.MethodAccessibility;
import org.eclipse.sensinact.gateway.core.security.MutableAccessTree;
import org.json.JSONObject;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract sensiNact resource model element (service provider, service
 * & resource) implementation
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class ModelElement<I extends ModelInstance<?>, M extends ModelElementProxy, P extends ProcessableData, E extends Nameable, R extends Nameable> extends Elements<E> implements SensiNactResourceModelElement<M> {
    abstract class ModelElementProxyWrapper extends ElementsProxyWrapper<M, R> {
        private final ImmutableAccessTree tree;

        /**
         * @param proxy
         */
        protected ModelElementProxyWrapper(M proxy, ImmutableAccessTree tree) {
            super(proxy);
            this.tree = tree;
        }

        /**
         * @inheritDoc
         * @see org.eclipse.sensinact.gateway.common.primitive.ElementsProxy#element(java.lang.String)
         */
        @Override
        public R element(String name) {
            E e = ModelElement.this.element(name);
            if (e == null) {
                return null;
            }
            try {
                R r = ModelElement.this.getElementProxy(tree, e);
                return r;
            } catch (ModelElementProxyBuildException e1) {
                e1.printStackTrace();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            return null;
        }

        /**
         * @inheritDoc
         * @see org.eclipse.sensinact.gateway.common.primitive.ElementsProxy#elements()
         */
        @Override
        public Enumeration<R> elements() {
            final Object[] es;

            synchronized (ModelElement.this.elements) {
                es = ModelElement.this.elements.toArray(new Object[0]);
            }

            return new Enumeration<R>() {
                int pos = 0;
                R r = next();

                private R next() {
                    while (pos < es.length) {
                        try {
                            R r = ModelElement.this.getElementProxy(tree, (E) es[pos++]);
                            if (r != null && (!Proxy.isProxyClass(r.getClass()) || ((ElementsProxyWrapper<?, ?>) Proxy.getInvocationHandler(r)).isAccessible())) {
                                return r;
                            }
                        } catch (ModelElementProxyBuildException e1) {
                            e1.printStackTrace();
                        }
                    }
                    return null;
                }

                @Override
                public boolean hasMoreElements() {
                    return r != null;
                }

                @Override
                public R nextElement() {
                    R current = r;
                    r = next();
                    return current;
                }
            };
        }

        /**
         * @return
         */
        protected List<R> list() {
            List<R> list = new ArrayList<R>();
            Enumeration<R> elements = elements();
            while (elements.hasMoreElements()) {
                list.add(elements.nextElement());
            }
            return Collections.unmodifiableList(list);
        }
    }

    ;

    /**
     * Returns the {@link Lifecycle} event associated
     * to the registration of this ModelElement
     *
     * @return the {@link Lifecycle} event for this
     * ModelElement registration
     */
    protected abstract Lifecycle getRegisteredEvent();

    /**
     * Returns the {@link Lifecycle} event associated
     * to the unregistration of this ModelElement
     *
     * @return the {@link Lifecycle} event for this
     * ModelElement unregistration
     */
    protected abstract Lifecycle getUnregisteredEvent();

    /**
     * Returns the interface type implemented by a proxy
     * instance of this AbstractModelElement
     *
     * @return the interface type of a proxy instance of
     * this AbstractModelElement
     */
    protected abstract Class<? extends ElementsProxy<R>> getProxyType();

    /**
     * Processes the {@link ProcessableData} passed as
     * parameter
     *
     * @param data the {@link ProcessableData} to process
     */
    public abstract void process(P data);

    /**
     * Creates and returns an  element of an {@link
     * ModelElementProxy} of this ModelElement for the
     * element of this collection  passed as parameter
     *
     * @param accessLevelOption the long unique identifier for which to create the
     *                          proxy instance of the specified element
     * @param element           the element for which to create the proxy counterpart
     * @return the proxy for the specified element and the specified
     * user
     * @throws ModelElementProxyBuildException
     */
    protected abstract R getElementProxy(AccessTree<?> tree, E element) throws ModelElementProxyBuildException;

    /**
     * TODO
     */
    protected abstract ModelElementProxyWrapper getWrapper(M proxy, ImmutableAccessTree tree);

    /**
     * this ModelElement's parent
     */
    protected final ModelElement<I, ?, ?, ?, ?> parent;

    /**
     * the {@link SensiNactResourceModel} to which this
     * extended {@link ModelElement} belongs to
     */
    protected I modelInstance;

    /**
     * the proxies of this AbstractResourceModelElement mapped to
     * the profile identifiers for which they have been created
     */
    protected Map<String, AccessLevelOption> sessions;
    /**
     * the proxies of this AbstractResourceModelElement mapped to
     * the {@link AccessLevel} for which they have been created
     */
    protected EnumMap<AccessLevelOption, M> proxies;

    /**
     * Started status of this AbstractModelElement
     */
    protected AtomicBoolean started;

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} that will allow the {@link
     *                 ModelElementOld} to instantiate to interact
     *                 with the OSGi host environment
     * @param uri      the string uri path of the {@link ModelElementProxy}
     *                 to instantiate
     */
    protected ModelElement(I modelInstance, ModelElement<I, ?, ?, ?, ?> parent, String uri) {
        super(uri);
        if (parent != null && parent.getModelInstance() != modelInstance) {
            throw new RuntimeException("Inconsistent hierarchy");
        }
        this.parent = parent;
        this.modelInstance = modelInstance;
        this.started = new AtomicBoolean(false);
        this.sessions = new HashMap<String, AccessLevelOption>();
        this.proxies = new EnumMap<AccessLevelOption, M>(AccessLevelOption.class);
    }

    /**
     * Returns the {@link SensiNactResourceModel} to which
     * this extended {@link ModelElement} belongs to
     *
     * @return the {@link SensiNactResourceModel} to which
     * this extended {@link ModelElement} belongs
     * to
     */
    public I getModelInstance() {
        return this.modelInstance;
    }

    /**
     * Creates and returns a proxy of this AbstractModelElement
     *
     * @param uid the long unique identifier of the user for who to
     *            create the appropriate proxy
     * @return a new proxy object based on this ModelElement
     * @throws ModelElementProxyBuildException
     */
    public <S extends ElementsProxy<R>> S getProxy(SessionKey key) throws ModelElementProxyBuildException {
        if (!this.started.get()) {
            throw new ModelElementProxyBuildException("this model element must be started first");
        }
        AccessTree<? extends AccessNode> accessTree = key.getAccessTree();
        if (accessTree == null || accessTree.getRoot() == null) {
            throw new ModelElementProxyBuildException("A valid access tree was expected");
        }
        return this.getProxy(accessTree);
    }

    /**
     * Creates and returns a proxy of this AbstractModelElement
     *
     * @param uid the long unique identifier of the user for who to
     *            create the appropriate proxy
     * @return a new proxy object based on this ModelElement
     * @throws ModelElementProxyBuildException
     */
    @SuppressWarnings("unchecked")
    public final <S extends ElementsProxy<R>> S getProxy(final AccessTree<?> tree) throws ModelElementProxyBuildException {
        if (!this.started.get()) {
            throw new ModelElementProxyBuildException(String.format("this model element '%s' must be started first", this.getName()));
        }
        AccessNode node = tree.getRoot().get(super.getPath());

        //AccessLevelOption will be the same for all methods, just check
        //what is the one associated with the DESCRIBE one
        AccessLevelOption accessLevelOption = node.getAccessLevelOption(AccessMethod.Type.valueOf(AccessMethod.DESCRIBE));

        if (accessLevelOption == null) {
            throw new ModelElementProxyBuildException("Access level option expected");
        }
        Class<S> proxied = (Class<S>) this.getProxyType();
        M proxy = this.proxies.get(accessLevelOption);
        if (proxy == null) {
            int index = -1;
            List<MethodAccessibility> methodAccessibilities = this.modelInstance.getAuthorizations(this, accessLevelOption);

            //if the describe method does not exists it means
            //that the user is not allowed to access this ModelElement
            if ((index = methodAccessibilities.indexOf(new Name<MethodAccessibility>(AccessMethod.DESCRIBE))) == -1 || !methodAccessibilities.get(index).isAccessible()) {
                return (S) Proxy.newProxyInstance(ModelElement.class.getClassLoader(), new Class<?>[]{proxied}, new UnaccessibleModelElementProxyWrapper(new UnaccessibleModelElementProxy(this.modelInstance.mediator(), proxied, this.getPath())));

            } else {
                proxy = this.getProxy(methodAccessibilities);
            }
        }
        if (proxy == null) {
            return null;
        }
        this.proxies.put(accessLevelOption, proxy);
        ImmutableAccessTree accessTree = null;
        if (tree.isMutable()) {
            accessTree = ((MutableAccessTree<?>) tree).immutable();

        } else {
            accessTree = (ImmutableAccessTree) tree;
        }
        ModelElementProxyWrapper wrapper = getWrapper(proxy, accessTree);

        //creates the java proxy based on the created ModelElementProxy and returns it
        return (S) Proxy.newProxyInstance(ModelElement.class.getClassLoader(), new Class<?>[]{proxied}, wrapper);
    }

    /**
     * Passes on the invocation of an {@link AccessMethod}
     * whose type is passed as parameter, for the specified
     * resource and parameterized with the arguments array
     *
     * @param type       the type of the invoked method
     * @param uri        the String uri of the ModelElement
     *                   targeted by the call
     * @param parameters the objects array parameterizing the call
     * @return the <code>&lt;TASK&gt;</code> typed result
     * object of the execution chain
     * @throws Exception
     */
    protected <TASK> TASK passOn(String type, String uri, Object[] parameters) throws Exception {
        if (this.parent != null) {
            return this.parent.<TASK>passOn(type, uri, parameters);
        }
        return null;
    }

    /**
     * @InheritedDoc
     * @see Elements#
     * addElement(Nameable)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean addElement(final E element) {
        if (!super.addElement(element)) {
            return false;
        }
        if (this.started.get() && ModelElement.class.isAssignableFrom(element.getClass())) {
            ((ModelElement<I, ?, ?, ?, ?>) element).start();
        }
        return true;
    }

    /**
     * @inheritDoc
     * @see Elements#
     * removeElement(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public E removeElement(String name) {
        E element = null;
        if ((element = super.removeElement(name)) != null) {
            if (this.started.get() && ModelElement.class.isAssignableFrom(element.getClass())) {
                ((ModelElement<I, ?, ?, ?, ?>) element).stop();
            }
            return element;
        }
        return null;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.ModelElementOld#
     * registered(org.eclipse.sensinact.gateway.core.model.message.SnaMessageHandler)
     */
    protected void start() {
        try {
            if (!this.modelInstance.isRegistered()) {
                this.modelInstance.mediator().error("%s not registered", this.modelInstance.getName());
                return;
            }
            if (this.started.get()) {
                this.modelInstance.mediator().debug("%s already started", this.getName());
                return;
            }
            this.started.set(true);
            String path = super.getPath();
            Lifecycle event = this.getRegisteredEvent();

            SnaLifecycleMessage notification = SnaNotificationMessageImpl.Builder.<SnaLifecycleMessage>notification(this.modelInstance.mediator(), event, path);

            JSONObject notificationObject = new JSONObject();
            notificationObject.put(SnaConstants.ADDED_OR_REMOVED, event.name());
            notification.setNotification(notificationObject);

            this.modelInstance.postMessage(notification);
            forEach(new Executable<E, Void>() {
                @SuppressWarnings("unchecked")
                @Override
                public Void execute(E element) throws Exception {
                    if (ModelElement.class.isAssignableFrom(element.getClass())) {
                        ((ModelElement<I, ?, ?, ?, ?>) element).start();
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            this.getModelInstance().mediator().error(e);
        }
    }

    /**
     * Stops this extended {@link ModelElementOld}
     */
    protected void stop() {
        if (!this.started.get()) {
            this.modelInstance.mediator().debug("%s not started", this.getName());
            return;
        }
        this.started.set(false);
        this.proxies.clear();
        try {
            forEach(new Executable<E, Void>() {
                @SuppressWarnings("unchecked")
                @Override
                public Void execute(E element) throws Exception {
                    if (ModelElement.class.isAssignableFrom(element.getClass())) {
                        ((ModelElement<I, ?, ?, ?, ?>) element).stop();
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            this.getModelInstance().mediator().error(e);
        }
        Lifecycle event = this.getUnregisteredEvent();
        String path = super.getPath();

        SnaLifecycleMessage notification = SnaNotificationMessageImpl.Builder.<SnaLifecycleMessage>notification(this.modelInstance.mediator(), event, path);

        JSONObject notificationObject = new JSONObject();
        notificationObject.put(SnaConstants.ADDED_OR_REMOVED, event.name());
        notification.setNotification(notificationObject);

        this.modelInstance.postMessage(notification);
    }
}
