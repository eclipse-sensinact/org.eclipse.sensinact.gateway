/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core.security.access.impl;

import java.lang.reflect.Method;
import java.security.InvalidKeyException;
import java.util.HashMap;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.security.AbstractUserUpdater;
import org.eclipse.sensinact.gateway.core.security.SecuredAccessException;
import org.eclipse.sensinact.gateway.core.security.SecurityDataStoreService;
import org.eclipse.sensinact.gateway.core.security.User;
import org.eclipse.sensinact.gateway.core.security.UserManager;
import org.eclipse.sensinact.gateway.core.security.UserManagerFinalizer;
import org.eclipse.sensinact.gateway.core.security.UserUpdater;
import org.eclipse.sensinact.gateway.core.security.dao.DAOException;
import org.eclipse.sensinact.gateway.core.security.dao.UserDAO;
import org.eclipse.sensinact.gateway.core.security.entity.UserEntity;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.util.CryptoUtils;
import org.osgi.framework.ServiceReference;

/**
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class UserManagerImpl implements UserManager {
	
	private Mediator mediator;
	private UserDAO userDAO;
	private UserEntity anonymous;

	/**
	 * 
	 * @param mediator
	 * @throws DataStoreException
	 * @throws DAOException
	 * 
	 */
	public UserManagerImpl(Mediator mediator) throws SecuredAccessException {
		this.mediator = mediator;
		try {
			ServiceReference<SecurityDataStoreService> reference = this.mediator.getContext(
					).getServiceReference(SecurityDataStoreService.class);
			this.userDAO = new UserDAO(mediator, this.mediator.getContext().getService(reference));
			anonymous = userDAO.find(ANONYMOUS_ID);
		} catch (DataStoreException | NullPointerException | IllegalArgumentException e) {
			mediator.error(e);
			throw new SecuredAccessException(e);
		}
	}

	public void finalize() {
		this.userDAO = null;
		try {
			ServiceReference<SecurityDataStoreService> reference = this.mediator.getContext(
				).getServiceReference(SecurityDataStoreService.class);
			this.mediator.getContext().ungetService(reference);
		} catch (NullPointerException | IllegalArgumentException e) {
			mediator.error(e);
		}
	}

	@Override
	public boolean loginExists(final String login) throws SecuredAccessException, DataStoreException {
		return this.userDAO.select(new HashMap<String, Object>() {{
				this.put("SULOGIN", login);}}).size()>0;
	}

	@Override
	public boolean accountExists(String account) throws SecuredAccessException, DataStoreException {
		return this.userDAO.findFromAccount(account) != null;
	}

	@Override
	public User getUser(String login, String password) throws SecuredAccessException, DataStoreException {
		return userDAO.find(login, password);
	}
	
	@Override
	public User getUserFromPublicKey(String publicKey) throws SecuredAccessException, DataStoreException {
		if (publicKey == null) {
			return anonymous;
		}
		return userDAO.find(publicKey);
	}

	@Override
	public User getUserFromAccount(String account) throws SecuredAccessException, DataStoreException {
		if (account == null) {
			return anonymous;
		}
		return userDAO.findFromAccount(account);
	}

	@Override
	public UserUpdater createUser(String token, final String login, final String password, final String account, final String accountType) throws SecuredAccessException {
		return new AbstractUserUpdater(mediator, token, "create") {
			@Override
			public String getAccount() {
				return account;
			}
			@Override
			public String getAccountType() {
				return accountType;
			}
			@Override
			protected String doUpdate() throws SecuredAccessException {
				final String publicKey;
				String publicKeyStr = new StringBuilder().append(login).append(":").append(account
						).append(System.currentTimeMillis()).toString();
				try {
					publicKey = CryptoUtils.cryptWithMD5(publicKeyStr);
					UserEntity user = new UserEntity(mediator, login, password, account, accountType, publicKey);
					UserManagerImpl.this.userDAO.create(user);
					UserManagerImpl.this.mediator.callServices(UserManagerFinalizer.class,new Executable<UserManagerFinalizer,Void>() {
						@Override
						public Void execute(UserManagerFinalizer finalizer) throws Exception {
							finalizer.userCreated(login, publicKey, account, accountType);
							return null;
						}}
					);
					return new StringBuilder().append("Public Key : ").append(publicKey).toString();
				} catch(DAOException | DataStoreException | InvalidKeyException e) {
					throw new SecuredAccessException(e);
				}
			}
			
		};
	}

	@Override
	public UserUpdater renewUserPassword(String token, final String account, final String accountType) throws SecuredAccessException {
		return new AbstractUserUpdater(mediator, token, "renew") {
			static final String ALPHABET = ".!0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
			@Override
			public String getAccount() {
				return account;
			}
			@Override
			public String getAccountType() {
				return accountType;
			}
			@Override
			protected String doUpdate() throws SecuredAccessException {		
				try {
					User user = UserManagerImpl.this.getUserFromAccount(account);
					StringBuilder builder = new StringBuilder();					
					do {
						String millis = String.valueOf(System.currentTimeMillis());
						for(int i=millis.length()-1;i>6;i--) {
							int val = Integer.parseInt(millis.substring(i-1, i+1));
							int hval = Integer.parseInt(millis.substring(i-1, i+1),16);
							int index = -1;
							if((index = ALPHABET.indexOf(val))>-1){
								builder.append(ALPHABET.substring(index, index+1));
							} else if((index = ALPHABET.indexOf(hval))>-1){
								builder.append(ALPHABET.substring(index, index+1));
							} else if(val < millis.length()) {
								builder.append(ALPHABET.substring(val, val+1));
							}else if(hval < millis.length()) {
								builder.append(ALPHABET.substring(hval, hval+1));
							}
						}
						Thread.sleep(345);
					}while(builder.length() <= 10);
					String password = builder.toString();
					String encryptedPassword = CryptoUtils.cryptWithMD5(password);
					((UserEntity)user).setPassword(encryptedPassword);
					UserManagerImpl.this.userDAO.update((UserEntity) user);
					return new StringBuilder().append("Your new password : ").append(password).toString();
				} catch(DAOException | DataStoreException | InvalidKeyException | InterruptedException e) {
					throw new SecuredAccessException(e);
				}
			}
		};
	}

	@Override
	public void updateField(User user, String fieldName, Object oldValue, Object newValue) throws SecuredAccessException {
		try {
			Class<? extends User> clazz = user.getClass();		
			String getPrefix = "get";
			String setPrefix = "set";
			String suffix = new StringBuilder().append(fieldName.substring(0, 1
				).toUpperCase()).append(fieldName.substring(1)).toString();
			Method getMethod = clazz.getMethod(new StringBuilder().append(getPrefix).append(suffix
				).toString());
			Object current = getMethod.invoke(user);
			if((current==null && oldValue!=null)||(current!=null && !current.equals(oldValue)))
				throw new SecuredAccessException("Invalid current value");
			
			Method setMethod = clazz.getMethod(new StringBuilder().append(setPrefix).append(suffix
				).toString(), newValue.getClass());
			setMethod.invoke(user, newValue);			
		} catch (Exception e) {
			throw new SecuredAccessException(e);
		}
	}
}