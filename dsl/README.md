# Handling Processing Errors in Kafka Streams DSL

This module demonstrates how to use the processing exception handler to manage processing errors in Kafka Streams DSL operations.

## Prerequisites

To compile and run this demo, you’ll need:

- Java 25
- Maven
- Docker

## Running the Application

To run the application manually:

- Start a [Confluent Platform](https://docs.confluent.io/platform/current/quickstart/ce-docker-quickstart.html#step-1-download-and-start-cp) in a Docker environment.
- Create a topic named `delivery_booked_topic`.
- Start the Kafka Streams application.

To run the application in Docker, use the following command:

```bash
docker-compose up -d
```

This will start the following services in Docker:

- Kafka Broker
- Control Center
- Kafka Streams Processing Exception Handler DSL

## Try It Out

Using the [Kafkagen](https://github.com/michelin/kafkagen) `produce` command, you can produce `DeliveryBooked` events to the `delivery_booked_topic` topic.

```bash
kafkagen produce -f ../.kafkagen/default-record.json
```

To trigger the processing exception handler, produce a record with a missing `numberOfTires` field. This will result in a `NullPointerException`:

```bash
kafkagen produce -f ../.kafkagen/processing-error-record.json
```