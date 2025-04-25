package com.michelin.kafka.error.handling.dsl.handler;

import com.michelin.kafka.error.handling.dsl.DeliveryBooked;
import java.util.Map;
import org.apache.kafka.streams.errors.ErrorHandlerContext;
import org.apache.kafka.streams.errors.ProcessingExceptionHandler;
import org.apache.kafka.streams.processor.api.Record;

public class RecordTypeProcessingHandler implements ProcessingExceptionHandler {
    @Override
    public ProcessingHandlerResponse handle(ErrorHandlerContext context, Record<?, ?> record, Exception exception) {
        if (record.value() instanceof DeliveryBooked deliveryBooked) {
            return deliveryBooked.getNumberOfTires() == null ? ProcessingHandlerResponse.CONTINUE : ProcessingHandlerResponse.FAIL;
        }

        return ProcessingHandlerResponse.CONTINUE;

    }

    @Override
    public void configure(Map<String, ?> map) {

    }
}
