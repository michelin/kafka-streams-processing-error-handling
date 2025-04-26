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

import static org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.PROCESSING_EXCEPTION_HANDLER_CLASS_CONFIG;

import com.google.gson.Gson;
import com.michelin.kafka.error.handling.dsl.handler.ExceptionTypeProcessingHandler;
import java.util.Optional;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;

public class KafkaStreamsApp {
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(APPLICATION_ID_CONFIG, "processing-error-handling-dsl-app");
        properties.put(
                BOOTSTRAP_SERVERS_CONFIG,
                Optional.ofNullable(System.getenv("BOOTSTRAP_SERVERS")).orElse("localhost:9092"));
        properties.put(PROCESSING_EXCEPTION_HANDLER_CLASS_CONFIG, ExceptionTypeProcessingHandler.class.getName());

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        buildTopology(streamsBuilder);

        KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), properties);
        kafkaStreams.setUncaughtExceptionHandler(
                exception -> StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_CLIENT);
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));

        kafkaStreams.start();
    }

    public static void buildTopology(StreamsBuilder streamsBuilder) {
        streamsBuilder.stream("delivery_booked_topic", Consumed.with(Serdes.String(), Serdes.String()))
                .mapValues(KafkaStreamsApp::parseFromJson) // JsonSyntaxException
                .filter((key, value) -> {
                    if (value.getNumberOfTires() < 0) {
                        throw new InvalidDeliveryException("Number of tires cannot be negative");
                    }

                    return value.getNumberOfTires() >= 10;
                }) // InvalidDeliveryException or NullPointerException
                .mapValues(KafkaStreamsApp::parseToJson)
                .to("filtered_delivery_booked_dsl_topic", Produced.with(Serdes.String(), Serdes.String()));
    }

    private static DeliveryBooked parseFromJson(String value) {
        return gson.fromJson(value, DeliveryBooked.class);
    }

    private static String parseToJson(DeliveryBooked value) {
        return gson.toJson(value);
    }
}
