/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.michelin.kafka.error.handling.dsl;

import static org.apache.kafka.common.utils.Utils.mkEntry;
import static org.apache.kafka.common.utils.Utils.mkMap;
import static org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.PROCESSING_EXCEPTION_HANDLER_CLASS_CONFIG;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.michelin.kafka.error.handling.dsl.handler.ExceptionTypeProcessingHandler;
import com.michelin.kafka.error.handling.dsl.handler.ProcessorIdProcessingHandler;
import com.michelin.kafka.error.handling.dsl.handler.RecordTypeProcessingHandler;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class KafkaStreamsAppTest {
    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, String> outputTopic;

    @AfterEach
    void tearDown() {
        testDriver.close();
    }

    static Stream<String> provideProcessingExceptionHandlerClassName() {
        return Stream.of(
                ExceptionTypeProcessingHandler.class.getName(),
                ProcessorIdProcessingHandler.class.getName(),
                RecordTypeProcessingHandler.class.getName());
    }

    @ParameterizedTest
    @MethodSource("provideProcessingExceptionHandlerClassName")
    void shouldContinueOnInvalidDeliveryAndNullPointerExceptions(String processingExceptionHandlerClassName) {
        instantiateTopologyTestDriver(processingExceptionHandlerClassName);

        inputTopic.pipeInput(
                "DEL12345",
                """
        {
          "deliveryId": "DEL12345",
          "truckId": "TRK56789",
          "numberOfTires": 18,
          "destination": "Bordeaux"
        }
        """);

        // "numberOfTires" is negative. This will throw an InvalidDeliveryException
        inputTopic.pipeInput(
                "DEL73148",
                """
        {
          "deliveryId": "DEL67145",
          "truckId": "TRK34567",
          "numberOfTires": -1,
          "destination": "Marseille"
        }
        """);

        // "numberOfTires" is missing. This will throw a NullPointerException
        inputTopic.pipeInput(
                "DEL73148",
                """
        {
          "deliveryId": "DEL73148",
          "truckId": "TRK48612",
          "destination": "Lyon"
        }
        """);

        List<KeyValue<String, String>> results = outputTopic.readKeyValuesToList();

        assertEquals("DEL12345", results.getFirst().key);

        assertEquals(2.0, testDriver.metrics().get(droppedRecordsTotalMetric()).metricValue());
        assertEquals(
                0.06666666666666667,
                testDriver.metrics().get(droppedRecordsRateMetric()).metricValue());
    }

    void instantiateTopologyTestDriver(String processingExceptionHandlerClassName) {
        Properties properties = new Properties();
        properties.setProperty(APPLICATION_ID_CONFIG, "processing-error-handling-dsl-app-test");
        properties.setProperty(BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        properties.setProperty(PROCESSING_EXCEPTION_HANDLER_CLASS_CONFIG, processingExceptionHandlerClassName);

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KafkaStreamsApp.buildTopology(streamsBuilder);
        testDriver = new TopologyTestDriver(streamsBuilder.build(), properties);

        inputTopic =
                testDriver.createInputTopic("delivery_booked_topic", new StringSerializer(), new StringSerializer());
        outputTopic = testDriver.createOutputTopic(
                "filtered_delivery_booked_dsl_topic", new StringDeserializer(), new StringDeserializer());
    }

    private MetricName droppedRecordsTotalMetric() {
        return createMetric("dropped-records-total", "The total number of dropped records");
    }

    private MetricName droppedRecordsRateMetric() {
        return createMetric("dropped-records-rate", "The average number of dropped records per second");
    }

    private MetricName createMetric(String name, String description) {
        return new MetricName(
                name,
                "stream-task-metrics",
                description,
                mkMap(mkEntry("thread-id", Thread.currentThread().getName()), mkEntry("task-id", "0_0")));
    }
}
