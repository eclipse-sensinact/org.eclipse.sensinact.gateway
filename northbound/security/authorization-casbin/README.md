# Casbin authorization

This module allows to use a Casbin definition to authorize or deny northbound users to access resources.

## Configuration

The configuration is done through the `sensinact.authorization.casbin` PID, with the following options:

- `allowByDefault`: set to `true` to allow access in cases not defined by policies (`false` by default)
- `policies`: list of policies written as `<user>, <modelPackageUri>, <model>, <provider>, <service>, <resource>, <levels>, <effect>, <priority>`
    - `<user>`: either a user name (`foo`) or a role/group (`role:admin`). Anonymous access is defined with the `role:anonymous` entry.
    - `<modelPackageUri>`: the package URI of a provider model, or `*` for any (e.g. `http://example.org/models#`)
    - `<model>`: a provider model name or `*` for any (e.g. `weatherProviderModel`)
    - `<provider>`: a provider name or `*` for any (e.g. `weatherProvider`)
    - `<service>`: a service name, or `*` for any (e.g. `sensor`)
    - `<resource>`: a resource name (e.g. `temperature`)
    - `<levels>`: a pipe-separator list of access levels: `DESCRIBE`, `READ`, `UPDATE`, `ACT` (e.g. `DESCRIBE|READ`) or `*` for all
    - `<effect>`: effect of the policy, either `allow` or `deny`
    - `<priority>`: priority of the policy, the lower the value, the higher the priority

### Sample

```json
{
    "sensinact.authorization.casbin": {
        "allowByDefault": false,
        "policies": [
            "role:user, *, *, *, *, *, *, deny, 1000",
            "role:user, *, *, *, *, *, DESCRIBE|READ, allow, 100",
            "foobar, *, *, *, input, comment, *, allow, 0",
            "externalSensor, *, *, *, sensor, *, UPDATE, allow, 0",
            "role:admin, *, *, *, *, *, *, allow, -1",
            "role:anonymous, *, *, *, *, *, *, deny, -1000",
        ]
    }
}
```
