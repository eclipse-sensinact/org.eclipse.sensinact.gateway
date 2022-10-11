/**
 */
package org.eclipse.sensinact.model.core;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Model Metadata</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.model.core.ModelMetadata#getVersion <em>Version</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sensinact.model.core.SensiNactPackage#getModelMetadata()
 * @model
 * @generated
 */
public interface ModelMetadata extends Metadata {
	/**
	 * Returns the value of the '<em><b>Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Version</em>' attribute.
	 * @see #setVersion(int)
	 * @see org.eclipse.sensinact.model.core.SensiNactPackage#getModelMetadata_Version()
	 * @model
	 * @generated
	 */
	int getVersion();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.model.core.ModelMetadata#getVersion <em>Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Version</em>' attribute.
	 * @see #getVersion()
	 * @generated
	 */
	void setVersion(int value);

} // ModelMetadata
