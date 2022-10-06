# eventuate-tram-example
## Eventuate Tram 消息发送流程
在使用Eventuate Tram的时候,最好是要了解一下整个消息的流转过程:
>* 1.业务将数据处理完毕
>* 2.发送端将消息发送处理
>  * 2.1在发送消息之前,会对消息进行校验和添加数据: 获取事件名称、设置消息头
  (partition_id,aggregate_id,aggregate_type,event_type)、payload
>  * 2.2 判断是使用数据自动成功的主键、还是使用应用程序的id作为主键
>  * 2.3 将数据插入到eventuate.message表中
>* 3.cdc(capture the data change) 服务启动之后,会使用select的方式,获取到eventuate.message表里面的最新数据,然后将数据写入到Kafka中。在获取变更数据的时候,使用的策略为:MySQL binlog、Postgresql WAL、事务发件箱。
>* 4.消费程序启动之后,消费者从指定Kafka Topic 中获取到数据,然后处理数据

##  项目代码实例
### pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.edu.eventuate</groupId>
    <artifactId>eventuate-tram-example</artifactId>
    <version>1.0-SNAPSHOT</version>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.2.6.RELEASE</version>
    </parent>
    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
        </dependency>
        <dependency>
            <groupId>io.eventuate.tram.core</groupId>
            <artifactId>eventuate-tram-spring-events</artifactId>
            <version>0.30.0.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>io.eventuate.tram.core</groupId>
            <artifactId>eventuate-tram-spring-jdbc-kafka</artifactId>
            <version>0.30.0.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```
### 创建相应的Bean、以及相应Event
```java
package com.edu.eventuate.model;
import com.edu.eventuate.event.CustomerAddEvent;
import io.eventuate.tram.events.publisher.ResultWithEvents;
import javax.persistence.*;
import static java.util.Collections.singletonList;
@Entity
@Table(name = "customer")
@Access(AccessType.FIELD)
public class Customer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String email;
	private String country;
	public Customer() {
	}
	public Customer(String name, String email, String country) {
		super();
		this.name = name;
		this.email = email;
		this.country = country;
	}
	public static ResultWithEvents<Customer> add(String name, String email, String country) {
		Customer customer = new Customer(name, email, country);
		return new ResultWithEvents<>(customer,
				singletonList(new CustomerAddEvent(customer.getName(), customer.getEmail(), customer.getCountry())));
	}
	public Long getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String getEmail() {
		return email;
	}
	public String getCountry() {
		return country;
	}
}
```
```java
import org.springframework.data.repository.CrudRepository;
public interface CustomerRepository extends CrudRepository<Customer, Long> {
}
```
```java
package com.edu.eventuate.model;
public class CustomerRequest {
	private String name;
	private String email;
	private String country;
	public CustomerRequest() {
	}
	public CustomerRequest(String name, String email, String country) {
		super();
		this.name = name;
		this.email = email;
		this.country = country;
	}
	public String getEmail() {
		return email;
	}
	public String getCountry() {
		return country;
	}
	public String getName() {
		return name;
	}
}
```
```java
package com.edu.eventuate.model;
public class CustomerResponse {
  private Long customerId;
  public CustomerResponse() {
  }
  public CustomerResponse(Long customerId) {
    this.customerId = customerId;
  }
  public Long getCustomerId() {
    return customerId;
  }
  public void setCustomerId(Long customerId) {
    this.customerId = customerId;
  }
}
```
### 创建相应事件Event
```java
package com.edu.eventuate.event;
import io.eventuate.tram.events.common.DomainEvent;
public interface CustomerEvent extends DomainEvent {
}
```
```java
package com.edu.eventuate.event;
public class CustomerAddEvent implements CustomerEvent {
	private String name;
	private String email;
	private String country;
	public CustomerAddEvent() {
	}
	public String getName() {
		return name;
	}
	public CustomerAddEvent(String name, String email, String country) {
		super();
		this.name = name;
		this.email = email;
		this.country = country;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
}
```
### Service相关代码
```java
package com.edu.eventuate.service;

import com.edu.eventuate.model.Customer;
import com.edu.eventuate.model.CustomerRepository;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.events.publisher.ResultWithEvents;
import org.springframework.beans.factory.annotation.Autowired;
public class CustomerService {
  @Autowired
  private CustomerRepository customerRepository;
  @Autowired
  private DomainEventPublisher domainEventPublisher;
  public Customer addCustomer(String name, String email, String country) {
    ResultWithEvents<Customer> customerWithAddEvents = Customer.add(name, email, country);
    Customer customer = customerRepository.save(customerWithAddEvents.result);
    domainEventPublisher.publish(Customer.class, customer.getId(), customerWithAddEvents.events);
    return customer;
  }
}
```
### 消费者代码
```java
package com.edu.eventuate.event;
import io.eventuate.tram.events.subscriber.DomainEventEnvelope;
import io.eventuate.tram.events.subscriber.DomainEventHandlers;
import io.eventuate.tram.events.subscriber.DomainEventHandlersBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
@Component
@Slf4j
public class CustomerEventConsumer {
    public DomainEventHandlers domainEventHandlers() {
        return DomainEventHandlersBuilder
                .forAggregateType("com.edu.eventuate.model.Customer")
                .onEvent(CustomerAddEvent.class, this::createCustomer)
                .build();
    }
    private void createCustomer(DomainEventEnvelope<CustomerAddEvent> de) {
        String restaurantIds = de.getAggregateId();
        long id = Long.parseLong(restaurantIds);
        log.info("create customer {} successfully!!!", de.getEvent().getName());
    }
}
```
### 配置类
```java
package com.edu.eventuate;
import com.edu.eventuate.event.CustomerEventConsumer;
import com.edu.eventuate.service.CustomerService;
import io.eventuate.tram.events.subscriber.DomainEventDispatcher;
import io.eventuate.tram.events.subscriber.DomainEventDispatcherFactory;
import io.eventuate.tram.spring.events.publisher.TramEventsPublisherConfiguration;
import io.eventuate.tram.spring.events.subscriber.TramEventSubscriberConfiguration;
import io.eventuate.tram.spring.jdbckafka.TramJdbcKafkaConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
@Configuration
@Import({TramJdbcKafkaConfiguration.class, TramEventsPublisherConfiguration.class,
        TramEventSubscriberConfiguration.class})
@EnableJpaRepositories
@EnableAutoConfiguration
public class CustomerServiceConfiguration {
    @Bean
    public CustomerService customerService() {
        return new CustomerService();
    }
    @Bean
    public DomainEventDispatcher domainEventDispatcher(CustomerEventConsumer customerEventConsumer, DomainEventDispatcherFactory domainEventDispatcherFactory) {
        return domainEventDispatcherFactory.make("customerServiceEvents", customerEventConsumer.domainEventHandlers());
    }
}
```
### controller类
```java
package com.edu.eventuate.controller;
import com.edu.eventuate.model.Customer;
import com.edu.eventuate.model.CustomerRequest;
import com.edu.eventuate.model.CustomerResponse;
import com.edu.eventuate.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class CustomerController {
    @Autowired
    private CustomerService customerService;
    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }
    @RequestMapping(value = "/customer_service/add_customer", method = RequestMethod.POST)
    public CustomerResponse createCustomer(@RequestBody CustomerRequest createCustomerRequest) {
        Customer customer = customerService.addCustomer(createCustomerRequest.getName(),
                createCustomerRequest.getEmail(), createCustomerRequest.getCountry());
        return new CustomerResponse(customer.getId());
    }
}
```
### 启动类
```java
package com.edu.ftgo;

import com.edu.ftgo.config.CustomerServiceConfiguration;
import io.eventuate.tram.spring.events.publisher.TramEventsPublisherConfiguration;
import io.eventuate.tram.spring.events.subscriber.TramEventSubscriberConfiguration;
import io.eventuate.tram.spring.jdbckafka.TramJdbcKafkaConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
@EnableAutoConfiguration
@Import({CustomerServiceConfiguration.class, TramJdbcKafkaConfiguration.class, TramEventsPublisherConfiguration.class,
        TramEventSubscriberConfiguration.class})
public class CustomerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }
}

```
### yml文件配置
```yaml
logging:
  level:
    io.eventuate: DEBUG
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/eventuate
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: update
eventuatelocal:
  kafka:
    bootstrap:
      servers: localhost:9092
  zookeeper:
    connection:
      string: localhost:2181
server:
  port: 8085
```

## 启动CDC服务
编写配置文件
```yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/eventuate
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
    driver.class.name: com.mysql.cj.jdbc.Driver
  profiles:
    active: EventuatePolling
eventuatelocal:
  kafka:
    bootstrap:
      servers: localhost:9092
  zookeeper:
    connection:
      string: localhost:2181
  cdc:
    mysql:
      binlog:
        client:
          unique:
            id: 1
    read:
      old:
        debezium:
          db:
            offset:
              storage:
                topic: false
    reader:
      name: customcdcreader
    db:
      user:
        name: root
      password: 123456
    source:
      table:
        name: message
    leadership:
      lock:
        path: /eventuatelocal/cdc/leader/1
eventuate:
  database:
    schema: eventuate
  cdc:
    type: EventuateTram
  outbox:
    id: 1
```
### 创建数据库相应的代码
```sql
create database eventuate;
GRANT ALL PRIVILEGES ON eventuate.* TO 'root'@'%' WITH GRANT OPTION;
DROP table IF EXISTS events;
DROP table IF EXISTS entities;
DROP table IF EXISTS snapshots;
DROP table IF EXISTS message;
DROP table IF EXISTS cdc_monitoring;
create table events (
event_id varchar(255) PRIMARY KEY,
event_type varchar(255),
event_data varchar(255) NOT NULL,
entity_type VARCHAR(255) NOT NULL,
entity_id VARCHAR(255) NOT NULL,
triggering_event VARCHAR(255),
metadata VARCHAR(255),
published TINYINT DEFAULT 0
);
CREATE INDEX events_idx ON events(entity_type, entity_id, event_id);
CREATE INDEX events_published_idx ON events(published, event_id);
create table entities (
entity_type VARCHAR(255),
entity_id VARCHAR(255),
entity_version VARCHAR(255) NOT NULL,
PRIMARY KEY(entity_type, entity_id)
);
CREATE INDEX entities_idx ON events(entity_type, entity_id);
create table snapshots (
entity_type VARCHAR(255),
entity_id VARCHAR(255),
entity_version VARCHAR(255),
snapshot_type VARCHAR(255) NOT NULL,
snapshot_json VARCHAR(255) NOT NULL,
triggering_events VARCHAR(255),
PRIMARY KEY(entity_type, entity_id, entity_version)
);
CREATE TABLE message (
id varchar(767) NOT NULL,
destination varchar(255) NOT NULL,
headers varchar(255) NOT NULL,
payload varchar(255) NOT NULL,
published smallint(6) DEFAULT '0',
creation_time bigint(20) DEFAULT NULL,
PRIMARY KEY (id),
KEY message_published_idx (published,id)
);
create table received_messages (
consumer_id varchar(255) NOT NULL,
message_id varchar(255) NOT NULL,
creation_time bigint(20) DEFAULT NULL,
PRIMARY KEY (consumer_id, message_id)
);
create table cdc_monitoring (
reader_id VARCHAR(255) PRIMARY KEY,
last_time BIGINT
);
CREATE TABLE offset_store(client_name VARCHAR(255) NOT NULL PRIMARY KEY, serialized_offset VARCHAR(255));
ALTER TABLE received_messages MODIFY creation_time BIGINT;
```
需要的jar包下载路径:
https://search.maven.org/artifact/io.eventuate.cdc/eventuate-cdc-service/0.13.0.RELEASE/jar
启动命令为:
java -jar eventuate-cdc-service-0.13.0.RELEASE.jar --spring.config.location="application.yml"

本文中只体现了: eventuate.cdc.type: EventuateTram,没有使用eventuate.cdc.type: EventuateLocal,因为一直在报错，以后调整不出错的情况下,在使用eventuate.cdc.type: EventuateLocal来展示。

本地关于数据表的sql:
```sql
CREATE TABLE `cdc_monitoring` (
  `reader_id` varchar(255) NOT NULL,
  `last_time` bigint DEFAULT NULL,
  PRIMARY KEY (`reader_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `entities` (
  `entity_type` varchar(255) NOT NULL,
  `entity_id` varchar(255) NOT NULL,
  `entity_version` longtext NOT NULL,
  PRIMARY KEY (`entity_type`,`entity_id`),
  KEY `entities_idx` (`entity_type`,`entity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `events` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `event_id` varchar(255) DEFAULT NULL,
  `event_type` longtext,
  `event_data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `entity_type` varchar(255) NOT NULL,
  `entity_id` varchar(255) NOT NULL,
  `triggering_event` longtext,
  `metadata` longtext,
  `published` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `events_idx` (`entity_type`,`entity_id`,`id`),
  KEY `events_published_idx` (`published`,`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1665028587327 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `message` (
  `id` varchar(255) NOT NULL,
  `destination` longtext NOT NULL,
  `headers` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `payload` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `published` smallint DEFAULT '0',
  `creation_time` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `message_published_idx` (`published`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `offset_store` (
  `client_name` varchar(255) NOT NULL,
  `serialized_offset` longtext,
  PRIMARY KEY (`client_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `received_messages` (
  `consumer_id` varchar(255) NOT NULL,
  `message_id` varchar(255) NOT NULL,
  `creation_time` bigint DEFAULT NULL,
  `published` smallint DEFAULT '0',
  PRIMARY KEY (`consumer_id`,`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `snapshots` (
  `entity_type` varchar(255) NOT NULL,
  `entity_id` varchar(255) NOT NULL,
  `entity_version` varchar(255) NOT NULL,
  `snapshot_type` longtext NOT NULL,
  `snapshot_json` longtext NOT NULL,
  `triggering_events` longtext,
  PRIMARY KEY (`entity_type`,`entity_id`,`entity_version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

## Command 样例
### 添加依赖
```pom
<dependency>
            <groupId>io.eventuate.tram.core</groupId>
            <artifactId>eventuate-tram-spring-commands</artifactId>
            <version>0.30.0.RELEASE</version>
</dependency>
```
### 创建command类
```java
import io.eventuate.tram.commands.common.Command;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class QueryWeatherCommand implements Command {
  @NonNull
  private String city;
}
```
辅助类
```java
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class QueryWeatherResult {
  @NonNull
  private String city;
  @NonNull
  private String result;
}
```
### 处理器handler
```java
import com.edu.ftgo.command.QueryWeatherCommand;
import com.edu.ftgo.domain.QueryWeatherResult;
import io.eventuate.tram.commands.consumer.CommandHandlers;
import io.eventuate.tram.commands.consumer.CommandHandlersBuilder;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.commands.consumer.PathVariables;
import io.eventuate.tram.messaging.common.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import static io.eventuate.tram.commands.consumer.CommandHandlerReplyBuilder.withSuccess;
@Component
@Slf4j
public class WeatherCommandHandlers {
    public CommandHandlers commandHandlers() {
        return CommandHandlersBuilder.fromChannel("weather")
                .onMessage(QueryWeatherCommand.class, this::queryWeather)
                .build();
    }
    private Message queryWeather(CommandMessage<QueryWeatherCommand> cm,
                                 PathVariables pathVariables) {
        log.info("cm result:" + cm.getCommand() + "!!!!!!!!!");
        return withSuccess(
                new QueryWeatherResult(cm.getCommand().getCity(), "Rain"));
    }
}
```
### Command 配置文件
```
import com.edu.ftgo.commandhandler.WeatherCommandHandlers;
import io.eventuate.tram.commands.consumer.CommandDispatcher;
import io.eventuate.tram.commands.consumer.CommandDispatcherFactory;
import io.eventuate.tram.spring.commands.consumer.TramCommandConsumerConfiguration;
import io.eventuate.tram.spring.commands.producer.TramCommandProducerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
@Configuration
@Import({TramCommandProducerConfiguration.class,
    TramCommandConsumerConfiguration.class})
public class WeatherConfiguration {
  @Bean
  public CommandDispatcher weatherCommandDispatcher(
      CommandDispatcherFactory commandDispatcherFactory,
      WeatherCommandHandlers weatherCommandHandlers) {
    return commandDispatcherFactory
        .make("weatherCommandDispatcher",
            weatherCommandHandlers.commandHandlers());
  }
}
```
### Controller
由于没有具体的业务，直接使用基础类来处理数据。
```java
import com.edu.ftgo.command.QueryWeatherCommand;
import com.edu.ftgo.domain.QueryWeatherResult;
import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Collections;
import java.util.HashMap;
@RestController
@Slf4j
public class CommandController {
    @Autowired
    CommandProducer commandProducer;
    @Autowired
    MessageConsumer messageConsumer;
    @GetMapping("/weather")
    public void weatherCommand() {
        messageConsumer
                .subscribe("weather-subscriber", Collections.singleton("weather-reply"),
                        message -> {
                            QueryWeatherResult result = JSonMapper
                                    .fromJson(message.getPayload(), QueryWeatherResult.class);
                            log.info("consumer result:" + result.getResult() + "!!!!");
                        });

        commandProducer
                .send("weather", new QueryWeatherCommand("Beijing"), "weather-reply",
                        new HashMap<>());
    }
}
```


## 参考文献
[How to setup Eventuate-tram-cdc Service locally](https://medium.com/@erandika_harshani/how-to-setup-eventuate-tram-framework-locally-b27f5ba36b87)
[search.maven.org](https://search.maven.org/)
[Configuring the Eventuate CDC Service](https://eventuate.io/docs/manual/eventuate-tram/latest/cdc-configuration.html)
[Pattern: Transaction log tailing](https://microservices.io/patterns/data/transaction-log-tailing.html)
[Pattern: Polling publisher](https://microservices.io/patterns/data/polling-publisher.html)
[Getting started with Eventuate Tram](https://eventuate.io/docs/manual/eventuate-tram/latest/getting-started-eventuate-tram.html#getting-started)
[microservices-patterns.github](https://github.com/wuyichen24/microservices-patterns)
