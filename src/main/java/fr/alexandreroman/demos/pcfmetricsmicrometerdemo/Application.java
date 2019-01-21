/*
 * Copyright (c) 2019 Pivotal Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.alexandreroman.demos.pcfmetricsmicrometerdemo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.atomic.AtomicLong;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@Controller
@RequiredArgsConstructor
class HelloController {
    private final AccessLog accessLog;

    @GetMapping("/")
    @ResponseBody
    public String hello() {
        accessLog.incrementCounter();
        return "Hello metrics: " + accessLog.getCounter();
    }
}

/**
 * Business component holding metric values.
 */
@Component
class AccessLog {
    private final AtomicLong counter = new AtomicLong();

    public void incrementCounter() {
        counter.incrementAndGet();
    }

    public long getCounter() {
        return counter.get();
    }

    public void resetCounter() {
        counter.set(0);
    }
}

/**
 * Export metrics to a Spring Boot actuator endpoint.
 */
@Component
@Endpoint(id = "accesslog")
@RequiredArgsConstructor
class AccessLogActuator {
    private final AccessLog accessLog;

    @ReadOperation
    public AccessLogActuatorValues get() {
        return new AccessLogActuatorValues(accessLog.getCounter());
    }

    @DeleteOperation
    public void reset() {
        accessLog.resetCounter();
    }
}

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class AccessLogActuatorValues {
    private final long counter;
}

/**
 * Export metrics to Micrometer.
 */
@Configuration
@RequiredArgsConstructor
class AccessLogMicrometer {
    private final AccessLog accessLog;

    @Bean
    public Gauge accessLogCounter(MeterRegistry registry) {
        return Gauge.builder("accesslog.counter", () -> accessLog.getCounter())
                .tag("kind", "performance")
                .description("Service access count")
                .register(registry);
    }
}
