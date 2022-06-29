/*
 */
package org.eclipse.core.model.sensinact.util;

import org.eclipse.core.model.sensinact.SensiNactPackage;

import org.eclipse.emf.common.util.URI;

import org.eclipse.emf.ecore.resource.Resource;

import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;

import org.gecko.emf.osgi.annotation.provide.ProvideEMFResourceConfigurator;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * <!-- begin-user-doc -->
 * The <b>Resource Factory</b> associated with the package.
 * <!-- end-user-doc -->
 * @see org.eclipse.core.model.sensinact.util.SensiNactResourceImpl
 * @generated
 */
 @Component( name = SensiNactPackage.eNAME + "Factory", service = Resource.Factory.class, scope = ServiceScope.SINGLETON)
 @ProvideEMFResourceConfigurator( name = SensiNactPackage.eNAME,
	contentType = { "" }, 
	fileExtension = {
	"sensinact"
 	},  
	version = "1.0.0"
)
public class SensiNactResourceFactoryImpl extends ResourceFactoryImpl {
	/**
	 * Creates an instance of the resource factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SensiNactResourceFactoryImpl() {
		super();
	}

	/**
	 * Creates an instance of the resource.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Resource createResource(URI uri) {
		Resource result = new SensiNactResourceImpl(uri);
		return result;
	}

} //SensiNactResourceFactoryImpl
