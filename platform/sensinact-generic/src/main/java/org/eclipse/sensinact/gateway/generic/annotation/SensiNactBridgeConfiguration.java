/*********************************************************************
* Copyright (c) 2020 Kentyou
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.eclipse.sensinact.gateway.core.ResourceImpl;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration.BuildPolicy;
import org.eclipse.sensinact.gateway.core.ServiceImpl;
import org.eclipse.sensinact.gateway.core.ServiceProviderImpl;
import org.eclipse.sensinact.gateway.generic.ExtResourceImpl;
import org.eclipse.sensinact.gateway.generic.ExtServiceImpl;
import org.eclipse.sensinact.gateway.generic.ExtServiceProviderImpl;
import org.eclipse.sensinact.gateway.generic.ProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.packet.Packet;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
/**
* Annotation describing the configuration of a sensiNact bridge 
*
* @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
*/
public @interface SensiNactBridgeConfiguration {
	
	String resourceDefinition() default "resources.xml"; 
	
	BuildPolicy[] serviceBuildPolicy() default {BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION};
	BuildPolicy[] resourceBuildPolicy() default {BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION};
	
	boolean startAtInitializationTime() default true;
	Class<? extends Packet> packetType() default Packet.class; 
	
	@SuppressWarnings("rawtypes")
	Class<? extends ProtocolStackEndpoint> endpointType() default LocalProtocolStackEndpoint.class;

	Class<? extends ServiceProviderImpl> serviceProviderType() default ExtServiceProviderImpl.class;
	Class<? extends ServiceImpl> serviceType() default ExtServiceImpl.class;
	Class<? extends ResourceImpl> resourceType() default ExtResourceImpl.class;
	
	ServiceProviderDefinition[] initialProviders() default {};
	String[] observed() default {}; 	
	
	boolean outputOnly() default false;
}
