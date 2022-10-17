/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.common.primitive;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.util.UriUtils;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract {@link SnaModelElementProxy} implementation
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class Elements<P extends Nameable> implements Nameable, PathElement {
    /**
     * the list of <code>&lt;P&gt;</code> typed objects owned
     * by this AbstractResourceModelElement
     */
    protected List<P> elements;
    /**
     * Poxied ResourceModelElement's uri
     */
    protected final String uri;

    /**
     * Poxied ResourceModelElement's name
     */
    protected final String name;

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} that will allow the
     *                 Elements to instantiate to interact with the OSGi
     *                 host environment
     * @param uri      the string uri path of the Elements
     *                 to instantiate
     */
    protected Elements(String uri) {
        this.uri = uri;
        this.name = UriUtils.getLeaf(uri);
        this.elements = new ArrayList<P>();
    }

    /**
     * @InheritedDoc
     * @see Nameable#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @InheritedDoc
     * @see PathElement#getPath()
     */
    @Override
    public String getPath() {
        return this.uri;
    }

    /**
     * Returns the length of this <code>&lt;P&gt;</code>
     * typed elements collection
     *
     * @return this <code>&lt;P&gt;</code> typed elements
     * collection length
     */
    public int length() {
        synchronized (this.elements) {
            return this.elements.size();
        }
    }

    /**
     * Returns the <code>&lt;P&gt;</code> typed owned
     * object whose name is passed as parameter
     *
     * @return the specified <code>&lt;P&gt;</code> typed
     * owned object
     */
    public P element(String name) {
        if (name == null) {
            return null;
        }
        synchronized (this.elements) {
        	return elements.stream()
        			.filter(p -> name.equals(p.getName()))
        			.findFirst()
        			.orElse(null);
        }
    }

    /**
     * Returns the <code>&lt;P&gt;</code> typed owned
     * object whose index is passed as parameter
     *
     * @param index the index of the element to return
     * @return the specified indexed <code>&lt;P&gt;</code>
     * typed owned object
     */
    public P element(int index) {
        if (index < this.elements.size()) {
            return null;
        }
        P element = null;

        synchronized (this.elements) {
            element = this.elements.get(index);
        }
        return element;
    }

    /**
     * Adds the <code>&lt;P&gt;</code> typed object passed as
     * parameter to the list of owned ones
     *
     * @param element the <code>&lt;P&gt;</code> typed object to add
     */
    public boolean addElement(P element) {
        if (element == null) {
            return false;
        }
        String name = element.getName();
        if(name == null) {
        	return false;
        }
        synchronized (this.elements) {
            if (this.elements.stream().noneMatch(p -> name.equals(p.getName()))) {
                return this.elements.add(element);
            }
        }
        return false;
    }

    /**
     * Removes <code>&lt;P&gt;</code> typed object with the
     * name passed as parameter from the list of owned ones
     *
     * @param element the name of the <code>&lt;P&gt;</code> typed object
     *                to remove     *
     * @return the removed <code>&lt;P&gt;</code> typed object
     */
    public P removeElement(String element) {
        if (element == null) {
            return null;
        }
        synchronized (this.elements) {
        	Iterator<P> iterator = this.elements.iterator();
        	while(iterator.hasNext()) {
        		P p = iterator.next();
        		if(element.equals(p.getName())) {
        			iterator.remove();
        			return p;
        		}
        	}
        }
        return null;
    }

    /**
     * Removes <code>&lt;P&gt;</code> typed object whose index
     * is passed as parameter from the list of owned ones
     *
     * @param index the index of the <code>&lt;P&gt;</code> typed
     *              object to remove
     * @return the removed <code>&lt;P&gt;</code> typed object
     */
    protected P removeElement(int index) {
        if (index < this.elements.size()) {
            return null;
        }
        synchronized (this.elements) {
            return this.elements.remove(index);
        }
    }

    /**
     * Searches the last {@link Elements} instance
     * through parsing the string uri passed as
     * parameter (not necessarily the last path element
     * of the uri)
     *
     * @param uri the string uri to parse
     * @return the last retrieved {@link Elements} instance
     * through parsing the specified uri
     */
    public <O> O search(String uri) {
        String[] uriElements = UriUtils.getUriElements(uri);
        int length = uriElements.length;
        O found = null;

        if (length == 0 || !this.getName().equals(uriElements[0])) {
            return found;
        }
        if (length == 1) {
            return (O) this;
        }
        String childUri = UriUtils.getChildUri(uri);
        int index = 0;

        synchronized (this.elements) {
            for (; index < this.elements.size(); index++) {
                P element = this.elements.get(index);
                if (!element.getName().equals(uriElements[1])) {
                    continue;
                }
                if (Elements.class.isAssignableFrom(element.getClass())) {
                    found = (O) ((Elements) element).search(childUri);
                    break;
                }
                found = (O) element;
                break;
            }
        }
        return found;
    }

    /**
     * Executes the {@link Executable} passed as parameter
     * with each <code>&lt;P&gt;</code> element of this
     * collection as parameter.
     *
     * @param executor the {@link Executable} to execute
     *                 with each <code>&lt;P&gt;</code> element of this
     *                 collection as parameter.
     * @throws Exception
     */
    public void forEach(Executable<P, Void> executor) throws Exception {
        synchronized (this.elements) {
            int index = 0;
            int length = this.elements.size();
            for (; index < length; index++) {
                executor.execute(this.elements.get(index));
            }
        }
    }

    /**
     * Returns the {@link Enumeration} of the
     * <code>&lt;P&gt;</code> typed owned objects
     * list
     *
     * @return the {@link Enumeration} of the <code>&lt;P&gt;</code>
     * typed owned objects list
     */
    public Enumeration<P> elements() {
        return new SnaModelElementEnumeration();
    }

    /**
     * {@link Enumeration} implementation for this
     * Elements <code>&lt;P&gt;</code> typed
     * owned objects list
     */
    private final class SnaModelElementEnumeration implements Enumeration<P> {
        int position = 0;
        Object[] elementArray = Elements.this.elements.toArray(new Object[0]);

        /**
         * @inheritDoc
         * @see java.util.Enumeration#hasMoreElements()
         */
        @Override
        public boolean hasMoreElements() {
            return position < elementArray.length;
        }

        /**
         * @inheritDoc
         * @see java.util.Enumeration#nextElement()
         */
        @Override
        public P nextElement() {
            if (!hasMoreElements()) {
                return null;
            }
            return (P) elementArray[position++];
        }
    }
}
