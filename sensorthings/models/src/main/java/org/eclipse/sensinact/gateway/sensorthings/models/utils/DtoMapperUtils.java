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
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.sensorthings.models.utils;

import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.TimedValue;

public class DtoMapperUtils {

	public static final String ADMIN = "admin";
	public static final String DESCRIPTION = "description";
	public static final String FRIENDLY_NAME = "friendlyName";
	public static final String LOCATION = "location";
	public static final String DEFAULT_ENCODING_TYPE = "text/plain";
	public static final String ENCODING_TYPE_VND_GEO_JSON = "application/vnd.geo+json";
	public static final String VERSION = "v1.1";

	public static final String NO_DESCRIPTION = "No description";
	public static final String NO_DEFINITION = "No definition";

	public static Optional<? extends ResourceSnapshot> getProviderAdminField(ProviderSnapshot provider, String resource) {
		ServiceSnapshot adminSvc = provider.getServices().stream().filter(s -> ADMIN.equals(s.getName())).findFirst().get();
		return adminSvc.getResources().stream().filter(r -> resource.equals(r.getName())).findFirst();
	}

	public static Optional<Object> getProviderAdminFieldValue(ProviderSnapshot provider, String resource) {
		Optional<? extends ResourceSnapshot> rc = getProviderAdminField(provider, resource);
		if (rc.isPresent()) {
			TimedValue<?> value = rc.get().getValue();
			if (value != null) {
				return Optional.ofNullable(value.getValue());
			}
		}
		return Optional.empty();
	}

	public static String toString(Object o) {
		return o == null ? null : String.valueOf(o);
	}

	public static String toString(Optional<?> o) {
		return o == null || o.isEmpty() ? null : String.valueOf(o.get());
	}

}
