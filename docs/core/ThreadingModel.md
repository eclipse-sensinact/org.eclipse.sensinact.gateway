# The sensiNact core threading model

The primary goal of sensiNact is to be a lightweight gateway, capable of providing access to a wide variety of sensors and actuators. In addition a sensiNact gateway may host application code, which runs either in response to data changes, or that can be triggered in some other way.

In order to maintain a simple, lightweight runtime for the core it is important to define the threading model, including which threads have direct access to read/mutate the internal data.

## The sensiNact gateway thread

When running sensiNact maintains an internal "digital twin" of the devices that it provides access to. All operations that are made on the twinned data model are performed by a single thread known as the **gateway thread**. Using a single thread reduces the resources needed to run the gateway, and also simplifies the internal data model. Read and update operations are submitted as commands which are executed by the gateway thread.

### Bulk operations

Bulk update/read operations can be performed in a single command that is submitted to the gateway thread. By executing as a single command multiple operations can be applied together, in such a way that other operations cannot see a partial update (i.e. only some of the update operations have been applied).

Note that there is no intent for this to make bulk operations truly *atomic*, specifically if part of the update fails then the successful part will not be automatically rolled back. It is, however, the case that commands do not need to worry about *dirty read/write* operations, or about the impact of concurrent data access/update.

### Notifications

Once all of the updates and actions performed by a command have been processed then notifications for these actions are generated and sent using OSGi Typed Events. Using Typed Events ensures that notifications are delivered asynchronously and do not block the gateway thread. By delaying notifications until all of the updates are complete we can ensure that listeners do not act on partial data.

If multiple notifications are generated for the same resource (e.g. the value of a resource is updated twice) then these notifications will be *debounced* and collapsed into a single notification. This avoids sending confusing notifications for a value update that cannot be seen.

### Potential drawbacks

As all commands are executed sequentially by a single thread there is the possibility for significant latency and/or scaling issues. All commands must execute quickly, but as data access does not require synchronization or locking this will be the default case normally.

If long-running tasks must be performed then this work should be performed on a separate thread and the result applied in a separate command. A `Promise` may also be helpfully used in these situations, such as the return value from a pull-based resource.


## Northbound provider access

Northbound providers offer external access to sensiNact, e.g. HTTP/MQTT/WebSocket. As such they will all have their own threading models. Northbound providers must therefore convert their incoming requests into commands that will be executed by the sensiNact gateway thread. These commands will provide access to their result using a `Promise` which can then be used in a response if required by the Northbound provider.

## Southbound provider access

Southbound providers can operate in one of two ways, *push based* or *pull based*

### Push based providers

Push based Southbound providers receive data pushed from a sensor or actuator. This data should be converted into a suitable update DTO and passed to the `PrototypePush` service which will wrap the update data in a command and submit it to the gateway thread. Once the gateway thread has processed the update then the returned `Promise` will be resolved.

### Pull based providers

Pull based Southbound providers retrieve data from a sensor or actuator when triggered. This may be a periodic call, or it may be as a result of a read request.

Whatever the reason for the trigger the request will not occur on the gateway thread, and therefore the calling thread may be used to contact the device. Pull based providers that use long running actions (e.g. MQTT/HTTP) to read are encouraged to use `Promise` return values to avoid exhausting the request threads.

If the pull-based request is as a result of a read operation then the initial command will complete rapidly to vacate the gateway thread. Once the pull-based request is complete a chained command will be submitted to update the internal data model and also to resolve the response of the original command.
