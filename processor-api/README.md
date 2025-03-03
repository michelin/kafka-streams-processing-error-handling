# Processing Error Handling with Processor API

This module demonstrates how the processing exception handler can be used to handle processing exceptions in Kafka Streams Processor API.

## Run

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

## Try it

From Control Center running on http://localhost:9021, you can produce `DeliveryBooked` events to the topic `delivery_booked_topic`.

Key:

```
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

To trigger the processing exception handler, you can produce a record with an incorrect format:

Value with missing `numberOfTires`, triggering a `NullPointerException`:

```json
{
  "deliveryId": "DEL12345",
  "truckId": "TRK56789",
  "destination": "Bordeaux"
}
```