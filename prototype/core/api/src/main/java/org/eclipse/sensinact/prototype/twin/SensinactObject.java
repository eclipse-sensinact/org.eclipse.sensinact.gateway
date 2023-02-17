/**
 * Copyright (c) 2012 - 2023 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.sensinact.prototype.twin;

import java.time.Instant;

import org.eclipse.emf.ecore.EObject;
import org.osgi.util.promise.Promise;

public interface SensinactObject {

    Promise<Void> update(EObject eObject, Instant timestamp);

}
