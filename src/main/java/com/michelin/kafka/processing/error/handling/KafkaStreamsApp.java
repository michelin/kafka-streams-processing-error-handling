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

import static org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.PROCESSING_EXCEPTION_HANDLER_CLASS_CONFIG;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

/**
 * Kafka Streams application.
 */
public class KafkaStreamsApp {

    /**
     * Main method.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(APPLICATION_ID_CONFIG, "error-handling-app");
        properties.put(
            BOOTSTRAP_SERVERS_CONFIG,
            Optional.ofNullable(System.getenv("BOOTSTRAP_SERVERS")).orElse("localhost:9092")
        );
        properties.put(PROCESSING_EXCEPTION_HANDLER_CLASS_CONFIG, CustomProcessingExceptionHandler.class.getName());

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        buildTopology(streamsBuilder);

        KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), properties);

        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));

        kafkaStreams.start();
    }

    /**
     * Build the Kafka Streams topology.
     *
     * @param streamsBuilder The streams builder
     */
    public static void buildTopology(StreamsBuilder streamsBuilder) {
        KStream<String, String> stream = streamsBuilder.stream(
            "INPUT_TOPIC", Consumed.with(Serdes.String(), Serdes.String())
        );

        stream
            // NullPointerException or NumberFormatException
            .filter((key, value) -> Integer.parseInt(value.split(",")[0].trim()) >= 2)
            .mapValues(value -> value.split(",", 2)[1]) // IndexOutOfBoundsException
            .process(CustomProcessor::new) // KaboomException or NullPointerException
            .to("OUTPUT_TOPIC", Produced.with(Serdes.String(), Serdes.String()));
    }

    /**
     * Custom processor.
     */
    public static class CustomProcessor extends ContextualProcessor<String, String, String, String> {
        @Override
        public void init(ProcessorContext<String, String> context) {
            super.init(context);
            context.schedule(
                Duration.ofMinutes(1),
                PunctuationType.WALL_CLOCK_TIME,
                timestamp -> {
                    // Simulate an intentional exception
                    throw new KaboomException("Simulated exception in punctuation");
                }
            );
        }

        @Override
        public void process(Record<String, String> record) {
            Arrays.stream(record.value().split(","))
                .forEach(item -> context().forward(record.withKey(record.key().toUpperCase()).withValue(item.trim())));
        }
    }
}
