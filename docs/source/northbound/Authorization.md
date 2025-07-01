# Authorization

The Northbound security API defines an AuthorizationEngine interface that can be implemented by Authorization providers.
The authorization engine will be called for each session creation, giving a user description including the user name and the known groups the user belongs to.
An authorization engine can provide two kinds of authorizers:

- the PreAuthorizer, which will always be called outside of the gateway thread. Its purpose it to avoid running a task in the gateway thread while it is already known the user can't access the associated resources. It is also called after a task returned, to filter out resources the user shouldn't be able to see. The PreAuthorizer can indicate that an operation is allowed, denied or in an unknown state. The latter is useful when no explicit rule match the operation. In that case, the task will be given to the gateway thread.
- the Authorizer, which is called in the gateway thread. Its implementation should be fast, in order to avoid slowing down the platform. Unlike the PreAuthorizer, the Authorizer must indicate if the operation is allowed or denied, even if no rule matches the operation. The choice of allowing or denying such operation is implementation specific.

It is possible to use the same object to implement both the PreAuthorizer and Authorizer.

## Casbin Authorization Engine

Eclipse sensiNact provides an authorization engine implementation based on [jCasbin](https://github.com/casbin/jcasbin), `org.eclipse.sensinact.gateway.northbound.security.authorization-casbin`.

It is configured using the `sensinact.authorization.casbin` PID, with the following options:

- `policies`: a list of Casbin policies
- `allowByDefault`: if true, allow anonymous read access and user read/write access by default, else reject any operation not explicitly allowed (false by default)

### Policies

The Casbin policies format used byu Eclipse sensiNact is as follows:
`role, modelPackageUri, model, provider, service, resource, operations, effect, priority`

- `role`: either a user name (`foobar`, `admin`) or a role name prefixed by `role:` (`role:user`, `role:admin`) or `anonymous`
- `modelPackageUri`: the package URI of a target provider model. It can be `*` for any or a regular expression (`http://example\.org/.*`)
- `model`: the name of a model of providers. It can be `*` for any or a regular expression (`model-sensor-[0-9a-fA-F]+`)
- `provider`: the name of a target provider. It can be `*` for any or a regular expression (`provider-\d+`)
- `service`: the name of a target service. It can be `*` for any or a regular expression (`sensor-.*-\d+`)
- `resource`: the name of a target resource. It can be `*` for any or a regular expression (`.*_value`)
- `operations`: the permission level (`DESCRIBE`, `READ`, `UPDATE`, `ACT`). It is possible to indicate multiple levels in a pipe-separated list (`DESCRIBE|READ|UPDATE`)
- `effect`: the policy effect (`allow` or `deny`)
- `priority`: the priority of the rule, the lower the value, the higher the priority

### Sample configuration

Here is a sample authorization configuration:

```js
{
    "sensinact.authorization.casbin": {
        // Reject operations not explicitly listed
        "allowByDefault": false,
        "policies": [
            // Anonymous sessions can't do anything
            "anonymous, *, *, *, *, *, *, deny, -10000",
            // User can read anything but the resources in a "private" service of any provider
            // Note that user won't be able to update nor act or resources.
            // Having different priorities ensures the rules will be applied as expected
            "role:user, *, *, *, *, *, DESCRIBE|READ, 1000",
            "role:user, *, *, *, private, *, *, deny, 999",
            // Users in the manager group can act on action resources named "apply"
            // of any service in providers which name ends with "-management"
            "role:manager, *, *, .*-management, *, apply, ACT, allow, 0",
            // All users can describe and read all resources of the sensiNact provider.
            // Note the priority 0 being higher than -10000,
            // it doesn't override the denial rule for anonymous sessions
            "*, *, *, sensiNact, *, *, *, DESCRIBE|READ, allow, 0"
        ]
    }
}
```
