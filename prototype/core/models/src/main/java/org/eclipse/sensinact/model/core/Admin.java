/**
 */
package org.eclipse.sensinact.model.core;

/**
 * <!-- begin-user-doc --> A representation of the model object
 * '<em><b>Admin</b></em>'. <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 * <li>{@link org.eclipse.sensinact.model.core.Admin#getFriendlyName
 * <em>Friendly Name</em>}</li>
 * <li>{@link org.eclipse.sensinact.model.core.Admin#getLocation
 * <em>Location</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sensinact.model.core.SensiNactPackage#getAdmin()
 * @model
 * @generated
 */
public interface Admin extends Service {
    /**
     * Returns the value of the '<em><b>Friendly Name</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Friendly Name</em>' attribute.
     * @see #setFriendlyName(String)
     * @see org.eclipse.sensinact.model.core.SensiNactPackage#getAdmin_FriendlyName()
     * @model
     * @generated
     */
    String getFriendlyName();

    /**
     * Sets the value of the
     * '{@link org.eclipse.sensinact.model.core.Admin#getFriendlyName <em>Friendly
     * Name</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value the new value of the '<em>Friendly Name</em>' attribute.
     * @see #getFriendlyName()
     * @generated
     */
    void setFriendlyName(String value);

    /**
     * Returns the value of the '<em><b>Location</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Location</em>' attribute.
     * @see #setLocation(String)
     * @see org.eclipse.sensinact.model.core.SensiNactPackage#getAdmin_Location()
     * @model
     * @generated
     */
    String getLocation();

    /**
     * Sets the value of the
     * '{@link org.eclipse.sensinact.model.core.Admin#getLocation
     * <em>Location</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value the new value of the '<em>Location</em>' attribute.
     * @see #getLocation()
     * @generated
     */
    void setLocation(String value);

} // Admin
