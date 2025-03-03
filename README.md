<div align="center">

<img src=".readme/kafka.png" alt="Apache Kafka"/>

# Processing Error Handling • KIP-1033

[![GitHub Build](https://img.shields.io/github/actions/workflow/status/michelin/processing-error-handling/push_main.yml?branch=main&logo=github&style=for-the-badge)](https://github.com/michelin/processing-error-handling/actions/workflows/push_main.yml)
[![Kafka Version](https://img.shields.io/badge/dynamic/xml?url=https%3A%2F%2Fraw.githubusercontent.com%michelin%2Fprocessing-error-handling%2Fmain%2Fpom.xml&query=%2F*%5Blocal-name()%3D'project'%5D%2F*%5Blocal-name()%3D'properties'%5D%2F*%5Blocal-name()%3D'kafka-streams.version'%5D%2Ftext()&style=for-the-badge&logo=apachekafka&label=version)](https://github.com/michelin/processing-error-handling/blob/main/pom.xml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?logo=apache&style=for-the-badge)](https://opensource.org/licenses/Apache-2.0)

[Requirements](#requirements) • [Running the Application](#running-the-application)

Code sample around [KIP-1033](https://cwiki.apache.org/confluence/display/KAFKA/KIP-1033%3A+Add+Processing+ExceptionHandler+to+Kafka+Streams) and the processing exception handler.

Demonstrates the use of the Kafka Streams configuration `processing.exception.handler` to handle processing exceptions that occur in DSL operations and the processor API.

</div>

## Requirements

- Java 21
- Maven
- Docker

## Running the Application

To run the application manually, follow the steps below:

- Start a [Confluent Platform](https://docs.confluent.io/platform/current/quickstart/ce-docker-quickstart.html#step-1-download-and-start-cp) in a Docker environment.
- Produce records of type `<String, String>` to a topic named `INPUT_TOPIC`. The records represent orders, with the order number as the key and a list of items (prefixed by the quantity) as the value.

```bash
docker exec -it broker bash

kafka-console-producer --bootstrap-server localhost:9092 --topic INPUT_TOPIC --property parse.key=true --property key.separator=,
> ORD1001, 2, MacBook Air, USB-C Hub
> ORD1002, 3, Samsung Galaxy S24, Wireless Charger, Phone Case
> ORD1003, 2, Sony WH-1000XM5 Headphones, Bluetooth Speaker
```

You can trigger processing exceptions by producing a record with an incorrect format:

```bash
kafka-console-producer --bootstrap-server localhost:9092 --topic INPUT_TOPIC --property parse.key=true --property key.separator=,
> ORD1001, MacBook Air, USB-C Hub # Missing quantity
> ORD1002, 3 # Missing items
```

- Start the Kafka Streams.

To run the application in Docker, use the following command:

```bash
docker-compose up -d
```

This command will start the following services in Docker:

- 1 Kafka broker KRaft
- 1 Control Center
- 1 Kafka Streams Processing Exception Handler