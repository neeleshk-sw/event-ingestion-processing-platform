package com.platform.common.filter;

import com.platform.common.util.MdcConstants;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MdcClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        propagate(request, MdcConstants.CORRELATION_ID, MdcConstants.HEADER_CORRELATION_ID);
        propagate(request, MdcConstants.TRACE_ID, MdcConstants.HEADER_TRACE_ID);
        propagate(request, MdcConstants.BATCH_ID, MdcConstants.HEADER_BATCH_ID);
        propagate(request, MdcConstants.EVENT_ID, MdcConstants.HEADER_EVENT_ID);
        propagate(request, MdcConstants.RETRY_COUNT, MdcConstants.HEADER_RETRY_COUNT);

        return execution.execute(request, body);
    }

    private void propagate(HttpRequest request, String mdcKey, String headerName) {
        String value = MDC.get(mdcKey);
        if (value != null && !value.isBlank()) {
            request.getHeaders().add(headerName, value);
        }
    }
}
