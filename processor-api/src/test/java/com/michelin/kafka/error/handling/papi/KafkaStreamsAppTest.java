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
package com.michelin.kafka.error.handling.papi;

import static org.apache.kafka.common.utils.Utils.mkEntry;
import static org.apache.kafka.common.utils.Utils.mkMap;
import static org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.PROCESSING_EXCEPTION_HANDLER_CLASS_CONFIG;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KafkaStreamsAppTest {
    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, String> outputTopic;

    @BeforeEach
    public void setUp() {
        Properties properties = new Properties();
        properties.setProperty(APPLICATION_ID_CONFIG, "processing-error-handling-papi-app");
        properties.setProperty(BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        properties.setProperty(
                PROCESSING_EXCEPTION_HANDLER_CLASS_CONFIG, CustomProcessingExceptionHandler.class.getName());

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KafkaStreamsApp.buildTopology(streamsBuilder);
        testDriver = new TopologyTestDriver(streamsBuilder.build(), properties);

        inputTopic =
                testDriver.createInputTopic("delivery_booked_topic", new StringSerializer(), new StringSerializer());
        outputTopic = testDriver.createOutputTopic(
                "filtered_delivery_booked_papi_topic", new StringDeserializer(), new StringDeserializer());
    }

    @AfterEach
    void tearDown() {
        testDriver.close();
    }

    @Test
    void shouldHandleExceptionsAndContinueProcessing() {
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

        inputTopic.pipeInput(
                "DEL67891",
                """
            {
              "deliveryId": "DEL67891",
              "truckId": "TRK12345",
              "numberOfTires": 7,
              "destination": "Paris"
            }
            """);

        // Json syntax error. This will throw a JsonSyntaxException
        inputTopic.pipeInput(
                "DEL67891",
                """
            {
              "deliveryId": ,
              "truckId": "TRK12345",
              "numberOfTires": 7,
              "destination": "Paris"
            }
            """);

        testDriver.advanceWallClockTime(Duration.ofMinutes(2)); // KaboomException

        List<KeyValue<String, String>> results = outputTopic.readKeyValuesToList();

        assertEquals("DEL12345", results.getFirst().key);

        assertEquals(3.0, testDriver.metrics().get(droppedRecordsTotalMetric()).metricValue());
        assertEquals(
                0.03333333333333333,
                testDriver.metrics().get(droppedRecordsRateMetric()).metricValue());
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
