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
/**
 *
 */
package org.eclipse.sensinact.gateway.generic.annotation;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Proxy to an OSGi service
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class InjectedService implements InvocationHandler, ServiceListener {
    private final Class<?> type;
    private final String filter;
    private final Mediator mediator;

    private ServiceReference reference;

    /**
     * Constructor
     *
     * @param mediator
     * @param type
     * @param filter
     * @throws InvalidSyntaxException
     * @
     */
    InjectedService(Mediator mediator, Class<?> type, String filter) throws InvalidSyntaxException {
        this.mediator = mediator;

        this.type = type;
        this.filter = filter;

        String objectClassFilter = new StringBuilder().append("(").append(Constants.OBJECTCLASS).append("=").append(type.getCanonicalName()).append(")").toString();

        String propertiesFilter = filter == null ? null : new StringBuilder().append(filter.startsWith("(") ? "" : "(").append(filter).append(filter.endsWith(")") ? "" : ")").toString();

        this.mediator.getContext().addServiceListener(this, new StringBuilder().append(propertiesFilter != null ? "(&" : "").append(objectClassFilter).append(propertiesFilter != null ? propertiesFilter : "").append(propertiesFilter != null ? ")" : "").toString());

        searchReference();
    }

    /**
     * @inheritDoc
     * @see java.lang.reflect.InvocationHandler#
     * invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] parameters) throws Throwable {
        Object result = null;
        Object service = null;

        if (this.reference != null && (service = this.mediator.getContext().getService(this.reference)) != null) {
            try {
                result = method.invoke(service, parameters);

            } finally {
                this.mediator.getContext().ungetService(this.reference);
            }
        }
        if (result == null && method.getReturnType().isPrimitive() && method.getReturnType() != void.class) {
            throw new NullPointerException();
        }
        return result;
    }

    /**
     * @inheritDoc
     * @see org.osgi.framework.ServiceListener#
     * serviceChanged(org.osgi.framework.ServiceEvent)
     */
    @Override
    public void serviceChanged(ServiceEvent event) {
        switch (event.getType()) {
            case ServiceEvent.REGISTERED:
                if (this.reference == null) {
                    this.reference = event.getServiceReference();
                }
                break;
            case ServiceEvent.UNREGISTERING:
                if (event.getServiceReference().equals(this.reference)) {
                    this.reference = null;
                    searchReference();
                }
                break;
        }
    }

    /**
     * Searches for the {@link ServiceReference} registered in
     * the OSGi environment, of a service whose type and properties
     * comply those of this InjectedService
     */
    private void searchReference() {
        ServiceReference[] references = null;
        try {
            references = this.mediator.getContext().getServiceReferences(this.type.getCanonicalName(), this.filter);

        } catch (InvalidSyntaxException e) {
            if (this.mediator.isDebugLoggable()) {
                this.mediator.error(e, e.getMessage());
            }
        }
        int index = 0;
        int length = references == null ? 0 : references.length;
        for (; index < length; index++) {
            if (this.mediator.getContext().getService(references[index]) != null) {
                break;
            }
        }
        if (index < length) {
            this.mediator.getContext().ungetService(references[index]);
            this.reference = references[index];
        }
    }

    protected boolean exists() {
        return this.reference != null;
    }

    protected void delete() {
        this.mediator.getContext().removeServiceListener(this);
        this.reference = null;
    }
}
