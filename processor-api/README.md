# Handling Processing Errors in Kafka Streams Processor API

This module demonstrates how to use the processing exception handler to manage processing errors in Kafka Streams Processor API.

## Prerequisites

To compile and run this demo, you’ll need:

- Java 21
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

- 1 Kafka broker (KRaft mode)
- 1 Control Center
- 1 Kafka Streams Processing Exception Handler Processor API

## Try It Out

Using Control Center at http://localhost:9021, you can produce `DeliveryBooked` events to the `delivery_booked_topic` topic.

### Example Record

Key:

```json
"DEL12345"
```

Value:

```json
{
  "deliveryId": "DEL12345",
  "truckId": "TRK56789",
  "numberOfTires": 18,
  "destination": "Bordeaux"
}
```

### Triggering an Exception

To trigger the processing exception handler, produce a record with a missing `numberOfTires` field. This will result in a `NullPointerException`:

```json
{
  "deliveryId": "DEL12345",
  "truckId": "TRK56789",
  "destination": "Bordeaux"
}
```
