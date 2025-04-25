package com.michelin.kafka.error.handling.dsl.handler;

import com.michelin.kafka.error.handling.dsl.DeliveryBooked;
import java.util.Map;
import org.apache.kafka.streams.errors.ErrorHandlerContext;
import org.apache.kafka.streams.errors.ProcessingExceptionHandler;
import org.apache.kafka.streams.processor.api.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecordTypeProcessingHandler implements ProcessingExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(RecordTypeProcessingHandler.class);

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

        if (record != null && record.value() instanceof DeliveryBooked deliveryBooked) {
            return deliveryBooked.getNumberOfTires() == null ? ProcessingHandlerResponse.CONTINUE : ProcessingHandlerResponse.FAIL;
        }

        return ProcessingHandlerResponse.CONTINUE;
    }

    @Override
    public void configure(Map<String, ?> map) {
        // Do nothing
    }
}
