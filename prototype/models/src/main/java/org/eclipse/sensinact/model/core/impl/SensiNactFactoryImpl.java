/**
 */
package org.eclipse.sensinact.model.core.impl;

import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.eclipse.sensinact.model.core.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class SensiNactFactoryImpl extends EFactoryImpl implements SensiNactFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static SensiNactFactory init() {
		try {
			SensiNactFactory theSensiNactFactory = (SensiNactFactory)EPackage.Registry.INSTANCE.getEFactory(SensiNactPackage.eNS_URI);
			if (theSensiNactFactory != null) {
				return theSensiNactFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new SensiNactFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SensiNactFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case SensiNactPackage.PROVIDER: return createProvider();
			case SensiNactPackage.ADMIN: return createAdmin();
			case SensiNactPackage.SERVICE: return createService();
			case SensiNactPackage.METADATA: return createMetadata();
			case SensiNactPackage.FEATURE_METADATA: return (EObject)createFeatureMetadata();
			case SensiNactPackage.MODEL_METADATA: return createModelMetadata();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Provider createProvider() {
		ProviderImpl provider = new ProviderImpl();
		return provider;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Admin createAdmin() {
		AdminImpl admin = new AdminImpl();
		return admin;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Service createService() {
		ServiceImpl service = new ServiceImpl();
		return service;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Metadata createMetadata() {
		MetadataImpl metadata = new MetadataImpl();
		return metadata;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Map.Entry<EStructuralFeature, Metadata> createFeatureMetadata() {
		FeatureMetadataImpl featureMetadata = new FeatureMetadataImpl();
		return featureMetadata;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ModelMetadata createModelMetadata() {
		ModelMetadataImpl modelMetadata = new ModelMetadataImpl();
		return modelMetadata;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SensiNactPackage getSensiNactPackage() {
		return (SensiNactPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static SensiNactPackage getPackage() {
		return SensiNactPackage.eINSTANCE;
	}

} //SensiNactFactoryImpl
