# SensiNact metrics

The Eclipse sensiNact core provides a metrics service to gather statistics about its use.
The API to access this service is defined in the core API bundle and its implementation is in the core `impl` bundle.
The metrics service implementation is based on the [Metrics](https://metrics.dropwizard.io/4.2.0/) library.

## Kinds of metrics

The sensiNact core metrics provides 4 kinds of metrics:
* Counter: a simple counter that can be incremented or decremented by 1. Only its current value is used in reports.
* Gauge: a registered method is called only when a report is generated. It is supposed to return a number.
* Histogram: a reservoir of values that will be able to generate various statistics on the metric:
  * count of value updates
  * min, max, average values
  * standard deviation
  * median (50th), 75th, 95th, 98th, 99th and 99.9th percentiles
* Timer: a reservoir that keeps track of the time taken to execute a task that will be able to generate various statistics:
  * count of calls
  * min, max, average times to execute task
  * standard deviation
  * median (50th), 75th, 95th, 98th, 99th and 99.9th percentiles
  * average call rate (number of calls per second)
  * average call rates for the last minute, last 5 minutes and last 15 minutes

Rates are given in number of calls per seconds while durations are given in milliseconds.

## Core metrics

Here is the list of metrics measured in the sensiNact core bundle:

* Gateway thread:
  * `sensinact.tasks.pending`: number of pending tasks at the time of the report
  * `sensinact.tasks.pending.hist`: histogram of the number of pending tasks
  * `sensinact.task.time`: time taken to execute each task in the gateway thread
* Session manager:
  * `sensinact.sessions`: total number of active sessions (anonymous and user sessions)  at the time of the report
* Whiteboard
  * `sensinact.whiteboard.act.task`: time taken to execute each `ACT` command implementation for action resources
  * `sensinact.whiteboard.act.request`: time taken to execute each `ACT` command implementation and update the twin for action resources
  * `sensinact.whiteboard.pull.task`: time taken to execute each task `GET` command implementation for pull-based resources
  * `sensinact.whiteboard.pull.request`: time taken to execute each `GET` command implementation and update the twin for action resources
  * `sensinact.whiteboard.push.task`: time taken to execute each task `SET` command implementation for push-based resources
  * `sensinact.whiteboard.push.request`: time taken to execute each `SET` command implementation and update the twin for action resources

The core bundle also measures metrics about the Java Virtual Machine:
* `jvm.memory.heap.max`: maximum allowed heap size
* `jvm.memory.heap.total`: total allocated heap size
* `jvm.memory.heap.free`: available heap size
* `jvm.memory.heap.usage`: size of heap in use (total minus free heap size)

More metrics might be added in the future in different parts of the sensiNact provided bundles.
You can add custom metrics as explained in [this section](#store-metrics).

## Metrics service configuration

The metrics service is always registered even if metrics are disabled.
In that case, the metrics service returns dummy metrics handlers to avoid the caller to check for `null` objects.

The sensiNact metrics service expects a configuration with PID `sensinact.metrics` and accepts the following entries:

* `enabled`: boolean to enable metrics gathering (false by default).
  If false, the metrics service will return dummy metrics handlers and no metrics reporter will be started.
  If true, the metrics listeners will be notified for each generated report.
* `metrics.rate`: delay in seconds between two metrics reports (10 seconds by default).
* `metrics.enabled`: explicit list of names of metrics to measure (empty by default).
  Only the metrics listed here will be measured, others will be given a dummy metrics handlers.
  If the list is empty, then all metrics are measured.
* `provider.enabled`: if true, the metrics will be stored as resources in a sensiNact provider (true by default).
* `provider.name`: name of the sensiNact provider where metrics will be stored (`sensiNact-metrics` by default).
* `provider.model`: name of the model of the sensiNact provider where metrics will be stored (`sensiNact-metrics` by default).
* `console.enabled`: if true, the metrics will be displayed in the standard output of sensiNact (false by default).

### Note on the metrics provider

The metrics provider store the metrics values as resources.
If the names of a metric contains a dot (`.`), then the part before the first dot will be the service name.
Dots in the rest of the name will be converted to dashes (`-`) to obtain a valid resource name.

Statistics of complex metrics like histograms and timers are stored in two ways:
* the resource that has the same name as the metric holds a map that describes all the statistics
* resources prefixed with the resource name and suffixed with the following names contain each the specific statistic value:
  * `-count`: Number of calls / updates
  * `-mean-rate`: Average number of calls per seconds
  * `-1min-rate`: Average number of calls per seconds in the last minute
  * `-5min-rate`: Average number of calls per seconds in the last 5 minutes
  * `-15min-rate`: Average number of calls per seconds in the last 15 minutes
  * `-min`: Minimum value / time
  * `-max`: Maximum value / time
  * `-mean`: Mean value / time
  * `-stddev`: Standard deviation of values / times
  * `-p50`: 50th percentile of values / times
  * `-p75`: 75th percentile of values / times
  * `-p95`: 95th percentile of values / times
  * `-p98`: 98th percentile of values / times
  * `-p99`: 99th percentile of values / times
  * `-p99_9`: 99.9th percentile of values / times

All reported metrics are stored in the sensiNact twin model at once using the `DataUpdate` service.
As a result, if many metrics have to be stored, the operation might impact the measured metrics themselves, like the core task time.

## Listen to metrics reports

You can listen to metrics reports by registering an `IMetricsListener` service.
The service must implement the `onMetricsReport(BulkGenericDto)` method that will be called after each report generation.

The `BulkGenericDto` provides a list of `GenericDto` objects that represents the resources as defined the [Core metrics section](#sensinact-metrics) and in the [metrics provider description](#note-on-the-metrics-provider).

## Store metrics

It is possible to store your own metrics in sensiNact by calling the `IMetricsManager` service.
Make sure that the callback functions for any gauges you register execute quickly.
If they block, or require significant computation, then they will significantly slow the metrics report generation *and* impact the measured metrics.

Here is an example to add some system metrics using the [Oshi](https://www.oshi.ooo/) library.

### Setup the project

Setup a new Maven project.

We will use the following compile-level dependencies:
* `org.eclipse.sensinact.gateway.core:api`: sensiNact API to have access to the `IMetricsManager` interface
* `com.github.oshi:oshi-core:6.4.4`: Oshi library as used in this tutorial

The integration tests will require the sensiNact implementation. The Metrics library will be found by transitivity:
* `org.eclipse.sensinact.gateway.core:impl`

Here is an example POM file:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.eclipse.sensinact.tutorials</groupId>
  <artifactId>metrics-oshi</artifactId>
  <version>0.0.2-SNAPSHOT</version>
  <name>System Metrics for Eclipse sensiNact</name>
  <description>Addition of system metrics for sensinact</description>
  <properties>
    <sensinact.version>0.0.2</sensinact.version>
    <bnd.version>6.4.0</bnd.version>
  </properties>
  <dependencies>
    <dependency>
      <groupId>org.eclipse.sensinact.gateway.core</groupId>
      <artifactId>api</artifactId>
      <version>${sensinact.version}</version>
    </dependency>
    <dependency>
      <groupId>org.osgi</groupId>
      <artifactId>org.osgi.service.component.annotations</artifactId>
      <version>${sensinact.version}</version>
    </dependency>
    <dependency>
      <groupId>com.github.oshi</groupId>
      <artifactId>oshi-core</artifactId>
      <version>6.4.4</version>
    </dependency>
  </dependencies>
  <build>
    <plugins>
      <plugin>
        <groupId>biz.aQute.bnd</groupId>
        <artifactId>bnd-maven-plugin</artifactId>
        <version>${bnd.version}</version>
      </plugin>
      <plugin>
        <groupId>biz.aQute.bnd</groupId>
        <artifactId>bnd-testing-maven-plugin</artifactId>
        <version>${bnd.version}</version>
      </plugin>
      <plugin>
        <groupId>biz.aQute.bnd</groupId>
        <artifactId>bnd-resolver-maven-plugin</artifactId>
        <version>${bnd.version}</version>
      </plugin>
    </plugins>
  </build>
</project>
```

### Add gauges

Here is an example component that will register two gauges: one computing the system CPU load and one getting the available memory.

This is an OSGi component that provides a `IMetricsMultiGauge` service that the `IMetricsManager` sensiNact service will detect.
To be valid, the service must be associated to an array of gauge names using the property defined in `IMetricsMultiGauge.NAMES` (`sensinact.metrics.multigauge.names`).

```java
package org.eclipse.sensinact.tutorials.metrics.oshi;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.sensinact.core.metrics.IMetricsMultiGauge;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;

/**
 * Oshi-based system metrics
 */
@Component(immediate = true, property = {
  IMetricsMultiGauge.NAMES + "=" + OshiMetrics.CPU + "," + OshiMetrics.MEM_AVAIL })
public class OshiMetrics implements IMetricsMultiGauge {
    /**
     * CPU load gauge name
     */
    static final String CPU = "oshi.cpu";

    /**
     * Available memory
     */
    static final String MEM_AVAIL = "oshi.memory.available";

    /**
     * Previous CPU ticks
     */
    private final AtomicReference<long[]> previousTicks = new AtomicReference<long[]>();

    /**
     * Oshi access to hardware
     */
    private HardwareAbstractionLayer hal;

    /**
     * Oshi access to CPU
     */
    private CentralProcessor cpu;

    /**
     * Component is activated, the metrics service is available
     */
    @Activate
    void activate() {
        // Get access to system information via Oshi
        final SystemInfo si = new SystemInfo();
        hal = si.getHardware();
        cpu = hal.getProcessor();

        // Get the initial CPU load ticks
        previousTicks.set(cpu.getSystemCpuLoadTicks());
    }

    /**
     * The component is deactivated
     */
    @Deactivate
    void deactivate() {
      cpu = null;
      hal = null;
    }

    @Override
    public Object gauge(String name) {
        switch (name) {
        case CPU: {
            // Get the previous ticks and store the new ones
            final long[] oldTicks = previousTicks.getAndSet(cpu.getSystemCpuLoadTicks());
            // Compute the system CPU load compared the previous ticks
            return cpu.getSystemCpuLoadBetweenTicks(oldTicks);
        }

        case MEM_AVAIL:
            return hal.getMemory().getAvailable();

        default:
            throw new RuntimeException("Unknown gauge name: " + name);
        }
    }
}
```

### Use timers

To use sensiNact metrics timers, it is recommended to use the try-with-resources pattern.
The timer is started as soon as it is returned by the metrics service and stopped when its `close()` method is called.

Here is an example usage:
```java
try (IMetricTimer timer = metrics.withTimer("tutorial.metric.name")) {
    executeTask();
}
```

A meta-timer is also available, to have multiple metric names for the same measurement.
```java
try (IMetricTimer timer = metrics.withTimers("tutorial.metric.name", "some.other.metric", "and.another.one")) {
    executeTask();
}
```

Note that the meta-timer is starting and closing a timer per enabled metric (all by default), which might cause a small delay depending on system performances.

Unlike other statistics, you *must not* reuse a timer instance.

### Use other metrics

Counters and histograms can be used inline or be kept as members.
The metric object will only work if overall metrics and the specific metric are both enabled.

Here are some example usage of counters and histograms:
```java
void addSensor() {
    metrics.getCounter("tutorial.metric.sensors.count").inc();
}

void removeSensor() {
    metrics.getCounter("tutorial.metric.sensors.count").dec();
}

void notified(int sensorId, int temperature) {
    metrics.getHistogram("tutorial.metric.temperature").update(temperature);
    metrics.getHistogram("tutorial.metric.temperature." + sensorId).update(temperature);
}
```

Note that histograms only accept long integers as argument of the `update()` method.
This is a limitation from the Metrics library.
