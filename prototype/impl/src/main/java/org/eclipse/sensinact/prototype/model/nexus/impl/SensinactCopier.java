/**
 * Copyright (c) 2022 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.sensinact.prototype.model.nexus.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil.Copier;

/**
 * As we modify the EClasses on the fly, we need to prevent the copier to call {@link EObject#sIsSet} or {@link EObject#sSet} for features that are new, as this would cause an {@link IndexOutOfBoundsException} 
 * @author Juergen Albert
 * @since 11 Oct 2022
 */
public class SensinactCopier extends Copier {

	/** serialVersionUID */
	private static final long serialVersionUID = 3455941336515352303L;

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.emf.ecore.util.EcoreUtil.Copier#copyAttribute(org.eclipse.emf.ecore.EAttribute, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject)
	 */
	@Override
	protected void copyAttribute(EAttribute eAttribute, EObject eObject, EObject copyEObject) {
		int attributeVersion = EMFUtil.getVersion(eAttribute);
		int containerVersion = EMFUtil.getContainerVersion(eAttribute);
		if(attributeVersion != containerVersion) {
			super.copyAttribute(eAttribute, eObject, copyEObject);
		}
	}
	
	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.emf.ecore.util.EcoreUtil.Copier#copyContainment(org.eclipse.emf.ecore.EReference, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject)
	 */
	@Override
	protected void copyContainment(EReference eReference, EObject eObject, EObject copyEObject) {
		int attributeVersion = EMFUtil.getVersion(eReference);
		int containerVersion = EMFUtil.getContainerVersion(eReference);
		if(attributeVersion != containerVersion) {
			super.copyContainment(eReference, eObject, copyEObject);
		}
	}
	
	public static <T extends EObject> T copySelective(T eObject){
		Copier copier = new SensinactCopier();
		EObject result = copier.copy(eObject);
		copier.copyReferences();
	    
	    @SuppressWarnings("unchecked")T t = (T)result;
	    return t;
	}
}
