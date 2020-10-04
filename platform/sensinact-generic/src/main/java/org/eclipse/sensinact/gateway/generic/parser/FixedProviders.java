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
package org.eclipse.sensinact.gateway.generic.parser;

import org.eclipse.sensinact.gateway.common.primitive.Nameable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The fixed service providers set of an SnaManager
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class FixedProviders implements Iterable<FixedProviders.FixedProvider> {
    final List<FixedProvider> providers;

    /**
     * Constructor
     *
     * @param providers the list of xml  definitions of service providers
     */
    public FixedProviders(List<DeviceDefinition> providers) {
        List<FixedProvider> preProviders = new ArrayList<FixedProvider>();

        if (providers != null) {
            Iterator<DeviceDefinition> iterator = providers.iterator();
            while (iterator.hasNext()) {
                DeviceDefinition deviceDefinition = iterator.next();
                preProviders.add(new FixedProvider(deviceDefinition.getServiceProviderProfile(), deviceDefinition.getServiceProviderIdentifier(), deviceDefinition.getServices()));
            }
        }
        this.providers = Collections.unmodifiableList(preProviders);
    }

    /**
     * @inheritDoc
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<FixedProvider> iterator() {
        return this.providers.iterator();
    }

    /**
     * @return
     */
    public Map<String, List<String>> getFixedMap() {
        Map<String, List<String>> fixed = new HashMap<String, List<String>>();

        Iterator<FixedProvider> iterator = this.iterator();
        while (iterator.hasNext()) {
            FixedProvider provider = iterator.next();
            List<String> services = new ArrayList<String>();
            fixed.put(provider.getName(), services);

            Iterator<String> serviceIterator = provider.iterator();
            while (serviceIterator.hasNext()) {
                services.add(serviceIterator.next());
            }
        }
        return fixed;
    }

    /**
     * @return
     */
    public Map<String, String> getProviderMap() {
        Map<String, String> fixedProviders = new HashMap<String, String>();

        Iterator<FixedProvider> iterator = this.iterator();
        while (iterator.hasNext()) {
            FixedProvider provider = iterator.next();
            fixedProviders.put(provider.getName(), provider.getProfile());
        }
        return fixedProviders;
    }

    /**
     * A fixed service provider definition
     */
    public final class FixedProvider implements Iterable<String>, Nameable {
        private final String[] services;
        private final String provider;
        private final String profile;

        FixedProvider(String profile, String provider, String[] services) {
            this.provider = provider;
            this.services = services;
            this.profile = profile;
        }

        /**
         * @inheritDoc
         * @see Nameable#getName()
         */
        public String getName() {
            return this.provider;
        }

        /**
         * Returns the list of the names of the fixed
         * services provided by the fixed service provider
         *
         * @return this fixed service provider's service names
         */
        public String[] getServices() {
            if (this.services == null) {
                return new String[0];
            }
            String[] services = new String[this.services.length];
            System.arraycopy(this.services, 0, services, 0, this.services.length);
            return services;
        }

        /**
         * @inheritDoc
         * @see java.lang.Iterable#iterator()
         */
        @Override
        public Iterator<String> iterator() {
            return new Iterator<String>() {
                private int pos = 0;
                private final int length = FixedProvider.this.services == null ? 0 : FixedProvider.this.services.length;

                @Override
                public boolean hasNext() {
                    return pos < length;
                }

                @Override
                public String next() {
                    return FixedProvider.this.services[pos++];
                }

                @Override
                public void remove() {
                }
            };
        }

        /**
         * @return
         */
        public String getProfile() {
            return this.profile;
        }
    }
}
