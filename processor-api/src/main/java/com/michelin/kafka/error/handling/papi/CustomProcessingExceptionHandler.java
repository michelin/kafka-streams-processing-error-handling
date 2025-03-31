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

import com.google.gson.JsonSyntaxException;
import java.util.Map;
import org.apache.kafka.streams.errors.ErrorHandlerContext;
import org.apache.kafka.streams.errors.ProcessingExceptionHandler;
import org.apache.kafka.streams.processor.api.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomProcessingExceptionHandler implements ProcessingExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(CustomProcessingExceptionHandler.class);

    @Override
    public ProcessingHandlerResponse handle(ErrorHandlerContext context, Record<?, ?> record, Exception exception) {
        log.warn(
                "Exception caught for processorNodeId = {}, topic = {}, partition = {}, offset = {}, key = {}, value = {}",
                context.processorNodeId(),
                context.topic(),
                context.partition(),
                context.offset(),
                record != null ? record.key() : null,
                record != null ? record.value() : null,
                exception);

        return isContinuableException(exception) ? ProcessingHandlerResponse.CONTINUE : ProcessingHandlerResponse.FAIL;
    }

    @Override
    public void configure(Map<String, ?> map) {
        // Do nothing
    }

    private boolean isContinuableException(Exception exception) {
        return exception instanceof JsonSyntaxException
                || exception instanceof NullPointerException
                || exception instanceof KaboomException;
    }
}
