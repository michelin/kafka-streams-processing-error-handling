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

import com.google.gson.Gson;
import java.time.Duration;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

public class CustomProcessor extends ContextualProcessor<String, String, String, String> {
    private static final Gson gson = new Gson();

    @Override
    public void init(ProcessorContext<String, String> context) {
        super.init(context);
        context.schedule(Duration.ofMinutes(1), PunctuationType.WALL_CLOCK_TIME, timestamp -> {
            // Simulate an intentional exception
            throw new KaboomException("Simulated exception in punctuation");
        });
    }

    @Override
    public void process(Record<String, String> message) {
        DeliveryBooked value = parseFromJson(message.value());

        if (value.getNumberOfTires() < 0) {
            throw new InvalidDeliveryException("Number of tires cannot be negative");
        }

        if (value.getNumberOfTires() >= 10) {
            context().forward(message.withValue(parseToJson(value)));
        }
    }

    private static DeliveryBooked parseFromJson(String value) {
        return gson.fromJson(value, DeliveryBooked.class);
    }

    private static String parseToJson(DeliveryBooked value) {
        return gson.toJson(value);
    }
}
