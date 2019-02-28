# PCF Metrics & Micrometer demo

This project shows how to monitor a Spring Boot app with
[PCF Metrics](https://pivotal.io/platform/services-marketplace/monitoring-metrics-and-logging/pcf-metrics)
and [Micrometer](https://micrometer.io).

## How to use it?

Compile this project using a JDK 8:
```bash
$ ./mvnw clean package
```

Start a Redis server (used to store metric values):
```bash
$ docker run --rm --name redis -p "6379:6379/tcp" redis:5
```

Run this app:
```bash
$ java -jar target/pcf-metrics-micrometer-demo.jar
```

This app is exposing an endpoint at http://localhost:8080:
```bash
$ curl http://localhost:8080
Hello metrics: 1%
```

Each time you access this endpoint, a counter is incremented.
This counter is exposed using a Spring Boot Actuator endpoint, available at http://localhost:8080/actuator/accesslog:
```bash
$ curl http://localhost:8080/actuator/accesslog
{"counter":1}%
```

You can reset this counter by sending a `DELETE` request to this endpoint:
```bash
$ curl -X DELETE http://localhost:8080/actuator/accesslog
$ curl http://localhost:8080/actuator/accesslog
{"counter":0}%
```

Thanks to Micrometer, this app also supports
[Prometheus](https://prometheus.io/) metrics,
without a single line of code.
Just add this dependency to your Spring Boot app to enable
Prometheus support:

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

Prometheus metrics are available at http://localhost:8080/actuator/prometheus:

```bash
$ curl http://localhost:8080/actuator/prometheus
# HELP accesslog_counter Service access count
# TYPE accesslog_counter gauge
accesslog_counter{kind="performance",} 1.0
```

## Run this app in Pivotal Cloud Foundry

Create a Redis instance:
```bash
$ cf create-service p-redis shared-vm metrics-redis
```

Push this app to PCF:
```bash
$ cf push
```

## Expose app metrics

You have two options to expose app metrics:
 - PCF Metrics Forwarder
 - PCF Metric Registrar (new feature from PAS 2.4)

Consider using the latter solution, since your app metrics will be
exposed using a "standard" format (Prometheus).
Using Prometheus for your metrics brings more flexibility when it
comes to platform compatibility.

### Using PCF Metrics Forwarder

Create a [PCF Metrics Forwarder](https://docs.pivotal.io/metrics-forwarder) service instance:
```bash
$ cf create-service metrics-forwarder unlimited metrics-forwarder
```

Bind your app to the metrics forwarder instance, and reload it:
```bash
$ cf bind-service pcf-metrics-micrometer-demo metrics-forwarder
$ cf restage pcf-metrics-micrometer-demo
```

### Using PCF Metric Registrar (PAS 2.4+)

[Metric Registrar](https://docs.pivotal.io/pivotalcf/2-4/metric-registrar/index.html)
is a new feature available in PAS 2.4. When this feature is enabled,
PAS will periodically scrap metrics from your app using a Prometheus
compatible endpoint. You don't need to install anything else when
using Metric Registrar, nor you need to push your metrics to some
central metrics repository: the platform takes care of everything.

Metrics are retrieved by the platform every 35 seconds by default.

You need to install the Metric Registrar CLI plugin in order to
register your app metrics:
```bash
$ cf install-plugin -r CF-Community "metric-registrar"
```

Then, you need to register your Prometheus metrics endpoint to
Metric Registrar:
```bash
$ cf register-metrics-endpoint pcf-metrics-micrometer-demo /actuator/prometheus
```

You're done!

## Use PCF Metrics to view app metrics

This app is now exporting its metrics to PCF Metrics.
You can create a metric chart to show these values on the PCF Metrics Dashboard.
<img src="https://imgur.com/download/S9j99sE"/>

You can also set up a webhook. For example, you can send a message to a Slack channel if the value is too high:
<img src="https://imgur.com/download/e53KA7e"/>

## Contribute

Contributions are always welcome!

Feel free to open issues & send PR.

## License

Copyright &copy; 2019 [Pivotal Software, Inc](https://pivotal.io).

This project is licensed under the [Apache Software License version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
