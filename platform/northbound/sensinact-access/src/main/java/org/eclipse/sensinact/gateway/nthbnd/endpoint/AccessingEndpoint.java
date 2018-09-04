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
package org.eclipse.sensinact.gateway.nthbnd.endpoint;

import org.eclipse.sensinact.gateway.core.security.AuthenticationToken;
import org.eclipse.sensinact.gateway.core.security.Credentials;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.LoginResponse.TokenMode;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.RegisteringResponse.RegisteringRequest;
import org.eclipse.sensinact.gateway.util.CryptoUtils;

/**
 * Connection point to a sensiNact instance allowing to create an {@link 
 * NorthboundEndpoint} for a specific user or to reactivate an existing 
 * one. It also allows to ask for a user creation, or the renewing of the 
 * password of an existing one
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class AccessingEndpoint {
    private NorthboundMediator mediator;

    /**
     * Constructor
     *
     * @param mediator the {@link NorthboundMediator} that will allow
     *                 the LoginEndpoint to be instantiated to interact with the
     *                 OSGi host environment
     * @throws InvalidCredentialException
     */
    public AccessingEndpoint(NorthboundMediator mediator) {
        super();
        this.mediator = mediator;
    }

    /**
     * Returns a newly created {@link NorthboundEndpoint} attached to
     * a {@link Session} built using the specified {@link Credentials}
     *
     * @param credentials the {@link Credentials}  that will allow
     *                    this LoginEndpoint to create a valid {@link NorthboundEndpoint}
     * @return {@link NorthboundEndpoint} for the specified {@link
     * Credentials}
     */
    public LoginResponse createNorthboundEndpoint(Credentials credentials) throws InvalidCredentialException {
        LoginResponse response = new LoginResponse(mediator, TokenMode.TOKEN_CREATION);
        if (credentials != null) {
            NorthboundEndpoints endpoints = this.mediator.getNorthboundEndpoints();
            NorthboundEndpoint endpoint = endpoints.add(new NorthboundEndpoint(this.mediator, credentials));

            long lifetime = endpoints.getLifetime();
            long timeout = endpoints.getTimeout(endpoint.getSessionToken());

            response.setTimeout(timeout);
            response.setGenerated(timeout - lifetime);
            response.setToken(endpoint.getSessionToken());

        } else {
            throw new InvalidCredentialException("Null credentials");
        }
        return response;
    }

    /**
     * Reactivates the {@link NorthboundEndpoint} attached to {@link
     * Session} whose String identifier is wrapped by the {@link
     * AuthenticationToken} passed as parameter
     *
     * @param token the String identifier of the {@link Session} attached
     *              to the {@link NorthboundEndpoint} to be reactivated
     * @return true if the appropriate the {@link NorthboundEndpoint} has
     * been reactivated; false otherwise
     */
    public LoginResponse reactivateEndpoint(AuthenticationToken token) {
        LoginResponse response = new LoginResponse(mediator, TokenMode.TOKEN_RENEW);

        String authenticationMaterial = token == null ? null : token.getAuthenticationMaterial();

        if (token != null) {
            NorthboundEndpoints endpoints = this.mediator.getNorthboundEndpoints();
            try {
                NorthboundEndpoint endpoint = endpoints.getEndpoint(token);
                if (endpoint == null) {
                    throw new NullPointerException("Null endpoint");
                }
                long lifetime = endpoints.getLifetime();
                long timeout = endpoints.getTimeout(authenticationMaterial);

                response.setTimeout(timeout);
                response.setGenerated(timeout - lifetime);
                response.setToken(authenticationMaterial);
            } catch (InvalidCredentialException | NullPointerException e) {
                mediator.error(e);
                response.setErrors(e);
            }
        } else {
            response.setErrors(new NullPointerException("Null token"));
        }
        return response;
    }

    /**
     * @param login
     * @param password
     * @param account
     * @param accountType
     * @return
     */
    public RegisteringResponse registeringEndpoint(String login, String password, String account, String accountType) {
        RegisteringResponse response = new RegisteringResponse(mediator, RegisteringRequest.USER_CREATION);
        if (login != null && password != null && account!=null && accountType!=null) {
            try {
                NorthboundEndpoint endpoint = this.mediator.getNorthboundEndpoints().getEndpoint();
                String encryptedPassword  = CryptoUtils.cryptWithMD5(password);
                endpoint.registerUser(login, encryptedPassword, account, accountType);    
                response.setMessage("Registering in progress");
                
	        } catch (Exception e) {
                response.setMessage("Error when registering");
	            response.setErrors(e);
	            mediator.error(e);
			}
        } else {
            response.setMessage("Error when registering");
            response.setErrors(new NullPointerException("login, password, account, and account type are required"));
        }
        return response;
    }

    /**
     * @param login
     * @param password
     * @param account
     * @param accountType
     * @return
     */
    public RegisteringResponse passwordRenewingEndpoint(String account) {
        RegisteringResponse response = new RegisteringResponse(mediator, RegisteringRequest.PASSWORD_RENEW);
        if (account!=null) {
            try {
                NorthboundEndpoint endpoint = this.mediator.getNorthboundEndpoints().getEndpoint();
                endpoint.renewUserPassword(account); 
                response.setMessage("Password renewing in progress");               
	        } catch (Exception e) {
                response.setMessage("Error when renewing the password");  
	            response.setErrors(e);
	            mediator.error(e);
			}
        } else {
            response.setErrors(new NullPointerException("account required"));
        }
        return response;
    }
}