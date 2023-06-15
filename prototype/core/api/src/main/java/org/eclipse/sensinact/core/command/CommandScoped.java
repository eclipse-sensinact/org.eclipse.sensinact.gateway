/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.core.command;

/**
 * A Command Scoped object is only valid for use during the scope of a command
 * execution, and from the thread executing the command.
 *
 * This forces single threaded access to avoid potential thread safety problems
 * related to concurrent access.
 */
public interface CommandScoped {

    public boolean isValid();
}
