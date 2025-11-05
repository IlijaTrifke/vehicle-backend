package com.meuhlbauer.vehicle_backend.filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(1)
public class CorrelationIdFilter implements Filter {

    public static final String TRACE_ID = "traceId";
    private static final String TRACE_HEADER = "X-Trace-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            String incoming = null;
            if (request instanceof HttpServletRequest httpReq) {
                incoming = httpReq.getHeader(TRACE_HEADER);
            }
            String traceId = Optional.ofNullable(incoming).orElse(UUID.randomUUID().toString());
            MDC.put(TRACE_ID, traceId);
            if (response instanceof HttpServletResponse httpResp) {
                httpResp.setHeader(TRACE_HEADER, traceId);
            }
            chain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID);
        }
    }
}
