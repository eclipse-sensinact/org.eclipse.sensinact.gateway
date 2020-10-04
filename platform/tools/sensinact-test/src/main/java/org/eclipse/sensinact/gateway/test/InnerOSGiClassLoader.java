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
package org.eclipse.sensinact.gateway.test;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;

public final class InnerOSGiClassLoader extends FilterOSGiClassLoader {
    public InnerOSGiClassLoader(ClassLoader parent, BundleContextProvider contextProvider, FilterOSGiClassLoader loader) throws IOException {
        super(parent, contextProvider, loader);
    }

    private Bundle findBundle(String bundleName) {
        if (bundleName != null) {
            Bundle[] bundles = super.contextProvider.getBundleContext().getBundles();

            int index = 0;
            int length = bundles == null ? 0 : bundles.length;
            for (; index < length; index++) {
                final Bundle tmp = bundles[index];
                if (bundleName.equals(tmp.getSymbolicName())) {
                    return AccessController.doPrivileged(new PrivilegedAction<Bundle>() {
                        public Bundle run() {
                            Bundle bundle = null;

                            if ((tmp.adapt(BundleRevision.class).getTypes() & BundleRevision.TYPE_FRAGMENT) != 0 && tmp.getState() == Bundle.RESOLVED) {
                                bundle = findBundle(tmp.getHeaders().get(Constants.FRAGMENT_HOST));
                            } else {
                                bundle = tmp;
                            }
                            return bundle;
                        }
                    });
                }
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.ClassLoader#loadClass(java.lang.String)
     */
    @Override
    public Class<?> loadClass(String classname, boolean resolve) throws ClassNotFoundException {
        //System.out.println(classname);
        Class<?> clazz = null;
        String bundleName = isAFilteredClass(classname);
        if (bundleName != null) {
            //avoid loop
            if (this.loadingClass != null) {
                this.loadingClass = null;
                throw new ClassNotFoundException(classname);
            }
            this.loadingClass = classname;
            final Bundle bundle = findBundle(bundleName);
            if (bundle != null) {
                try {
                    ClassLoader loader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                        public ClassLoader run() {
                            BundleWiring wiring = bundle.adapt(BundleWiring.class);
                            if (wiring != null) {
                                return wiring.getClassLoader();
                            }
                            return null;
                        }
                    });
                    if (loader != null) {
                        return loader.loadClass(classname);
                    }
                } finally {
                    this.loadingClass = null;
                }
            }
            return null;
        }
        if (classname.startsWith("[")) {
            clazz = Class.forName(classname);
        } else {
            clazz = super.loadClass(classname, resolve);
        }
        return clazz;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.ClassLoader#findResource(java.lang.String)
     */
    public URL findResource(final String name) {
        String bundleName = isAFilteredResource(name);
        if (bundleName != null) {
            //avoid loop
            if (this.loadingResource != null) {
                this.loadingResource = null;
                return null;
            }
            this.loadingResource = name;
            final Bundle bundle = findBundle(bundleName);
            if (bundle != null) {
                try {
                    URL url = AccessController.doPrivileged(new PrivilegedAction<URL>() {
                        public URL run() {
                            return bundle.getResource(name);
                        }
                    });
                    return url;

                } finally {
                    this.loadingResource = null;
                }
            }
            return null;
        }
        return super.findResource(name);
    }
}