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
package org.eclipse.sensinact.gateway.core.security.impl;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.AuthenticationService;
import org.eclipse.sensinact.gateway.core.security.Credentials;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.core.security.UserKey;
import org.eclipse.sensinact.gateway.core.security.dao.DAOException;
import org.eclipse.sensinact.gateway.core.security.dao.UserDAO;
import org.eclipse.sensinact.gateway.core.security.entity.UserEntity;
import org.eclipse.sensinact.gateway.util.CryptoUtils;

import java.security.InvalidKeyException;

public class AuthenticationServiceImpl implements AuthenticationService {
    private UserDAO userDAO;
    private Mediator mediator;

    public AuthenticationServiceImpl(Mediator mediator) throws DAOException {
        this.mediator = mediator;
        this.userDAO = new UserDAO(mediator);
    }

    /**
     * @throws InvalidKeyException
     * @throws DAOException
     * @inheritDoc
     * @see AuthenticationService#
     * getUserId(Credentials)
     */
    @Override
    public UserKey buildKey(Credentials credentials) throws InvalidKeyException, DAOException, InvalidCredentialException {
        String md5 = CryptoUtils.cryptWithMD5(credentials.password);

//		System.out.println("---------------------------");
//		System.out.println(credentials.password + "==" + md5);
//		System.out.println("---------------------------");

        UserEntity userEntity = this.userDAO.find(credentials.login, md5);
        if (userEntity == null) {
            return null;
        } else {
            return new UserKey(userEntity.getPublicKey());
        }
    }
}
