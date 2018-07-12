/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.commands.gogo.internal;

import org.apache.felix.service.command.Descriptor;
import org.eclipse.sensinact.gateway.commands.gogo.osgi.CommandServiceMediator;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;

import java.io.Console;
import java.security.InvalidKeyException;

public class UserCommands {
    private CommandServiceMediator mediator;

    public UserCommands(CommandServiceMediator mediator) {
        this.mediator = mediator;
    }

    /**
     * Enable to switch to the anonymous user
     */
    @Descriptor("switch to the anonymous user")
    public void su() {
        try {
            mediator.switchUser();
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
            mediator.switchUser(userID, new String(passwordChar));
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
        System.out.println("Current user: " + mediator.getCurrentUser());
    }
}
