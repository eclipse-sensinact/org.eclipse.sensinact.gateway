/**
 */
package org.eclipse.core.model.sensinact;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Location</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.core.model.sensinact.Location#getLat <em>Lat</em>}</li>
 *   <li>{@link org.eclipse.core.model.sensinact.Location#getLng <em>Lng</em>}</li>
 * </ul>
 *
 * @see org.eclipse.core.model.sensinact.SensiNactPackage#getLocation()
 * @model
 * @generated
 */
public interface Location extends EObject {
	/**
	 * Returns the value of the '<em><b>Lat</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Lat</em>' attribute.
	 * @see #setLat(double)
	 * @see org.eclipse.core.model.sensinact.SensiNactPackage#getLocation_Lat()
	 * @model required="true"
	 * @generated
	 */
	double getLat();

	/**
	 * Sets the value of the '{@link org.eclipse.core.model.sensinact.Location#getLat <em>Lat</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Lat</em>' attribute.
	 * @see #getLat()
	 * @generated
	 */
	void setLat(double value);

	/**
	 * Returns the value of the '<em><b>Lng</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Lng</em>' attribute.
	 * @see #setLng(double)
	 * @see org.eclipse.core.model.sensinact.SensiNactPackage#getLocation_Lng()
	 * @model required="true"
	 * @generated
	 */
	double getLng();

	/**
	 * Sets the value of the '{@link org.eclipse.core.model.sensinact.Location#getLng <em>Lng</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Lng</em>' attribute.
	 * @see #getLng()
	 * @generated
	 */
	void setLng(double value);

} // Location
