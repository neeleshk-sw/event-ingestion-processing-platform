package com.platform.common.filter;

import com.platform.common.util.MdcConstants;
import org.slf4j.MDC;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.stereotype.Component;

@Component
public class MdcMessagePostProcessor implements MessagePostProcessor {

    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        // Propagation: MDC -> Headers
        propagateToHeader(message, MdcConstants.CORRELATION_ID, MdcConstants.HEADER_CORRELATION_ID);
        propagateToHeader(message, MdcConstants.TRACE_ID, MdcConstants.HEADER_TRACE_ID);
        propagateToHeader(message, MdcConstants.BATCH_ID, MdcConstants.HEADER_BATCH_ID);
        propagateToHeader(message, MdcConstants.EVENT_ID, MdcConstants.HEADER_EVENT_ID);
        propagateToHeader(message, MdcConstants.RETRY_COUNT, MdcConstants.HEADER_RETRY_COUNT);

        // Extraction: Headers -> MDC (for consumers)
        extractToMdc(message, MdcConstants.HEADER_CORRELATION_ID, MdcConstants.CORRELATION_ID);
        extractToMdc(message, MdcConstants.HEADER_TRACE_ID, MdcConstants.TRACE_ID);
        extractToMdc(message, MdcConstants.HEADER_BATCH_ID, MdcConstants.BATCH_ID);
        extractToMdc(message, MdcConstants.HEADER_EVENT_ID, MdcConstants.EVENT_ID);
        extractToMdc(message, MdcConstants.HEADER_RETRY_COUNT, MdcConstants.RETRY_COUNT);

        return message;
    }

    private void propagateToHeader(Message message, String mdcKey, String headerName) {
        String value = MDC.get(mdcKey);
        if (value != null && !value.isBlank()) {
            message.getMessageProperties().setHeader(headerName, value);
        }
    }

    private void extractToMdc(Message message, String headerName, String mdcKey) {
        Object value = message.getMessageProperties().getHeaders().get(headerName);
        if (value != null) {
            MDC.put(mdcKey, value.toString());
        }
    }
}
