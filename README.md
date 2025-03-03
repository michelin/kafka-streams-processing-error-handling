<div align="center">

<img src=".readme/kafka.png" alt="Apache Kafka"/>

# Processing Error Handling • KIP-1033

[![GitHub Build](https://img.shields.io/github/actions/workflow/status/michelin/processing-error-handling/push_main.yml?branch=main&logo=github&style=for-the-badge)](https://github.com/michelin/processing-error-handling/actions/workflows/push_main.yml)
[![Kafka Version](https://img.shields.io/badge/dynamic/xml?url=https%3A%2F%2Fraw.githubusercontent.com%michelin%2Fprocessing-error-handling%2Fmain%2Fpom.xml&query=%2F*%5Blocal-name()%3D'project'%5D%2F*%5Blocal-name()%3D'properties'%5D%2F*%5Blocal-name()%3D'kafka-streams.version'%5D%2Ftext()&style=for-the-badge&logo=apachekafka&label=version)](https://github.com/michelin/processing-error-handling/blob/main/pom.xml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?logo=apache&style=for-the-badge)](https://opensource.org/licenses/Apache-2.0)

[Prerequisites](#Prerequisites) • [Examples](#examples)

Code sample for [KIP-1033](https://cwiki.apache.org/confluence/display/KAFKA/KIP-1033%3A+Add+Kafka+Streams+exception+handler+for+exceptions+occurring+during+processing).

Available since Apache Kafka 3.9.0, this code sample demonstrates the use of the Kafka Streams configuration `processing.exception.handler` to handle processing exception.

</div>

## Prerequisites

- Java 21
- Maven
- Docker

## Examples

- Processing Error Handling with [DSL](/dsl).
- Processing Error Handling with [Processor API](/processor-api).