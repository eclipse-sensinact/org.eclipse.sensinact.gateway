/**
 */
package org.eclipse.sensinact.model.core;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc --> A representation of the model object
 * '<em><b>Provider</b></em>'. <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 * <li>{@link org.eclipse.sensinact.model.core.Provider#getId <em>Id</em>}</li>
 * <li>{@link org.eclipse.sensinact.model.core.Provider#getAdmin
 * <em>Admin</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sensinact.model.core.SensiNactPackage#getProvider()
 * @model
 * @generated
 */
public interface Provider extends EObject {
    /**
     * Returns the value of the '<em><b>Id</b></em>' attribute. <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Id</em>' attribute.
     * @see #setId(String)
     * @see org.eclipse.sensinact.model.core.SensiNactPackage#getProvider_Id()
     * @model id="true" required="true"
     * @generated
     */
    String getId();

    /**
     * Sets the value of the '{@link org.eclipse.sensinact.model.core.Provider#getId
     * <em>Id</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value the new value of the '<em>Id</em>' attribute.
     * @see #getId()
     * @generated
     */
    void setId(String value);

    /**
     * Returns the value of the '<em><b>Admin</b></em>' containment reference. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Admin</em>' containment reference.
     * @see #setAdmin(Admin)
     * @see org.eclipse.sensinact.model.core.SensiNactPackage#getProvider_Admin()
     * @model containment="true"
     * @generated
     */
    Admin getAdmin();

    /**
     * Sets the value of the
     * '{@link org.eclipse.sensinact.model.core.Provider#getAdmin <em>Admin</em>}'
     * containment reference. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value the new value of the '<em>Admin</em>' containment reference.
     * @see #getAdmin()
     * @generated
     */
    void setAdmin(Admin value);

} // Provider
