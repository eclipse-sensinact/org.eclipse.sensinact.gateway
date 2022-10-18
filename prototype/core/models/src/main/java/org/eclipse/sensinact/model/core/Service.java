/**
 */
package org.eclipse.sensinact.model.core;

import org.eclipse.emf.common.util.EMap;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * <!-- begin-user-doc --> A representation of the model object
 * '<em><b>Service</b></em>'. <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 * <li>{@link org.eclipse.sensinact.model.core.Service#getMetadata
 * <em>Metadata</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sensinact.model.core.SensiNactPackage#getService()
 * @model
 * @generated
 */
public interface Service extends EObject {
    /**
     * Returns the value of the '<em><b>Metadata</b></em>' map. The key is of type
     * {@link org.eclipse.emf.ecore.EStructuralFeature}, and the value is of type
     * {@link org.eclipse.sensinact.model.core.Metadata}, <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Metadata</em>' map.
     * @see org.eclipse.sensinact.model.core.SensiNactPackage#getService_Metadata()
     * @model mapType="org.eclipse.sensinact.model.core.FeatureMetadata&lt;org.eclipse.emf.ecore.EStructuralFeature,
     *        org.eclipse.sensinact.model.core.Metadata&gt;"
     * @generated
     */
    EMap<EStructuralFeature, Metadata> getMetadata();

} // Service
