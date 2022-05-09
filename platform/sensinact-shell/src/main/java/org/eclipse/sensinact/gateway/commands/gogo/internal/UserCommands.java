/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.commands.gogo.internal;

import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.annotations.GogoCommand;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.Console;
import java.security.InvalidKeyException;

@Component(service = UserCommands.class)
@GogoCommand(
		scope = "sna", 
		function = {"su", "info"}
	)
public class UserCommands {
    
	@Reference
	private CommandComponent component;

    /**
     * Enable to switch to the anonymous user
     */
    @Descriptor("switch to the anonymous user")
    public void su() {
        try {
            component.getCommandMediator().switchUser();
        } catch (InvalidCredentialException e) {
            System.out.println("Invalid credentials. Try again.");
        } catch (DataStoreException e) {
            System.out.println("Unable to switch to user anonymous. Problem accessing the DataStore.");
        } catch (InvalidKeyException e) {
            System.out.println("Unable to switch to user anonymous. Invalid inputs.");
        }
    }

    /**
     * Enable to switch to a different sNa user
     *
     * @param userID the user ID to switch to
     */
    @Descriptor("switch to another user")
    public void su(@Descriptor("the user login") String userID) {
        Console console = System.console();
        char[] passwordChar = console.readPassword("Enter the password: ");
        try {
            component.getCommandMediator().switchUser(userID, new String(passwordChar));
        } catch (InvalidCredentialException e) {
            System.out.println("Invalid credentials. Try again.");
        } catch (DataStoreException e) {
            System.out.println("Unable to switch to user " + userID + ". Problem accessing the DataStore.");
        } catch (InvalidKeyException e) {
            System.out.println("Unable to switch to user " + userID + ". Invalid inputs.");
        }
    }

    /**
     * Provides information about the current user
     */
    @Descriptor("information about the current user")
    public void info() {
        System.out.println("Current user: " + component.getCommandMediator().getCurrentUser());
    }
}
