package com.meuhlbauer.vehicle_backend.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static String currentTraceId() {
        return MDC.get("traceId");
    }

    private static String requestUri(WebRequest request) {
        if (request instanceof ServletWebRequest swr) {
            return swr.getRequest().getRequestURI();
        }
        return "/";
    }

    private static ProblemDetail baseProblem(HttpStatus status, String detail, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setTitle(status.getReasonPhrase());
        if (detail != null) {
            pd.setDetail(detail);
        }
        pd.setInstance(URI.create(requestUri(request)));
        pd.setType(URI.create("https://api.example.com/problems/" + status.value()));
        pd.setProperty("traceId", Optional.ofNullable(currentTraceId()).orElse("-"));
        return pd;
    }

    // 400 - JSON parse / type mismatch
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(@NonNull HttpMessageNotReadableException ex,
                                                                  @NonNull org.springframework.http.HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull org.springframework.web.context.request.WebRequest request) {
        ProblemDetail pd = baseProblem(HttpStatus.BAD_REQUEST, "Malformed JSON or invalid types", request);
        pd.setProperty("code", ErrorCode.BAD_REQUEST.name());
        log.warn("400 Bad Request (not readable) traceId={}", currentTraceId());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    // 400 - @Valid on @RequestBody (field errors)
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull org.springframework.http.HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull org.springframework.web.context.request.WebRequest request) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        DefaultMessageSourceResolvable::getDefaultMessage,
                        (a, b) -> a,
                        LinkedHashMap::new));
        ProblemDetail pd = baseProblem(HttpStatus.BAD_REQUEST, "Validation failed", request);
        pd.setProperty("code", ErrorCode.VALIDATION_ERROR.name());
        pd.setProperty("errors", fieldErrors);
        log.warn("400 Validation failed traceId={}", currentTraceId());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    // 400 - @Validated on params/path/query (method-level) - Jakarta validation
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolations(ConstraintViolationException ex,
                                                                    WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            errors.put(v.getPropertyPath().toString(), v.getMessage());
        }
        ProblemDetail pd = baseProblem(HttpStatus.BAD_REQUEST, "Validation failed", request);
        pd.setProperty("code", ErrorCode.VALIDATION_ERROR.name());
        pd.setProperty("errors", errors);
        log.warn("400 Constraint violation traceId={}", currentTraceId());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    // 400 - Method-level validation (Spring 6 HandlerMethodValidationException)
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            @NonNull HandlerMethodValidationException ex,
            @NonNull org.springframework.http.HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull org.springframework.web.context.request.WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        int i = 0;
        for (var err : ex.getAllErrors()) {
            String[] codes = err.getCodes();
            String key = (codes != null && codes.length > 0) ? codes[0] : ("parameter[" + i + "]");
            String msg = err.getDefaultMessage();
            errors.put(key, msg != null ? msg : "Invalid parameter");
            i++;
        }
        ProblemDetail pd = baseProblem(HttpStatus.BAD_REQUEST, "Validation failed", request);
        pd.setProperty("code", ErrorCode.VALIDATION_ERROR.name());
        pd.setProperty("errors", errors);
        log.warn("400 Handler method validation traceId={}", currentTraceId());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    // 404 - domain not found
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(NotFoundException ex, WebRequest request) {
        ProblemDetail pd = baseProblem(HttpStatus.NOT_FOUND,
                ex.getMessage() != null ? ex.getMessage() : "Resource not found", request);
        pd.setProperty("code", ErrorCode.NOT_FOUND.name());
        if (ex.getResource() != null)
            pd.setProperty("resource", ex.getResource());
        if (ex.getResourceId() != null)
            pd.setProperty("resourceId", ex.getResourceId());
        log.warn("404 Not Found traceId={}", currentTraceId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }

    // 500 - fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneral(Exception ex, WebRequest request) {
        ProblemDetail pd = baseProblem(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request);
        pd.setProperty("code", ErrorCode.INTERNAL_ERROR.name());
        // Ne izlagati ex poruku; ostaje samo u logu
        log.error("500 Internal Server Error traceId={}", currentTraceId(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
    }

    // no-op

    // 400 - Pogrešan tip parametra / path varijable
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                            WebRequest request) {
        ProblemDetail pd = baseProblem(HttpStatus.BAD_REQUEST, "Invalid parameter type", request);
        pd.setProperty("code", ErrorCode.TYPE_MISMATCH.name());
        String expected = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        pd.setProperty("errors", Map.of(ex.getName(), "Expected type: " + expected));
        pd.setType(URI.create("https://api.example.com/problems/type-mismatch"));
        log.warn("400 Type mismatch traceId={}", currentTraceId());
        return ResponseEntity.badRequest().body(pd);
    }
}