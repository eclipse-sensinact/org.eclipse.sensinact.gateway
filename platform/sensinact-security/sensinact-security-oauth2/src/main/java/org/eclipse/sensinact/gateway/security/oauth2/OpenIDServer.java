/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.security.oauth2;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Hashtable;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfigurationImpl;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleRequest;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleResponse;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.spi.JsonProvider;

public class OpenIDServer extends IdentityServerWrapper implements OAuthServer {

	private Hashtable<String, UserInfo> credentials;
	UserInfo anonymous;
	URI authEP;
	URI tokenEP;
	URI userinfoEP;
	String publicKey;
	String client_id;
	String client_secret;
	String issuer;
	StringBuilder returnToUrl;
	String localAuth;
	Properties properties;

	public OpenIDServer(BundleContext context, String configfile) {
		anonymous = new OpenID();
		String discoveryURL = null;
		String certsURL = null;
		try {
			properties = new Properties();
			properties.load(new FileInputStream(configfile));
			
			discoveryURL = properties.getProperty("discoveryURL").toString();
			certsURL = properties.getProperty("certsURL");
			
			client_id = properties.getProperty("client_id").toString();
			client_secret = properties.getProperty("client_secret").toString();

			Bundle bundles[] = context.getBundles();
			for (Bundle bundle : bundles) {
				checkBundle(bundle);
			}
			context.addBundleListener(new BundleListener() {
				public void bundleChanged(BundleEvent e) {
					checkBundle(e.getBundle());
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		credentials = new Hashtable<String, UserInfo>();
		localAuth = System.getProperty(AUTH_BASEURL_PROP, AUTH_BASEURL_DEFAULT);
		JsonProvider provider = JsonProviderFactory.getProvider();
		try {
			ConnectionConfigurationImpl<SimpleResponse, SimpleRequest> conf = new ConnectionConfigurationImpl<SimpleResponse, SimpleRequest>();
			conf.setHttpMethod("GET");
			conf.setAccept("application/json");
			conf.setUri(discoveryURL.toString());
			
			SimpleResponse response = new SimpleRequest(conf).send();
			int status = response.getStatusCode();
			if (status == 200) {				
				JsonObject jsonObject = provider.createReader(
						new ByteArrayInputStream(response.getContent())).readObject();
				authEP = new URI(jsonObject.getString("authorization_endpoint"));
				tokenEP = new URI(jsonObject.getString("token_endpoint"));
				userinfoEP = new URI(jsonObject.getString("userinfo_endpoint"));
				issuer = jsonObject.getString("issuer");
			}
			conf = new ConnectionConfigurationImpl<SimpleResponse, SimpleRequest>();
			conf.setHttpMethod("GET");
			conf.setAccept("application/json");
			conf.setUri(certsURL.toString());
			
			response = new SimpleRequest(conf).send();
			status = response.getStatusCode();
			if (status == 200) {				
				JsonArray array = provider.createReader(
						new ByteArrayInputStream(response.getContent())).readObject().getJsonArray("keys");
				JsonObject keys = array.getJsonObject(0);				
				publicKey = new StringBuilder(
					).append(keys.getString("n")
					).append("."
					).append(keys.getString("e")
					).toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void checkBundle(Bundle bundle) {
		String bundlename = bundle.getSymbolicName();

		String bundleconfig;
		bundleconfig = properties.getProperty(bundlename);
		if (bundleconfig != null) {
			String configs[] = bundleconfig.split(":");
			register(configs[0], configs[1], Pattern.compile(configs[2]));
		}

		int i = 0;
		bundleconfig = properties.getProperty(bundlename + "[" + i + "]");
		while (bundleconfig != null) {
			String configs[] = bundleconfig.split(":");
			register(configs[0], configs[1], Pattern.compile(configs[2]));
			i++;
			bundleconfig = properties.getProperty(bundlename + "[" + i + "]");
		}
		try {
			Properties properties2 = new Properties();
			FileInputStream input = new FileInputStream("cfgs/" + bundlename + ".config");
			properties2.load(input);
			bundleconfig = properties2.getProperty("securityfilter");
			if (bundleconfig != null) {
				String configs[] = bundleconfig.split(":");
				register(configs[0], configs[1], Pattern.compile(configs[2]));
			}
			i = 0;
			bundleconfig = properties2.getProperty("securityfilter[" + i + "]");
			while (bundleconfig != null) {
				String configs[] = bundleconfig.split(":");
				register(configs[0], configs[1], Pattern.compile(configs[2]));
				i++;
				bundleconfig = properties2.getProperty("securityfilter[" + i + "]");
			}
		} catch (IOException e) {
		}
	}

	String getClientId() {
		return client_id;
	}

	String getClientSecret() {
		return client_secret;
	}

	String getPublicKey() {
		return publicKey;
	}

	/**********************************************/
	/** oAuthServer API **/
	/**********************************************/
	@Override
	public JsonObject verify(String code, ServletRequest req) {
		JsonObject jsonObject = null;
		ConnectionConfigurationImpl<SimpleResponse, SimpleRequest> conf = new ConnectionConfigurationImpl<SimpleResponse, SimpleRequest>();
		conf.setHttpMethod("POST");
		conf.setContentType("application/x-www-form-urlencoded");
		JsonProvider provider = JsonProviderFactory.getProvider();
		try {
			conf.setUri(tokenEP.toURL().toExternalForm());
			String credentials = new String(client_id + ":" + client_secret);
			String basic = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
			conf.addHeader("Authorization", "Basic " + basic);
			
			StringBuilder urlParameters = new StringBuilder();
			
			if (returnToUrl == null) {
				returnToUrl = new StringBuilder().append(req.getScheme()
					).append("://").append(req.getServerName()).append(":"
					).append(req.getServerPort()).append(localAuth);		
			}
			urlParameters.append("redirect_uri=");
			urlParameters.append(returnToUrl.toString());		
			urlParameters.append("&client_id=");
			urlParameters.append(client_id);
			urlParameters.append("&code=");
			urlParameters.append(code);
			urlParameters.append("&scope=openid%20roles");
			urlParameters.append("&grant_type=authorization_code");
			urlParameters.append("&response_type=id_token%20token");
			conf.setContent(urlParameters.toString());
			
			String access_token = null;
			SimpleResponse response;
			try {
				response = new SimpleRequest(conf).send();
				int status = response.getStatusCode();
				if (status == 200) {
					jsonObject = provider.createReader(
							new ByteArrayInputStream(response.getContent())).readObject();
					access_token = jsonObject.getString("access_token");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (access_token != null) {
				return jsonObject;
			}
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean handleSecurity(ServletRequest request, ServletResponse response) {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		
		if (localAuth.equals(req.getPathInfo()))
			return true;
		try {
			returnToUrl = new StringBuilder().append(req.getScheme()
			).append("://").append(req.getServerName()).append(":"
			).append(req.getServerPort()).append(localAuth);
			
			StringBuilder builder= new StringBuilder().append(authEP.getScheme()
				).append("://").append(authEP.getHost()).append(":"
				).append(authEP.getPort()).append(authEP.getPath()
				).append("?").append("redirect_uri"
				).append("=").append(returnToUrl.toString()
				).append("&").append("client_id").append("="
				).append(client_id).append("&").append("scope"
				).append("=").append("openid%20profile%20roles"
				).append("&").append("response_type").append("="
				).append("code");
			res.sendRedirect(builder.toString());

			HttpSession session = req.getSession();
			session.setAttribute("redirect_uri", req.getRequestURI());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public UserInfo check(String access_token) throws IOException {
		OpenID found = (OpenID) credentials.get(access_token);
		if (found != null)
			return found;
		try {
			found = new OpenID(this, access_token);
			if (found.isValid())
				return found;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public String basicToken(ServletRequest request, String authorization) {
		try {
			ConnectionConfigurationImpl<SimpleResponse, SimpleRequest> conf = new ConnectionConfigurationImpl<SimpleResponse, SimpleRequest>();
			conf.setHttpMethod("POST");
			conf.setContentType("application/x-www-form-urlencoded");
			
			conf.setUri(tokenEP.toURL().toExternalForm());
			String credentials = new String(client_id + ":" + client_secret);
			String basic = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
			conf.addHeader("Authorization", "Basic " + basic);
			
			StringBuilder urlParameters = new StringBuilder();
			HttpServletRequest req = (HttpServletRequest) request;
				
			if (returnToUrl == null) {
				returnToUrl = new StringBuilder().append(req.getScheme()
					).append("://").append(req.getServerName()).append(":"
					).append(req.getServerPort()).append(localAuth);		
			}		
			String credentialsStr = new String(Base64.getDecoder().decode(authorization.substring(6)));
			String credentialsArr[] = credentialsStr.split(":");
			String username = credentialsArr[0];
			String password = credentialsArr[1];
			
			urlParameters.append("redirect_uri=");
			urlParameters.append(returnToUrl.toString());		
			urlParameters.append("&client_id=");
			urlParameters.append(client_id);
			urlParameters.append("&username=");
			urlParameters.append(username);
			urlParameters.append("&password=");
			urlParameters.append(password);
			urlParameters.append("&scope=openid%20roles");
			urlParameters.append("&grant_type=password");
			urlParameters.append("&response_type=id_token%20token");
			conf.setContent(urlParameters.toString());
			
			SimpleResponse response = new SimpleRequest(conf).send();
			int status = response.getStatusCode();
			if (status == 200) {
				JsonObject obj = JsonProviderFactory.getProvider().createReader(
						new ByteArrayInputStream(response.getContent())).readObject();
				return obj.getString("access_token");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public UserInfo anonymous() {
		return anonymous;
	}

	public void addCredentials(String token, UserInfo newUser) {
		((OpenID) newUser).addAccessToken(token);
		credentials.put(token, newUser);
	}

	/**********************************************/
	/** IdentityServer API **/
	/**********************************************/
	
	@Override
	public UserInfo getUserInfo(String token, String authorization) {
		OpenID user = null;
		try {
			ConnectionConfigurationImpl<SimpleResponse, SimpleRequest> conf = new ConnectionConfigurationImpl<SimpleResponse, SimpleRequest>();
			conf.setHttpMethod("GET");
			conf.setUri(userinfoEP.toURL().toExternalForm()+ "?client_id=" + client_id);
			conf.addHeader("Authorization", "Bearer " + authorization);	
			
			SimpleResponse response = new SimpleRequest(conf).send();
			int status = response.getStatusCode();
			if (status == 200) {
				JsonObject jsonObject = JsonProviderFactory.getProvider().createReader(
						new ByteArrayInputStream(response.getContent())).readObject();
				user = new OpenID(jsonObject);
			} else {
				System.out.println(userinfoEP + " response : " + status);
				System.out.println(response.getHeaders());
				System.out.println("error " + new String(response.getContent(),"UTF-8"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return user;
	}

	public boolean check(UserInfo user, ServletRequest request) {
		return super.check(user, request);
	}
}
