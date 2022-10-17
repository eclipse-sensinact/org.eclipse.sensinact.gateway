# Sensinact: OpenID security plugin

## Introduction

Filter on any HTTP request of the server. It will check the rights and will 
redirect the user on the OpenID server or will give the access to the service 
requested.

## OpenID strategy

![OIDC schemas](./Schemas_OIDC.png)

The module is the relaying part.

## Implementation architecture

The module will check the rights, and the authorization code with the OIDC.

## Configuration

The module has to know some information like the OIDC address.
This configuration should be available inside *securityfilter.config* property
file

```bash
$ cat security.config 
discoveryURL=http://localhost:8180/auth/realms/master/.well-known/openid-configuration
client_secret=0f724088-70c7-4e8b-b339-cc1286cdae81
client_id=test_id
```

`discoveryURL` is use to set the address of the OICD configuration URL.
The servlet will read the content of this URL and uses it to known 
the different information about the OIDC.  
`client_id` and `client_secret` are the login information of the relaying part
on the OIDC.

As it, the module should refuse any requests and the rights' rules
has to be set first.

To ways are availables to do it.

Each service may use a configuration file that is inside the sensinact 
distribution's *cfgs* directory

```bash
$ cat temperatures-generator.config
# Specify the number of devices that must be generated (max is 8601)
org.eclipse.sensinact.simulated.generator.amount=10
securityfilter[0]=anonymous:GET:*
securityfilter[1]=admin:POST:*
```
All configuration of the security may be gathered in the global *org.eclipse.sensinact.security.oAuth2.config* configuration file

```bash
$ cat org.eclipse.sensinact.security.oAuth2.config 
discoveryURL=http://localhost:8180/auth/realms/master/.well-known/openid-configuration
client_secret=0f724088-70c7-4e8b-b339-cc1286cdae81
client_id=test_id
light=anonymous:GET:/sensinact/light/*
light[0]=anonymous:GET:/sensinact/light/*
light[1]=admin:POST:/sensinact/light/*
slider[0]=anonymous:GET:/sensinact/slider/*/*
slider[1]=admin:GET:/sensinact/slider/254/*
```

After the OIDC setting, each line describes an access's rule to a service.

Ex: `slider[0]=anonymous:GET:/sensinact/slider/*/*`
_service_: `slider`
_rule number_: `[0]`
_role_: `anonymous`
_action_: `GET`
_regular expression_: `/sensinact/slider/*/*`

If _service_ is available, _rule number_ are loaded.  
The users with the `anonymous` _role_ are allowed to request a _GET_
on all URL that match the _regular expression_.

The OIDC must contain all users and their roles for the `client_id`.

There is two specials users:
 * `anonymous`: It's not real user, and all request on the _regular expression_ will be accepted without OIDC authorization.
 * `admin`: The users with the `admin` role should be allowed to access on any URL.

The _regular expression_ may be more complicated and may contain special fields (`user`,`group`,`role`).

Ex:
    light[0]=users:GET:/sensinact/light/*  
    light[1]=users:POST:/sensinact/light/${user}/*

For the `light` service all users with the role `users` may `GET` all URL
in `/sensinact/light/*` but only `JDoe` with the role `users` may `POST` on 
`/sensinact/light/JDoe/*`

