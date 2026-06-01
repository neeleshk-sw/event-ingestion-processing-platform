package com.platform.common.filter;

import com.platform.common.util.MdcConstants;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class MdcLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        putMdc(request, MdcConstants.HEADER_CORRELATION_ID, MdcConstants.CORRELATION_ID);
        putMdc(request, MdcConstants.HEADER_TRACE_ID, MdcConstants.TRACE_ID);
        putMdc(request, MdcConstants.HEADER_BATCH_ID, MdcConstants.BATCH_ID);
        putMdc(request, MdcConstants.HEADER_EVENT_ID, MdcConstants.EVENT_ID);
        putMdc(request, MdcConstants.HEADER_RETRY_COUNT, MdcConstants.RETRY_COUNT);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private void putMdc(HttpServletRequest request, String header, String mdcKey) {
        String value = request.getHeader(header);
        if (value != null && !value.isBlank()) {
            MDC.put(mdcKey, value);
        }
    }
}
