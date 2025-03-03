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

package com.michelin.kafka.processing.error.handling;

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
        properties.setProperty(APPLICATION_ID_CONFIG, "error-handling-app-test");
        properties.setProperty(BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        properties.setProperty(
            PROCESSING_EXCEPTION_HANDLER_CLASS_CONFIG,
            CustomProcessingExceptionHandler.class.getName()
        );

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KafkaStreamsApp.buildTopology(streamsBuilder);
        testDriver = new TopologyTestDriver(streamsBuilder.build(), properties);

        inputTopic = testDriver.createInputTopic("INPUT_TOPIC", new StringSerializer(), new StringSerializer());
        outputTopic = testDriver.createOutputTopic("OUTPUT_TOPIC", new StringDeserializer(), new StringDeserializer());
    }

    @AfterEach
    void tearDown() {
        testDriver.close();
    }

    @Test
    void shouldHandleExceptionsAndContinueProcessing() {
        inputTopic.pipeInput("ORD1001", "iPhone 15, AirPods Pro, MagSafe Charger"); // NumberFormatException
        inputTopic.pipeInput("ORD1002", "2, MacBook Air, USB-C Hub");
        inputTopic.pipeInput("ORD1003", (String) null); // NullPointerException
        inputTopic.pipeInput("ORD1004", "3, Samsung Galaxy S24, Wireless Charger, Phone Case");
        inputTopic.pipeInput("ORD1005", "4"); // IndexOutOfBoundsException
        inputTopic.pipeInput("ORD1006", "2, Sony WH-1000XM5 Headphones, Bluetooth Speaker");
        inputTopic.pipeInput(null, "3, Nintendo Switch, Extra Joy-Cons, Game: Zelda TOTK"); // NullPointerException

        testDriver.advanceWallClockTime(Duration.ofMinutes(2)); // KaboomException

        List<KeyValue<String, String>> results = outputTopic.readKeyValuesToList();

        assertEquals(KeyValue.pair("ORD1002", "MacBook Air"), results.getFirst());
        assertEquals(KeyValue.pair("ORD1002", "USB-C Hub"), results.get(1));
        assertEquals(KeyValue.pair("ORD1004", "Samsung Galaxy S24"), results.get(2));
        assertEquals(KeyValue.pair("ORD1004", "Wireless Charger"), results.get(3));
        assertEquals(KeyValue.pair("ORD1004", "Phone Case"), results.get(4));
        assertEquals(KeyValue.pair("ORD1006", "Sony WH-1000XM5 Headphones"), results.get(5));
        assertEquals(KeyValue.pair("ORD1006", "Bluetooth Speaker"), results.get(6));

        assertEquals(5.0, testDriver.metrics().get(droppedRecordsTotalMetric()).metricValue());
        assertEquals(0.03333333333333333, testDriver.metrics().get(droppedRecordsRateMetric()).metricValue());
    }

    private MetricName droppedRecordsTotalMetric() {
        return createMetric("dropped-records-total", "The total number of dropped records");
    }

    private MetricName droppedRecordsRateMetric() {
        return createMetric("dropped-records-rate", "The average number of dropped records per second");
    }

    private MetricName createMetric(String name, String description) {
        return new MetricName(
            name, "stream-task-metrics", description,
            mkMap(
                mkEntry("thread-id", Thread.currentThread().getName()),
                mkEntry("task-id", "0_0")
            )
        );
    }
}
