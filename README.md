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

This app is exposing a single endpoint at http://localhost:8080:
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

## Use PCF Metrics to get app metrics

Create a [PCF Metrics Forwarder](https://docs.pivotal.io/metrics-forwarder) service instance:
```bash
$ cf create-service metrics-forwarder unlimited metrics-forwarder
```

Create a Redis instance:
```bash
$ cf create-service p-redis shared-vm metrics-redis
```

Push this app to PCF:
```bash
$ cf push
```

Bind your app to the metrics forwarder instance, and reload it:
```bash
$ cf bind-service pcf-metrics-micrometer-demo metrics-forwarder
$ cf restage pcf-metrics-micrometer-demo
```

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
