# HTTP callback

Eclipse sensiNact provides an HTTP callback endpoint to handle updates pushed by sensors in HTTP.

## Bundles

The HTTP callback is provided by the bundle `org.eclipse.sensinact.gateway.southbound.http:http-callback-whiteboard:0.0.2`

It requires the sensiNact REST gateway feature.

## Configuration

An HTTP callback is defined by a configuration with PID `sensinact.http.callback.whiteboard`.

The configuration accepts the following entry:
* `base.uri`: base URI of the HTTP endpoint. If not set, it will be computed from the HTTP service properties.

The configuration is required for the HTTP callback service to be initiatied, but the configuration can be empty.

The root request URI will be `<base.uri>/southbound/callback/`.

## HTTP listener

The request listener will be a service registered for interface `org.eclipse.sensinact.gateway.southbound.http.callback.api.HttpCallback`.
Each listener detected by the HTTP callback will be associated to a unique ID (UUID) and to a unique HTTP path: `<base.uri>/southbound/callback/<UUID>`.

The URI associated to the listener will be be given to with its `activate(String uri)` method.

When a POST HTTP request is received on the URI of a listener, the latter will be notified using its `call(String uri, Map<String, String> headers, BufferedReader bodyReader)` method; note that the callback cannot reply to the client.
An HTTP 204 (No content) status code will be returned to the client when the callback method returns.
If the callback method throws a `RuntimeException`, an HTTP 500 status code will be returned to the client without error message nor stack trace.

When the HTTP callback service is disabled, all the listeners are disabled and notified with their `deactivate(uri)` method.

## Example

```java
import org.eclipse.sensinact.gateway.southbound.http.callback.api.RequireHttpCallback;

@Component()
@RequireHttpCallback
class HttpHandler implements HttpCallback {
    @Override
    void activate(String uri) {
        System.out.println("HTTP callback available at: " + uri);
    }

    @Override
    void deactivate(String uri) {
        System.out.println("HTTP callback at " + uri + " has been disabled.");
    }

    @Override
    void call(String uri, Map<String, List<String>> headers, Reader body) {
        System.out.println("Got message at " + uri + ": " + new BufferedReader(body).lines().collect(Collectors.joining()));
    }
}
```
