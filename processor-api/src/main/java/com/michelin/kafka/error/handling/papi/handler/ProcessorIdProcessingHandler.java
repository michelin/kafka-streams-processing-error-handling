package com.michelin.kafka.error.handling.papi.handler;

import java.util.Map;
import org.apache.kafka.streams.errors.ErrorHandlerContext;
import org.apache.kafka.streams.errors.ProcessingExceptionHandler;
import org.apache.kafka.streams.processor.api.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessorIdProcessingHandler implements ProcessingExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ProcessorIdProcessingHandler.class);

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

        if (context.processorNodeId().equals("MAP_PROCESSOR"))
            return ProcessingHandlerResponse.FAIL;
        if (context.processorNodeId().equals("FILTER_PROCESSOR"))
            return ProcessingHandlerResponse.CONTINUE;
        if (context.processorNodeId().equals("SELECT_KEY_PROCESSOR"))
            return ProcessingHandlerResponse.FAIL;

        return ProcessingHandlerResponse.CONTINUE;

    }

    @Override
    public void configure(Map<String, ?> map) {
        // Do nothing
    }
}
