<div align="center">

<img src=".readme/logo.png" alt="Apache Kafka"/>

# Processing Error Handling

[![GitHub Build](https://img.shields.io/github/actions/workflow/status/michelin/kafka-streams-processing-error-handling/build.yml?branch=main&logo=github&style=for-the-badge)](https://github.com/michelin/kafka-streams-processing-error-handling/actions/workflows/build.yml)
[![Kafka Version](https://img.shields.io/badge/dynamic/xml?url=https%3A%2F%2Fraw.githubusercontent.com%2Fmichelin%2Fkafka-streams-processing-error-handling%2Fmain%2Fpom.xml&query=%2F*%5Blocal-name()%3D'project'%5D%2F*%5Blocal-name()%3D'properties'%5D%2F*%5Blocal-name()%3D'kafka-streams.version'%5D%2Ftext()&style=for-the-badge&logo=apachekafka&label=version)](https://github.com/michelin/kafka-streams-processing-error-handling/blob/main/pom.xml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?logo=apache&style=for-the-badge)](https://opensource.org/licenses/Apache-2.0)

[Prerequisites](#prerequisites) • [Examples](#examples) •  [Current London 2025](#current-london-2025)

Code sample for processing error handling ([KIP-1033](https://cwiki.apache.org/confluence/display/KAFKA/KIP-1033%3A+Add+Kafka+Streams+exception+handler+for+exceptions+occurring+during+processing)).

Available since Apache Kafka 3.9.0, this example showcases the use of the Kafka Streams configuration property `processing.exception.handler` to manage processing exceptions effectively.

</div>

## Prerequisites

- Java 21
- Maven
- Docker

## Examples

- Processing Error Handling with [DSL](/dsl).
- Processing Error Handling with [Processor API](/processor-api).

## Current London 2025

Processing Error Handling (KIP-1033) has been presented at [Current London 2025](https://current.confluent.io/london/agenda). Slides are available [here](.readme/Slides_Processing_Exception_Handling.pptx).
